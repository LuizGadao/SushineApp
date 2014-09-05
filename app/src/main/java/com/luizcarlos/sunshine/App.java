package com.luizcarlos.sunshine;

import android.app.Application;

/**
 * Created by luizcarlos on 05/09/14.
 */
public class App extends Application
{
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();

        App.application = this;
    }

    public static Application getApplication(){ return application; }
}
