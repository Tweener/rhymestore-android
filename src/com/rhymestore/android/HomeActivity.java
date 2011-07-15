package com.rhymestore.android;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import com.rhymestore.android.rhymes.Rhyme;
import com.rhymestore.android.rhymes.RhymeService;
import com.rhymestore.android.utils.Utils;

public class HomeActivity extends GDActivity implements OnInitListener, OnClickListener
{
    private TextToSpeech textToSpeech;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private ArrayList<String> matches;

    private RhymeService rhymeService;

    private Button mSpeakButton;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addActionBarItem(getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(
            R.drawable.ic_action_bar_help), R.id.action_bar_help);

        setActionBarContentView(R.layout.home);

        mSpeakButton = (Button) findViewById(R.id.btn_speak);

        // Initialize the rhyme service
        rhymeService = new RhymeService(this);

        // Enable TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

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
                Utils.AlertShort(HomeActivity.this, "Language is not available.");
            }
            else
            {
                // getTheRhyme("Pillado en twitter");
            }
        }
        else
        {
            Utils.AlertShort(HomeActivity.this, "Could not initialize TextToSpeech.");
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
            Rhyme responseRhyme = rhymeService.getRhymeFromAPI(text);

            speechTheRhyme(responseRhyme.getText());

            Utils.AlertShort(HomeActivity.this, responseRhyme.getText());
        }
        catch (Exception ex)
        {
            Utils.AlertShort(HomeActivity.this, "Error getting the rhyme: " + ex.getMessage());
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
                Utils.AlertShort(HomeActivity.this, "No result, try again !");
            }
            // mList.setAdapter(new ArrayAdapter<String>(this,
            // android.R.layout.simple_list_item_1,
            // matches));
        }

        super.onActivityResult(requestCode, resultCode, data);
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
}
