package com.luizcarlos.sunshine.fragments;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.luizcarlos.sunshine.Callback;
import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.adapters.AdapterListItemForecast;
import com.luizcarlos.sunshine.controller.ControllerSaveData;
import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.utils.LogUtils;
import com.luizcarlos.sunshine.utils.Utils;

import java.util.ArrayList;

    /**
 * Created by luizcarlos on 25/07/14.
 */
public class ForecastFragment extends Fragment {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int NO_ITEM_SELECTED = -1;

    private AdapterListItemForecast adapter;
    private int currentItemListViewSelected = NO_ITEM_SELECTED;
    private ControllerSaveData controllerSaveData;

    public ForecastFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        String location = getString( R.string.pref_location_key );
        Intent intent = new Intent("INTENT_SERVICE");
        intent.putExtra( getString( R.string.pref_location_key ), location );

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

                    LogUtils.info(LOG_TAG, "adapter data change");
                    LogUtils.info(LOG_TAG, "data: " + weatherDay.getDay());

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
                LogUtils.info(LOG_TAG, "click-item-action-search");
                updateLocation( true );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateLocation( false );
    }

    private void updateLocation( Boolean isRefresh ) {
        String location = Utils.getPreferenceLocation();
        controllerSaveData = new ControllerSaveData( this, location, isRefresh );

        /*
        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utils.getPreferenceLocation());

        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);

        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        //Set the AlarmManager to wake up the system.
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        */
    }

    public void updateAdapter( final ArrayList<WeatherDay> weatherDays )
    {
        LogUtils.info(LOG_TAG, "update-adapter");
        getActivity().runOnUiThread( new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll( weatherDays );
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("item_selected", currentItemListViewSelected);
    }
}
