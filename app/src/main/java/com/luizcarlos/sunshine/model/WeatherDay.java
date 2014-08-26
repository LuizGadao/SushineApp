package com.luizcarlos.sunshine.model;

import java.io.Serializable;

/**
 * Created by luizcarlos on 26/08/14.
 */
public class WeatherDay implements Serializable
{
    private String day;
    private String sesson;
    private String maxTemp;
    private String minTemp;
    private String description;

    public WeatherDay(String day, String sesson, String maxTemp, String minTemp) {
        this.day = day;
        this.sesson = sesson;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    public WeatherDay(String day, String sesson, String maxTemp, String minTemp, String description) {
        this.day = day;
        this.sesson = sesson;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.description = description;
    }

    public WeatherDay() {

    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getSesson() {
        return sesson;
    }

    public void setSesson(String sesson) {
        this.sesson = sesson;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
