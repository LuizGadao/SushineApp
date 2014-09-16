package com.luizcarlos.sunshine.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;

import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.adapters.AdapterListItemForecast;
import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.service.SunshineService;
import com.luizcarlos.sunshine.utils.LogUtils;

import java.util.ArrayList;

    /**
 * Created by luizcarlos on 25/07/14.
 */
public class ForecastFragment extends Fragment implements ServiceConnection {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int NO_ITEM_SELECTED = -1;

    private AdapterListItemForecast adapter;
    private int currentItemListViewSelected = NO_ITEM_SELECTED;
    private SunshineService.Controller controller;


    public interface Callback{
        public void onItemSelected( WeatherDay weatherDay );
    }

    public ForecastFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        String location = getString( R.string.pref_location_key );
        Intent intent = new Intent("INTENT_SERVICE");
        intent.putExtra( getString( R.string.pref_location_key ), location );
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);

        if ( savedInstanceState != null && savedInstanceState.containsKey( "item_selected" ) ) {
            currentItemListViewSelected = savedInstanceState.getInt("item_selected");
        }

        adapter = new AdapterListItemForecast( getActivity(), new ArrayList<WeatherDay>() );
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

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

                    listView.setSelection( item );
                }

                @Override
                public void onInvalidated() {
                    super.onInvalidated();
                }
            });
        }

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

        if ( adapter.getCount() == 0 )
            updateLocation();
    }

    private void updateLocation() {
        //FetchWeatherTask fetchWeatherTask = new FetchWeatherTask( adapter );


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //fetchWeatherTask.execute( location );

        Toast.makeText( getActivity(), "update location: " + location, Toast.LENGTH_LONG ).show();

        Intent intent = new Intent("INTENT_SERVICE");
        intent.putExtra( getString( R.string.pref_location_key ), location );

        getActivity().startService(intent);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);

        LogUtils.logInfo( LOG_TAG, "update-location" );
    }

    public void updateAdapter( final ArrayList<WeatherDay> weatherDays )
    {
        LogUtils.logInfo( LOG_TAG, "update-adapter" );
        getActivity().runOnUiThread( new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll( weatherDays );
            }
        } );

        Intent intent = new Intent("INTENT_SERVICE");
        getActivity().stopService( intent );
        getActivity().unbindService( this );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt( "item_selected", currentItemListViewSelected );
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        LogUtils.logInfo(LOG_TAG, "onServiceConnected");
        controller = (SunshineService.Controller) iBinder;
        controller.forecastFragment = this;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }


}
