package com.luizcarlos.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luizcarlos.sunshine.model.WeatherDay;

public class DetailActivity extends ActionBarActivity {

    private String stringExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.detail, menu );

        MenuItem item = menu.findItem(R.id.action_share);

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(easyShare());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_settings:
                startActivity( new Intent( getBaseContext(), SettingsActivity.class ) );
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Intent easyShare()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        String strShare = getIntent().getStringExtra(Intent.EXTRA_TEXT) + " #SunshineApp";

        intent.putExtra(Intent.EXTRA_TEXT, strShare);

        return intent;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Context context = getActivity().getBaseContext();

            String stringExtra = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
            WeatherDay weatherDay = (WeatherDay) getActivity().getIntent().getSerializableExtra( "data_day" );

            TextView textDay = (TextView) rootView.findViewById( R.id.text_detail_day );
            TextView textWeather = (TextView) rootView.findViewById( R.id.text_sesson_day );
            TextView tempMax = (TextView) rootView.findViewById( R.id.temp_max_day );
            TextView tempMin = (TextView) rootView.findViewById( R.id.temp_min_day );
            TextView umidty = (TextView) rootView.findViewById( R.id.text_umidity );
            TextView pressure = (TextView) rootView.findViewById( R.id.text_pressure );
            TextView windy = (TextView) rootView.findViewById( R.id.text_windy );

            textDay.setText( weatherDay.getDay() );
            textWeather.setText( weatherDay.getDescription() );
            tempMax.setText( weatherDay.getMaxTemp() );
            tempMin.setText( weatherDay.getMinTemp() );
            umidty.setText( context.getString( R.string.format_humidity, weatherDay.getHumidity() ) );
            pressure.setText( context.getString( R.string.format_pressure, weatherDay.getPressure() ) );
            return rootView;
        }
    }
}
