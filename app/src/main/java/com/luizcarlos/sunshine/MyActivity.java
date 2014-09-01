package com.luizcarlos.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.luizcarlos.sunshine.fragments.ForecastFragment;
import com.luizcarlos.sunshine.utils.LogUtils;


public class MyActivity extends ActionBarActivity {

    private final static String TAG = MyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if ( findViewById( R.id.weather_detail_container ) == null )
            {
                fragmentTransaction
                        .add(R.id.fragment_forecast, new ForecastFragment());
            }
            else //tablet
            {

                ForecastFragment forecastFragment = new ForecastFragment();
                DetailActivity.DetailFragment detailFragment = new DetailActivity.DetailFragment();
                forecastFragment.setDetailFragment( detailFragment );

                fragmentTransaction.add( R.id.fragment_forecast, forecastFragment );
                fragmentTransaction.add( R.id.weather_detail_container, detailFragment);
            }

            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId())
        {
            case R.id.action_settings:
                startActivity( new Intent( getBaseContext(), SettingsActivity.class ) );
                return true;

            case R.id.action_map:
                openPreferredLocationInMap();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openPreferredLocationInMap() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPreferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default) );

        Uri geo = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geo);

        if ( intent.resolveActivity(getPackageManager()) != null )
            startActivity(intent);
        else
            LogUtils.logInfo(TAG, "Android não consegue resolver a intent.");

    }
}
