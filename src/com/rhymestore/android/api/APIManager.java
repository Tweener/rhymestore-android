package com.rhymestore.android.api;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.util.Base64;

import com.rhymestore.android.utils.SSLUtils;

public class APIManager
{
    private static APIManager instance;

    private static HttpClient httpClient;

    private String login;

    private String pass;

    public static final int REGISTRATION_TIMEOUT_MS = 30 * 1000; // ms

    public static final String BASE_URL = "https://www.rimamelo.com/web/api/";

    public static APIManager getInstance()
    {
        if (instance == null)
        {
            instance = new APIManager();
        }

        return instance;
    }

    public static void deleteInstance()
    {
        instance = null;
    }

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient(final HttpRequestBase request) throws Exception
    {
        if (httpClient == null)
        {
            httpClient = new DefaultHttpClient();
            final HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT_MS);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT_MS);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT_MS);
        }

        if (request.getURI().toString().startsWith("https"))
        {
            SSLUtils.installIgnoreCertTrustManager();
        }
    }

    /**
     * Try to authenticate the user with the given login and password
     * 
     * @param login
     * @param pass
     * @return Returns if the authentication is successful or fail
     */
    // public boolean tryAuthentication(final String login, final String pass) throws Exception
    // {
    // // Add authentication params
    // ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    // params.add(new BasicNameValuePair("contenu", "pwals"));
    //
    // HttpEntity entity = null;
    // try
    // {
    // entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
    // }
    // catch (final UnsupportedEncodingException e)
    // {
    // throw new AssertionError(e);
    // }
    //
    // setLogin(login);
    // setPass(pass);
    //
    // try
    // {
    // HttpResponse resp = sendGetRequest(BASE_URL);
    //
    // if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
    // {
    // String str = EntityUtils.toString(resp.getEntity());
    // Log.d("ABIQUO", "Authenticaion successful !");
    //
    // return true;
    // }
    // else
    // {
    // Log.d("ABIQUO", "Authenticaion error : " + resp.getStatusLine());
    // throw new Exception("Authenticaion error : " + resp.getStatusLine());
    // }
    // }
    // catch (Exception ex)
    // {
    // Log.d("ABIQUO", "Exception in auth: " + ex);
    // throw new Exception("Authenticaion error : " + ex.getMessage());
    // }
    // }

    /**
     * Encode the authentication credentials using Base64 following this schema : "login:pass"
     * 
     * @param login
     * @param pass
     * @return Encoded authentication credentials in Base64
     */
    private String getEncodedAuthenticationBase64(final String login, final String pass)
    {
        String auth = login + ":" + pass;
        String encAuth = Base64.encodeToString(auth.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);

        return "Basic " + encAuth;
    }

    public InputStream sendGetRequest(final String stringUrl) throws Exception
    {
        try
        {
            if (stringUrl.startsWith("https"))
            {
                SSLUtils.installIgnoreCertTrustManager();
            }

            URL url = new URL(stringUrl);
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("Authorization", getEncodedAuthenticationBase64(getLogin(),
                getPass()));

            return conn.getInputStream();
        }
        catch (Exception ex)
        {
            throw new Exception("Error while sending the request: " + ex.getMessage());
        }
    }

    public HttpResponse sendPostRequest(final String url, final ArrayList<NameValuePair> params)
        throws Exception
    {
        try
        {
            HttpEntity entity = null;
            try
            {
                entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            }
            catch (final UnsupportedEncodingException e)
            {
                throw new AssertionError(e);
            }

            HttpPost post = new HttpPost(url);
            post.setHeader("Accept", "*/*");
            post.setHeader("Authorization", getEncodedAuthenticationBase64(getLogin(), getPass()));
            post.setHeader(entity.getContentType());
            post.setEntity(entity);

            maybeCreateHttpClient(post);

            HttpResponse response = httpClient.execute(post);
            return response;
        }
        catch (Exception ex)
        {
            throw new Exception("Error while sending the request: " + ex.getMessage());
        }
    }

    public void setLogin(final String login)
    {
        this.login = login;
    }

    public String getLogin()
    {
        return login;
    }

    public void setPass(final String pass)
    {
        this.pass = pass;
    }

    public String getPass()
    {
        return pass;
    }
}
