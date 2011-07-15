package com.rhymestore.android.rhymes;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;

import com.rhymestore.android.api.APIManager;
import com.rhymestore.android.utils.Utils;

public class RhymeService
{
    private Context context;

    private String rhymeUrl;

    public RhymeService(final Context context)
    {
        setContext(context);

        // Configure the url to get the rhyme
        setRhymeUrl(APIManager.BASE_URL + "?model.rhyme=");
    }

    public static void speak(final String setence)
    {

    }

    public Rhyme getRhymeFromAPI(final String text) throws Exception
    {
        try
        {
            // Set the rhyme url concat to the text to rhyme with
            String url = getRhymeUrl() + text;
            InputStream in = APIManager.getInstance().sendGetRequest(url);
            InputSource ipsrc = new InputSource(in);

            // Parse the XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(ipsrc);
            doc.getDocumentElement().normalize();

            // Manage results from the XML to get the rhymes
            NodeList rhymeNodes = doc.getElementsByTagName("rhyme");

            if (rhymeNodes.getLength() > 0)
            {
                Node rhymeNode = rhymeNodes.item(0);
                String currentValue = rhymeNode.getTextContent();
                if (currentValue != null)
                {
                    return new Rhyme(currentValue);
                }
            }
            else
            {
                StringBuilder errors = new StringBuilder();
                NodeList errorNodes = doc.getElementsByTagName("error");

                for (int i = 0; i < errorNodes.getLength(); i++)
                {
                    errors.append(errorNodes.item(i).getTextContent());
                    errors.append(". ");
                }

                Utils.AlertShort(getContext(), errors.toString());
            }

            return null;
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
    }

    public void setContext(final Context context)
    {
        this.context = context;
    }

    public Context getContext()
    {
        return context;
    }

    public void setRhymeUrl(final String rhymeUrl)
    {
        this.rhymeUrl = rhymeUrl;
    }

    public String getRhymeUrl()
    {
        return rhymeUrl;
    }
}
