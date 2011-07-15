package com.rhymestore.android;

import greendroid.app.GDApplication;
import android.content.Intent;

public class RhymestoreApplication extends GDApplication
{

    @Override
    public Class< ? > getHomeActivityClass()
    {
        return HomeActivity.class;
    }

    @Override
    public Intent getMainApplicationIntent()
    {
        return null;
        // Enable this line to add link over the icon bar
        // return new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
    }
}
