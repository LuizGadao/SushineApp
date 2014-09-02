package com.luizcarlos.sunshine.fragments;

import android.content.SharedPreferences;
import android.database.DataSetObserver;
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
    private static final int NO_ITEM_SELECTED = -1;

    private AdapterListItemForecast adapter;
    private int currentItemListViewSelected = NO_ITEM_SELECTED;

    public ForecastFragment() {
    }

    public interface Callback{
        public void onItemSelected( WeatherDay weatherDay );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        ArrayList<String> list = new ArrayList<String>();

        adapter = new AdapterListItemForecast( getActivity(), new ArrayList<WeatherDay>() );
        if ( getResources().getBoolean( R.bool.isTablet ) ) {
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();

                    int item = currentItemListViewSelected == NO_ITEM_SELECTED ? 0 : currentItemListViewSelected;

                    WeatherDay weatherDay = (WeatherDay) adapter.getItem(item);
                    ((Callback) getActivity()).onItemSelected(weatherDay);

                    LogUtils.logInfo(LOG_TAG, "adapter data change");
                    LogUtils.logInfo(LOG_TAG, "data: " + weatherDay.getDay());
                }

                @Override
                public void onInvalidated() {
                    super.onInvalidated();
                }
            });
        }

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                WeatherDay weatherDay = (WeatherDay) adapter.getItem(position);
                ((Callback)getActivity()).onItemSelected( weatherDay );

                currentItemListViewSelected = position;
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
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask( adapter );

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        fetchWeatherTask.execute( location );
    }


}
