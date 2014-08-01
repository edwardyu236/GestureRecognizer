package com.edwardyu.gesturerecognizer.gesturerecognizerapp;

import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;

/**
 * Created by edwardyu on 01/08/14.
 */
public class TimeSeriesContainer {
    private String description;
    private TimeSeries timeSeries;

    public TimeSeriesContainer(String description, TimeSeries timeSeries) {
        this.description = description;
        this.timeSeries = timeSeries;
    }

    public String getDescription (){
        return description;
    }

    public TimeSeries getTimeSeries() {
        return timeSeries;
    }
}
