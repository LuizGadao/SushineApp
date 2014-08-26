package com.luizcarlos.sunshine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.model.WeatherDay;

import java.util.ArrayList;

/**
 * Created by luizcarlos on 26/08/14.
 */
public class ListItemForecast extends BaseAdapter
{

    private final Context context;
    private LayoutInflater inflater;
    private ArrayList<WeatherDay> daysWeather;

    public ListItemForecast( Context context, ArrayList<WeatherDay> daysWeather )
    {
        this.context = context;
        this.daysWeather = daysWeather;
    }

    @Override
    public int getCount() {
        return daysWeather.size();
    }

    @Override
    public Object getItem(int position) {
        return daysWeather.get( position );
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convetView, ViewGroup viewGroup) {

        ViewHoder holder;

        if ( inflater == null )
            inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if ( convetView == null )
        {
            convetView = inflater.inflate( R.layout.list_item_forecast, null );

            holder = new ViewHoder();
            holder.day = (TextView) convetView.findViewById( R.id.text_day );
            holder.sesson = (TextView) convetView.findViewById( R.id.text_sesson );
            holder.maxTemp = (TextView) convetView.findViewById( R.id.temp_max );
            holder.minTemp = (TextView) convetView.findViewById( R.id.temp_min );

            convetView.setTag( holder );
        }
        else
            holder = (ViewHoder) convetView.getTag();

        WeatherDay weatherDay = daysWeather.get( position );
        holder.day.setText( weatherDay.getDay() );
        holder.sesson.setText( weatherDay.getDescription() );
        holder.maxTemp.setText( weatherDay.getMaxTemp() );
        holder.minTemp.setText( weatherDay.getMinTemp() );

        return convetView;
    }

    public void clear()
    {
        this.daysWeather.clear();
        notifyDataSetInvalidated();
    }

    public void addAll( ArrayList<WeatherDay> newsDaysWeather )
    {
        for ( WeatherDay day : newsDaysWeather )
        {
            if ( ! this.daysWeather.contains( day ) )
                this.daysWeather.add( day );
        }

        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }


    static class ViewHoder
    {
        //ImageView iconWeather;
        TextView day;
        TextView sesson;
        TextView maxTemp;
        TextView minTemp;
    }
}
