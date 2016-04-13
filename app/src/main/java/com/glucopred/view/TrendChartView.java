package com.glucopred.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.highlight.Highlight;
import com.glucopred.model.FingerPrick;
import com.glucopred.model.GlucopredData;
import com.glucopred.model.HistorianResults;
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
    private long intervalOfDay = 60 * Utils.SECOND_MILLISECONDS;
    private long intervalOfHour = 10 * Utils.SECOND_MILLISECONDS;

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
        setDescription("");

        XAxis xAxis = getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(10.0f);

        getAxisRight().setEnabled(false);

        invalidate();
    }

    public void refreshChart() {
        clear();

        invalidate();
    }


    public void drawHistorian(HistorianResults historianResults) {
        if (historianResults == null)
            return;
        trendMode = historianResults.getTrendMode();

        switch (trendMode) {
            case REALTIME:
                long end = (new Date()).getTime();
                long start = end - 2 * Utils.HOUR_MILLISECONDS;
                long timeinterval = intervalOfHour;

                drawInterGlucopred(historianResults.getGlucopreds(), start, end, timeinterval);
                break;
            case TODAY:
                start = Utils.getDayStart(new Date());
                end = start + Utils.DAY_MILLISECONDS;
                timeinterval = intervalOfDay;

                drawInterGlucopred(historianResults.getGlucopreds(), start, end, timeinterval);
                break;
            case YESTERDAY:
                long yesterday = (new Date()).getTime() - Utils.DAY_MILLISECONDS;
                start = Utils.getDayStart(new Date(yesterday));
                end = start + Utils.DAY_MILLISECONDS;
                timeinterval = intervalOfDay;

                drawInterGlucopred(historianResults.getGlucopreds(), start, end, timeinterval);
                break;
            case WEEK:
                drawAverageGlucopred(historianResults.getAverageGlucopreds(), historianResults.getFingerPricks());
                break;
            default:
                break;
        }
    }

    private void drawInterGlucopred(RealmResults<GlucopredData> data, long start, long end, long timeinterval) {
        if (data == null)
            return;

        List<Entry> yVals = new ArrayList<Entry>();
        List<String> xVals = new ArrayList<String>();
        String timeFormat = getTimeFormat(trendMode);

        int i = 0, j = 0;
        long timestamp = start;
        int datasize = data.size();

        while (timestamp < end) {
            DateFormat df = new SimpleDateFormat(timeFormat);
            String timeString = df.format(timestamp);
            xVals.add(timeString);

            while (i < datasize) {
                GlucopredData gd = data.get(i);
                long time = gd.getTimestamp();
                if (time >= timestamp && time < timestamp + timeinterval) {
                    float val = (float) gd.getValue();
                    yVals.add(new Entry(val, j));
                    i++;
                    break;
                }
                else if (time < timestamp)
                    i++;
                else if (time >= timestamp + timeinterval)
                    break;
            }

            timestamp += timeinterval;
            j++;
        }

        setTrend(xVals, yVals);
    }

    private void drawAverageGlucopred(List<TrendData> data, RealmResults<FingerPrick> finger) {
        List<Entry> yVals = new ArrayList<Entry>();
        List<String> xVals = new ArrayList<String>();

        for (int i = 0; i < data.size(); i++) {
            TrendData td = data.get(i);
            float val = (float) td.getValue();

            xVals.add(td.getTimeString());
            yVals.add(new Entry(val,i));
        }

        setTrend(xVals, yVals);
    }

    private void drawAllGlucopred(RealmResults<GlucopredData> data) {
        List<Entry> yVals = new ArrayList<Entry>();
        List<String> xVals = new ArrayList<String>();

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

    private void setTrend(List xVals, List yVals) {
        clear();

        setLine(xVals, yVals);
        //setScatter(xVals, yVals);

        invalidate();
    }

    private void setLine(List xVals, List yVals) {
        LineDataSet set = new LineDataSet(yVals, "Glucose Level");
        set.setColor(Color.GREEN);
        set.setLineWidth(2.0f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setDrawCubic(false);
        set.setDrawFilled(false);
        LineData lineData = new LineData(xVals, set);
        setData(lineData);
    }

    private void setScatter(List xVals, List yVals) {
        clear();
        ScatterDataSet set = new ScatterDataSet(yVals, "Glucose Level");
        set.setColor(Color.GREEN);
        set.setDrawValues(false);
        set.setScatterShapeSize(2.0f);
        set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);

        ScatterData scatterData = new ScatterData(xVals, set);
        //setData(scatterData);
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

    public void pushGlucopred(Date time, float entryValue) {
        if (trendMode == TrendMode.REALTIME) {
            String timeString = Utils.getTimeString(time, "HH:mm:ss");

            addGlucopredToRealtime(timeString, entryValue);
        }
        else if (trendMode == TrendMode.TODAY) {
            addGlucopredToToday(time, entryValue);
        }
    }

    private void addGlucopredToToday(Date time, float entryValue) {
        LineData data = getData();
        //ScatterData data = getData();
        if (data == null)
            return;

        ILineDataSet set = data.getDataSetByIndex(0);
        //IScatterDataSet set = data.getDataSetByIndex(0);
        if (set == null)
            return;

        long start = Utils.getDayStart(new Date());
        long end = start + Utils.DAY_MILLISECONDS;
        long timestamp = start;
        int i = 0;
        long entrytime = time.getTime();

        while (timestamp < end) {
            if (entrytime >= timestamp && entrytime <= timestamp + intervalOfDay) {
                set.addEntry(new Entry(entryValue, i));
                notifyDataSetChanged();
                break;
            }

            timestamp += intervalOfDay;
            i++;
        }

        invalidate();
    }

    private void addGlucopredToRealtime(String timeString, float entryValue) {
        LineData data = getData();
        //ScatterData data = getData();
        if (data == null)
            return;

        ILineDataSet set = data.getDataSetByIndex(0);
        //IScatterDataSet set = data.getDataSetByIndex(0);
        if (set == null)
            return;

        //data.removeXValue(0);
        data.addXValue(timeString);
        int index = data.getXValCount() - 1;
        data.addEntry(new Entry(entryValue, index), 0);

        notifyDataSetChanged();     // let the chart know it's data has changed

        //moveViewToAnimated(index, 10f, YAxis.AxisDependency.LEFT, 2000);        // this automatically refreshes the chart (calls invalidate())

        int count = (int)(2 * Utils.HOUR_MILLISECONDS / intervalOfHour);
        setVisibleXRangeMaximum(count);
        moveViewToX(data.getXValCount() - count - 1);
        if (entryValue > 10) {
            getAxisLeft().setAxisMaxValue(entryValue);
            //moveViewToY(entryValue, YAxis.AxisDependency.LEFT);
        }

        invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }
}
