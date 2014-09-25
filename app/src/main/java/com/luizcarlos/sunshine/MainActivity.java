package com.luizcarlos.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.utils.LogUtils;


public class MainActivity extends ActionBarActivity implements Callback {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if ( findViewById( R.id.weather_detail_container ) != null )
        {
            //TABLET
            if ( savedInstanceState != null ) {
                fragmentTransaction
                        .replace(R.id.weather_detail_container, new DetailActivity.DetailFragment())
                        .commit();
            }
        }

        //ForecastFragment forecastFragment = (ForecastFragment) fragmentManager.findFragmentById( R.id.fragment_forecast );
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
            LogUtils.info(TAG, "Android não consegue resolver a intent.");

    }


    @Override
    public void onItemSelected(WeatherDay weatherDay)
    {
        if( ! getResources().getBoolean( R.bool.isTablet ) ) {

            //String forecast = adapter.getItem(position);
            Intent intent = new Intent( this , DetailActivity.class);
            intent.putExtra(DetailActivity.DATA_DAY, weatherDay );
            startActivity(intent);
            //Toast.makeText(getActivity(), "whether: " + forecast, Toast.LENGTH_SHORT).show();
        }else // tablet
        {

            DetailActivity.DetailFragment detailFragment = new DetailActivity.DetailFragment();
            Bundle args = new Bundle();
            args.putSerializable( DetailActivity.DATA_DAY, weatherDay );
            detailFragment.setArguments( args );

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace( R.id.weather_detail_container, detailFragment)
                    .commit();
        }

    }
}
