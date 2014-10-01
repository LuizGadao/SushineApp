package com.luizcarlos.sunshine;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luizcarlos.sunshine.model.WeatherDay;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends ActionBarActivity {

    public static final String DATA_DAY = "data-day";
    private String stringExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable( DATA_DAY, getIntent().getSerializableExtra( DATA_DAY ) );
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments( bundle );

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, detailFragment)
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private View rootView;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);

            inflater.inflate( R.menu.detail, menu );

            MenuItem item = menu.findItem(R.id.action_share);

            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            if (shareActionProvider != null)
                shareActionProvider.setShareIntent(easyShare());
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId())
            {
                case R.id.action_settings:
                    startActivity( new Intent( getActivity(), SettingsActivity.class ) );
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
            String strShare = " #SunshineApp";

            intent.putExtra(Intent.EXTRA_TEXT, strShare);

            return intent;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            WeatherDay weatherDay = null;

            if ( getArguments() != null && getArguments().getSerializable( DATA_DAY ) != null )
                weatherDay = (WeatherDay) getArguments().getSerializable( DATA_DAY );

            if ( weatherDay != null )
            {
                setupView( weatherDay, View.VISIBLE );
            }
            else
                rootView.setVisibility( View.INVISIBLE );

            return rootView;
        }

        public void setupView( WeatherDay weatherDay, int visible ) {

            Context context = getActivity().getBaseContext();
            Date date = weatherDay.getDate();
            String dateDetail = new SimpleDateFormat("MMMM, d").format( date ).toString();

            ImageView imageView = (ImageView) rootView.findViewById( R.id.image_weather_detail );
            Drawable drawable = context.getResources().getDrawable( WeatherDay.getArtResourceForWeatherCondition(weatherDay.getId()) );
            imageView.setImageDrawable( drawable );

            TextView textDay = (TextView) rootView.findViewById( R.id.text_detail_day );
            TextView textDetailDate = (TextView) rootView.findViewById( R.id.text_detail_date );
            TextView textWeather = (TextView) rootView.findViewById( R.id.text_sesson_day );
            TextView tempMax = (TextView) rootView.findViewById( R.id.temp_max_day );
            TextView tempMin = (TextView) rootView.findViewById( R.id.temp_min_day );
            TextView umidty = (TextView) rootView.findViewById( R.id.text_umidity );
            TextView pressure = (TextView) rootView.findViewById( R.id.text_pressure );
            TextView windy = (TextView) rootView.findViewById( R.id.text_windy );

            textDay.setText( weatherDay.getDay() );
            textDetailDate.setText( dateDetail );
            textWeather.setText( weatherDay.getDescription() );
            tempMax.setText( weatherDay.getMaxTemp() );
            tempMin.setText( weatherDay.getMinTemp() );
            umidty.setText( context.getString( R.string.format_humidity, weatherDay.getHumidity() ) );
            pressure.setText( context.getString( R.string.format_pressure, weatherDay.getPressure() ) );
            windy.setText( weatherDay.getWindy() );

            rootView.setVisibility( visible );
        }

        public boolean isVisibleRootView()
        {
            return rootView.getVisibility() == View.VISIBLE? true : false;
        }

    }


}
