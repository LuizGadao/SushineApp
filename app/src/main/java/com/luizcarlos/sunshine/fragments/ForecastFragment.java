package com.luizcarlos.sunshine.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.luizcarlos.sunshine.DetailActivity;
import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.adapters.AdapterListItemForecast;
import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.tasks.FetchWeatherTask;
import com.luizcarlos.sunshine.utils.LogUtils;

import java.util.ArrayList;

/**
 * Created by luizcarlos on 25/07/14.
 */
public class ForecastFragment extends Fragment {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private AdapterListItemForecast adapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        ArrayList<String> list = new ArrayList<String>();
        /*list.add("Today - Sunny - 88/63");®
        list.add("Monday - Sunny - 88/63");
        list.add("sunday - Sunny - 88/63");
        list.add("Friday - Sunny - 88/63");
        list.add("Wednesday - Sunny - 88/63");
        list.add("Satarday - Sunny - 88/63");*/

        adapter = new AdapterListItemForecast( getActivity(), new ArrayList<WeatherDay>() );

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String forecast = adapter.getItem(position);
                Intent intent = new Intent( getActivity(), DetailActivity.class );
                intent.putExtra(Intent.EXTRA_TEXT, "teste");
                intent.putExtra( "data_day", (WeatherDay) adapter.getItem( position ) );
                startActivity(intent);
                //Toast.makeText(getActivity(), "whether: " + forecast, Toast.LENGTH_SHORT).show();
            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecast_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() )
        {
            case R.id.action_refresh:
                LogUtils.logInfo( LOG_TAG, "click-item-action-search" );
                updateLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateLocation();
    }

    private void updateLocation() {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(adapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        fetchWeatherTask.execute( location );
    }
}
