package com.glucopred.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.highlight.Highlight;
import com.glucopred.R;
import com.glucopred.utils.Utils;

import java.util.Date;

public class TrendChartView extends LineChart implements OnChartValueSelectedListener {


    int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

    private static int MAX_VALUE_COUNT = 24;
    private static int MAX_X_VISIBLE_COUNT = 20;
    private static int MAX_Y_VISIBLE_COUNT = 15;

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

        // add an empty data object
        setData(new LineData());
        getXAxis().setDrawLabels(true);
        getXAxis().setDrawGridLines(true);

        //setVisibleXRangeMaximum(MAX_X_VISIBLE_COUNT);
        setVisibleYRangeMaximum(MAX_Y_VISIBLE_COUNT, AxisDependency.LEFT);

        invalidate();
    }

    public void refreshChart() {
        clear();
        setData(new LineData());

        invalidate();
    }

    public void pushCurrentData(float entryValue) {
        Date now = new Date();
        String timeString = Utils.getTimeString(now, "HH:mm:ss");

        addEntry(timeString, entryValue);
    }

    public void addEntry(String timeString, float entryValue) {
        LineData data = getData();

        if(data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            int dataSize = data.getXValCount();
            if (dataSize >= MAX_VALUE_COUNT)
                data.removeXValue(0);

            data.addXValue(timeString);
            data.addEntry(new Entry(entryValue, set.getEntryCount()), 0);

            notifyDataSetChanged();     // let the chart know it's data has changed

            moveViewToAnimated(data.getXValCount() - 7, 50f, AxisDependency.LEFT, 2000);        // this automatically refreshes the chart (calls invalidate())
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

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Glucose Level");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(getResources().getColor(R.color.theme_color));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(AxisDependency.LEFT);
        set.setValueTextSize(10f);

        return set;
    }
}
