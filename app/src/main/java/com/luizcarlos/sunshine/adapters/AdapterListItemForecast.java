package com.luizcarlos.sunshine.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.model.WeatherDay;

import java.util.ArrayList;

/**
 * Created by luizcarlos on 26/08/14.
 */
public class AdapterListItemForecast extends BaseAdapter
{

    private final Context context;
    private LayoutInflater inflater;
    private ArrayList<WeatherDay> daysWeather;

    private static final int VIEW_TYPE_TODAY = 0;


    public AdapterListItemForecast(Context context, ArrayList<WeatherDay> daysWeather)
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHoder holder;

        if ( inflater == null )
            inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if ( convertView == null )
        {
           convertView = inflater.inflate( getViewType( position ) , null);

            holder = new ViewHoder();
            holder.iconWeather = (ImageView) convertView.findViewById( R.id.image_weather );
            holder.day = (TextView) convertView.findViewById( R.id.text_day );
            holder.sesson = (TextView) convertView.findViewById( R.id.text_sesson );
            holder.maxTemp = (TextView) convertView.findViewById( R.id.temp_max );
            holder.minTemp = (TextView) convertView.findViewById( R.id.temp_min );

            convertView.setTag(holder);
        }
        else
            holder = (ViewHoder) convertView.getTag();

        WeatherDay weatherDay = daysWeather.get( position );
        Drawable drawable = getWetherIcon( position, daysWeather.get( position ).getId() );

        holder.iconWeather.setImageDrawable( drawable );
        holder.day.setText( weatherDay.getDay() );
        holder.sesson.setText( weatherDay.getDescription() );
        holder.maxTemp.setText( weatherDay.getMaxTemp() );
        holder.minTemp.setText( weatherDay.getMinTemp() );

        return convertView;
    }

    private int getViewType( int position )
    {
        return position == VIEW_TYPE_TODAY ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
    }

    private Drawable getWetherIcon( int positon, int id )
    {
        int iconId = positon == VIEW_TYPE_TODAY ?
                WeatherDay.getArtResourceForWeatherCondition( id ) : WeatherDay.getIconResourceForWeatherCondition( id );

        return getContext().getResources().getDrawable( iconId );
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
        ImageView iconWeather;
        TextView day;
        TextView sesson;
        TextView maxTemp;
        TextView minTemp;
    }
}
