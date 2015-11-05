package com.pitmasteriq.qsmart.export;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.pitmasteriq.qsmart.DataModel;
import com.pitmasteriq.qsmart.DataSource;
import com.pitmasteriq.qsmart.Preferences;
import com.pitmasteriq.qsmart.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphActivity extends Activity
{
    private LinearLayout chartLyt;
    private long startTime, endTime;
    private boolean[] valuesToGraph;

    private String[] dataTitles;

    private DataSource dataSource;
    private List<DataModel> data;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        dataTitles = getResources().getStringArray(R.array.exportValues);
        startTime = getIntent().getLongExtra("startTime", -1);
        endTime = getIntent().getLongExtra("endTime", -1);

        dataSource = new DataSource(getApplicationContext());
        loadData();


        valuesToGraph = getIntent().getBooleanArrayExtra("values");

        chartLyt = (LinearLayout)findViewById(R.id.chart);
        chartLyt.addView(setupChart());
    }

    private View setupChart()
    {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        boolean f = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(Preferences.TEMPERATURE_UNITS, true);

        int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.CYAN, Color.YELLOW };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
                PointStyle.TRIANGLE, PointStyle.SQUARE };

        List<String> titles = new ArrayList<>();
        for (int i = 0; i < valuesToGraph.length; i++)
        {
            if (valuesToGraph[i])
            {
                titles.add(dataTitles[i]);
            }
        }

        List<Date> xValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            xValues.add(new Date(data.get(i).getDate()));
        }

        for (int i = 0; i < valuesToGraph.length; i++)
        {
            if (valuesToGraph[i])
            {
                renderer.addSeriesRenderer(buildSeriesRenderer(colors[i], styles[i]));
                dataset.addSeries(buildTimeSeries(titles.get(i), xValues, getYValues(i)));
            }
        }

        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }

        // Finaly we create the multiple series renderer to control the graph
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] { 20, 30, 15, 20 });
        // We want to avoid black border
        renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        // Disable Pan on two axis
        renderer.setPanEnabled(false, false);
        renderer.setYAxisMax(400);
        renderer.setYAxisMin(0);
        renderer.setXLabels(10);
        renderer.setYLabels(10);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setShowGrid(true); // we show the grid

        // Now we add our series


        GraphicalView chartView = ChartFactory.getTimeChartView(this, dataset, renderer, "h:mm a");

        //return null;
        return chartView;
    }

    private void loadData()
    {
        dataSource.open();
        data = dataSource.getDataInRange(startTime, endTime);
        dataSource.close();
    }

    private XYSeriesRenderer buildSeriesRenderer(int color, PointStyle style)
    {
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        return r;
    }

    private TimeSeries buildTimeSeries(String title, List<Date> dates, List<Integer> yValues)
    {
        TimeSeries series = new TimeSeries(title);
        for (int i = 0; i < dates.size(); i++)
        {
            series.add(dates.get(i), yValues.get(i));
        }

        Log.e("TAG", "series " + title + " count: " + series.getItemCount());

        return series;
    }

    private List<Integer> getYValues(int index)
    {
        List<Integer> v = new ArrayList<>();
        switch (index)
        {
            case 0:
                for (DataModel d : data)
                    v.add(d.getPitSet());
                break;
            case 1:
                for (DataModel d : data)
                    v.add(d.getPitTemp());
                break;
            case 2:
                for (DataModel d : data)
                    v.add(d.getFood1Temp());
                break;
            case 3:
                for (DataModel d : data)
                    v.add(d.getFood2Temp());
                break;
        }

        return v;
    }
}