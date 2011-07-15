package com.rhymestore.android;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.rhymestore.android.api.APIManager;
import com.rhymestore.android.rhymes.Rhyme;

public class HomeActivity extends GDActivity implements OnInitListener, OnClickListener
{
    private TextToSpeech textToSpeech;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private ArrayList<String> matches;

    private Button mSpeakButton;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(
            R.drawable.ic_action_bar_help), R.id.action_bar_help);

        setActionBarContentView(R.layout.home);

        // Enable TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        mSpeakButton = (Button) findViewById(R.id.btn_speak);

        // Check to see if a recognition activity is present
        checkRecognizerPresence();
    }

    @Override
    public boolean onHandleActionBarItemClick(final ActionBarItem item, final int position)
    {
        if (item.getItemId() == R.id.action_bar_help)
        {
            try
            {
                // Intent intent = new Intent(this, HelpActivity.class);
                // intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Help");
                // startActivity(intent);

                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
        else
        {
            return super.onHandleActionBarItemClick(item, position);
        }
    }

    /**
     * Handle the click on the start recognition button.
     */
    public void onClick(final View v)
    {
        if (v.getId() == R.id.btn_speak)
        {
            startVoiceRecognitionActivity();
        }
    }

    @Override
    public void onInit(final int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            int result = textToSpeech.setLanguage(new Locale("spa", "ESP"));

            if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                shortAlert("Language is not available.");
            }
            else
            {
                // getTheRhyme("Pillado en twitter");
            }
        }
        else
        {
            shortAlert("Could not initialize TextToSpeech.");
        }
    }

    /**
     * Speech a specific rhyme
     * 
     * @param sentence phrase to speech
     */
    private void speechTheRhyme(final String sentence)
    {
        textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Get the response rhyme from API and text-to-speech it
     */
    private void getTheRhyme(final String text)
    {
        try
        {
            Rhyme responseRhyme = getRhymeFromAPI(text);
            speechTheRhyme(responseRhyme.getText());
            shortAlert(responseRhyme.getText());
        }
        catch (Exception ex)
        {
            shortAlert("Error getting the rhyme: " + ex.getMessage());
        }
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "spa-ESP");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something in spanish");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Fill the list view with the strings the recognizer thought it could have heard
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches.size() > 0)
            {
                getTheRhyme(matches.get(0));
            }
            else
            {
                shortAlert("No result, try again !");
            }
            // mList.setAdapter(new ArrayAdapter<String>(this,
            // android.R.layout.simple_list_item_1,
            // matches));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void shortAlert(final String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private Rhyme getRhymeFromAPI(final String text) throws Exception
    {
        try
        {
            APIManager.getInstance().setLogin("rimamelo");
            APIManager.getInstance().setPass("R1m4mel0");

            String url = APIManager.BASE_URL + "?model.rhyme=" + text;
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

                // if (rhymeNode.getNodeType() == Node.ELEMENT_NODE)
                // {
                // Element eElement = (Element) rhymeNode;
                //
                // String currentValue = getTagValue("rhyme", eElement);
                // if (currentValue != null)
                // {
                // return new Rhyme(currentValue);
                // }
                // }
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

                shortAlert(errors.toString());
            }

            return null;
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
    }

    private void checkRecognizerPresence()
    {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities =
            pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0)
        {
            mSpeakButton.setOnClickListener(this);
        }
        else
        {
            mSpeakButton.setEnabled(false);
            mSpeakButton.setText("Recognizer not present");
        }
    }

    /**
     * Get the tag value of the current rhyme
     * 
     * @param tag Name of the tag
     * @param element Element to check
     * @return Value of the tag
     * @deprecated Use {@link Node#getTextContent()}
     */
    @Deprecated
    private static String getTagValue(final String tag, final Element element)
    {
        NodeList nlList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = nlList.item(0);

        return nValue.getNodeValue();
    }
}
