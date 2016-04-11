package com.glucopred.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.highlight.Highlight;
import com.glucopred.model.GlucopredData;
import com.glucopred.model.TrendData;
import com.glucopred.model.TrendMode;
import com.glucopred.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;

public class TrendChartView extends LineChart implements OnChartValueSelectedListener {


    int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

    private static int MAX_VALUE_COUNT = 24;
    private static int MAX_X_VISIBLE_COUNT = 20;
    private static int MAX_Y_VISIBLE_COUNT = 15;
    private TrendMode trendMode = TrendMode.REALTIME;

    public TrendChartView(Context context) {
        super(context);
        initChart();
    }

    public TrendChartView(Context context, AttributeSet attrs){
        super(context, attrs);
        initChart();
    }

    public TrendChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChart();
    }

    public void initChart()
    {
        setOnChartValueSelectedListener(this);
        setDrawGridBackground(false);
        setDescription("History Glucose Level");

        //setData(new LineData());

        XAxis xAxis = getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(10.0f);
        //setVisibleYRangeMaximum(MAX_Y_VISIBLE_COUNT, AxisDependency.LEFT);

        invalidate();
    }

    public void refreshChart(TrendMode mode) {
        clear();
        //setData(new LineData());

        trendMode = mode;
        //setTimelabels(mode);

        invalidate();
    }

    public void drawData(RealmResults<GlucopredData> data) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        String timeFormat = getTimeFormat(trendMode);

        for (int i = 0; i < data.size(); i++) {
            GlucopredData gd = data.get(i);
            float val = (float)gd.getValue();
            long time = gd.getTimestamp();

            DateFormat df = new SimpleDateFormat(timeFormat);
            String timeString = df.format(time);

            yVals.add(new Entry(val, i));
        }
    }

    public void drawRealtimeData(RealmResults<GlucopredData> data) {
        trendMode = TrendMode.REALTIME;
        if (data == null || data.size() <= 0)
            return;

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();
        String timeFormat = getTimeFormat(trendMode);

        long nowstamp = (new Date()).getTime();
        long timestamp = nowstamp - 2 * Utils.HOUR_MILLISECONDS;//Utils.getDayStart(now);
        long endTimestamp = nowstamp;

        int i = 0, j = 0;
        long timespan = 10 * Utils.SECOND_MILLISECONDS;
        int datasize = data.size();

        while (timestamp < endTimestamp) {
            if (i < datasize) {
                GlucopredData gd = data.get(i);
                long time = gd.getTimestamp();
                if (time >= timestamp && time < timestamp + timespan) {
                    float val = (float) gd.getValue();
                    yVals.add(new Entry(val, j));
                    i++;
                }
            }

            DateFormat df = new SimpleDateFormat(timeFormat);
            String timeString = df.format(timestamp);
            xVals.add(timeString);

            timestamp += timespan;
            j++;
        }

        setTrend(xVals, yVals);
    }

    public void drawTodayData(RealmResults<GlucopredData> data) {
        trendMode = TrendMode.TODAY;
        if (data == null || data.size() <= 0)
            return;

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();
        String timeFormat = getTimeFormat(trendMode);

        Date now = new Date();
        long timestamp = Utils.getDayStart(now);
        long endTimestamp = timestamp + Utils.DAY_MILLISECONDS;

        int i = 0, j = 0;
        int datasize = data.size();
        long timespan = 10 * Utils.SECOND_MILLISECONDS;

        while (timestamp < endTimestamp) {
            if (i < datasize) {
                GlucopredData gd = data.get(i);
                long time = gd.getTimestamp();
                if (time >= timestamp && time < timestamp + timespan) {
                    float val = (float) gd.getValue();
                    yVals.add(new Entry(val, j));
                    i++;
                }
            }

            DateFormat df = new SimpleDateFormat(timeFormat);
            String timeString = df.format(timestamp);
            xVals.add(timeString);

            timestamp += timespan;
            j++;
        }

        setTrend(xVals, yVals);
    }

    public void drawYesterdayData(RealmResults<GlucopredData> data) {
        trendMode = TrendMode.YESTERDAY;
        if (data == null || data.size() <= 0)
            return;

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();
        String timeFormat = getTimeFormat(trendMode);

        Date now = new Date();
        long timestamp = (new Date()).getTime() - Utils.DAY_MILLISECONDS;
        long starttime = Utils.getDayStart(new Date(timestamp));
        long endTimestamp = starttime + Utils.DAY_MILLISECONDS;
        timestamp = starttime;

        int i = 0, j = 0;
        int datasize = data.size();
        long timespan = 10 * Utils.SECOND_MILLISECONDS;

        while (timestamp < endTimestamp) {
            if (i < datasize) {
                GlucopredData gd = data.get(i);
                long time = gd.getTimestamp();
                if (time >= timestamp && time < timestamp + timespan) {
                    float val = (float) gd.getValue();
                    yVals.add(new Entry(val, j));
                    i++;
                }
            }

            DateFormat df = new SimpleDateFormat(timeFormat);
            String timeString = df.format(timestamp);
            xVals.add(timeString);

            timestamp += timespan;
            j++;
        }

        setTrend(xVals, yVals);
    }

    public void drawAverageData(List<TrendData> data) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < data.size(); i++) {
            TrendData td = data.get(i);
            float val = (float) td.getValue();

            xVals.add(td.getTimeString());
            yVals.add(new Entry(val,i));
        }

        setTrend(xVals, yVals);
    }

    private void setTrend(List xVals, List yVals) {
        clear();
        LineDataSet set = new LineDataSet(yVals, "Glucose Level");
        set.setColor(Color.GREEN);
        set.setLineWidth(1.0f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setDrawCubic(false);
        set.setDrawFilled(false);
        LineData lineData = new LineData(xVals, set);
        setData(lineData);
        invalidate();
    }

    private void setScatterTrend(List xVals, List yVals) {
        clear();
        ScatterDataSet set = new ScatterDataSet(yVals, "Glucose Level");
        set.setColor(Color.GREEN);

        ScatterData scatterData = new ScatterData(xVals, set);
        //setData(scatterData);
        invalidate();
    }

    private String getTimeFormat(TrendMode mode) {
        switch (mode) {
            case REALTIME:
                return "HH:mm:ss";
            case TODAY:
                return "HH:mm";
            case YESTERDAY:
                return "HH:mm";
            case WEEK:
                return "yyyy-MM-dd";
            default:
                return "HH:mm:ss";
        }
    }

    public void pushCurrentData(float entryValue) {
        if (trendMode == TrendMode.REALTIME) {
            Date now = new Date();
            String timeString = Utils.getTimeString(now, "HH:mm:ss");

            addSingleData(timeString, entryValue);
        }
        else if (trendMode == TrendMode.TODAY) {
            Date now = new Date();
            String timeString = Utils.getTimeString(now, "HH:mm:ss");

            addDataToToday(timeString, entryValue);
        }
    }

    private void addDataToToday(String timeString, float entryValue) {
        //TODO:

    }

    private void addSingleData(String timeString, float entryValue) {
        LineData data = getData();

        if(data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set != null) {
                data.addXValue(timeString);
                data.addEntry(new Entry(entryValue, data.getXValCount() - 1), 0);

                notifyDataSetChanged();     // let the chart know it's data has changed

                //moveViewToAnimated(data.getXValCount() - 7, 50f, AxisDependency.LEFT, 2000);        // this automatically refreshes the chart (calls invalidate())
            }
            //if (set == null) {
            //    set = createSet();
            //   data.addDataSet(set);
            //}

            //int dataSize = data.getXValCount();
            //if (dataSize >= MAX_VALUE_COUNT)
            //    data.removeXValue(0);
        }

        invalidate();
    }

    private void removeLastEntry() {
        LineData data = getData();

        if(data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set != null) {

                Entry e = set.getEntryForXIndex(set.getEntryCount() - 1);

                data.removeEntry(e, 0);

                notifyDataSetChanged();
                invalidate();
            }
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }
}
