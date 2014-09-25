package com.luizcarlos.sunshine;

import com.luizcarlos.sunshine.model.WeatherDay;

/**
 * Created by luizcarlos on 19/09/14.
 */
public interface Callback {
    public void onItemSelected( WeatherDay weatherDay );
}
