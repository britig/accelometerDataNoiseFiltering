package com.example.briti.filternoise;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import mr.go.sgfilter.SGFilter;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager manager;
    private Sensor accelerometer;
    private float x;
    private float y;
    private float z;
    private float normalizedValue;
    long timestamp;
    private LineGraphSeries<DataPoint> mSeries;
    private LineGraphSeries<DataPoint> mSeries2;
    //shows unfiltered accelerometer data
    private GraphView graph;
    //shows filtered accelerometer data
    private GraphView graph2;
    private double lastXValue = 5d;
    //Variable to store appended data points
    float data[] = new float[500];
    double timestamps[] = new double[500];
    double coefficients[];
    static int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graph = (GraphView) findViewById(R.id.graph);
        graph2 = (GraphView) findViewById(R.id.graph2);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Setting GraphView Viewport layouts
        Viewport gViewport =  graph.getViewport();
        Viewport gViewport2 = graph2.getViewport();
        gViewport.setXAxisBoundsManual(true);
        gViewport.setMinX(0);
        gViewport.setScrollable(true);
        gViewport.setMinY(0);
        gViewport.setScalable(true);
        // activate horizontal and vertical zooming and scrolling
        gViewport.setScalableY(true);
        // activate vertical scrolling
        gViewport.setScrollableY(true);
        gViewport2.setXAxisBoundsManual(true);
        gViewport2.setMinX(0);
        gViewport2.setScrollable(true);
        gViewport2.setMinY(0);
        gViewport2.setScalable(true);
        // activate horizontal and vertical zooming and scrolling
        gViewport2.setScalableY(true);
        // activate vertical scrolling
        gViewport2.setScrollableY(true);
        //add blank mseries to graph
        mSeries = new LineGraphSeries<>();
        //mseries features
        mSeries.setTitle("Acceleration curve");
        mSeries.setColor(Color.GREEN);
        mSeries.setDrawDataPoints(true);
        mSeries.setDataPointsRadius(10);
        mSeries.setThickness(8);
        graph.addSeries(mSeries);
        mSeries2 = new LineGraphSeries<>();
        //mseries features
        mSeries2.setTitle("Filtered curve");
        mSeries2.setColor(Color.BLUE);
        mSeries2.setDrawDataPoints(true);
        mSeries2.setDataPointsRadius(7);
        mSeries2.setThickness(6);
        graph2.addSeries(mSeries2);
        //Calculate sgolay filter coefficients
        coefficients = SGFilter.computeSGCoefficients(0,31,3);
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onPause(){
        super.onPause();
        manager.unregisterListener((SensorEventListener)this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            x=sensorEvent.values[0];
            y=sensorEvent.values[1];
            z=sensorEvent.values[2];
            normalizedValue = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
            timestamp=System.currentTimeMillis();
            lastXValue += 1d;
            //Append data to plot graph whenever there is a change i acceleration plots upto 500 data changes
            mSeries.appendData(new DataPoint(lastXValue,normalizedValue), true, 500);
            while(index < 500) {
                data[index] = normalizedValue;
                timestamps[index] = lastXValue;
                index++;
                break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onStartClick(View view){
        manager.registerListener((SensorEventListener)this,accelerometer,manager.SENSOR_DELAY_NORMAL);
    }

    public void onStopClick(View view){
        double yValue =5d;
        manager.unregisterListener((SensorEventListener)this);
        //Log.w("LOG", "inside");
        SGFilter sgfilt = new SGFilter(0,31);
        index=0;
        float smoothData[] = sgfilt.smooth(data,coefficients);
        for(int i=0;i<500;i++){
            if(timestamps[i]!=0) {
                mSeries2.appendData(new DataPoint(timestamps[i], smoothData[i]), true, 500);
            }else{
                break;
            }
        }

    }
}

