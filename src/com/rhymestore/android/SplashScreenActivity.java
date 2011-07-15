package com.rhymestore.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.rhymestore.android.utils.Utils;

public class SplashScreenActivity extends Activity
{
    /** SplashScreen display time (in ms) **/
    private final int welcomeScreenDisplay = 3000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Create SplahScreen thread
        Thread welcomeThread = new Thread()
        {
            int wait = 0;

            @Override
            public void run()
            {
                try
                {
                    super.run();

                    while (wait < welcomeScreenDisplay)
                    {
                        sleep(100);
                        wait += 100;
                    }
                }
                catch (Exception e)
                {
                    Utils.AlertShort(SplashScreenActivity.this, e.getMessage());
                }
                finally
                {
                    // Launch Home activity
                    startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));

                    // Close current activity
                    finish();
                }
            }
        };

        // Start SplashScreen thread
        welcomeThread.start();
    }
}
