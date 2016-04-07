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


public class TrendChartView extends LineChart implements OnChartValueSelectedListener {


    int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

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

        invalidate();
    }

    public void addEntry(String xvalue, float entryValue) {
        LineData data = getData();

        if(data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // add a new x-value first
            //data.addXValue(set.getEntryCount() + "");
            data.addXValue(xvalue);

            // choose a random dataSet
            //int randomDataSetIndex = (int) (Math.random() * data.getDataSetCount());

            data.addEntry(new Entry(entryValue, set.getEntryCount()), 0);

            // let the chart know it's data has changed
            notifyDataSetChanged();

            setVisibleXRangeMaximum(24);
            setVisibleYRangeMaximum(15, AxisDependency.LEFT);
//
//            // this automatically refreshes the chart (calls invalidate())
            moveViewToAnimated(data.getXValCount() - 7, 50f, AxisDependency.LEFT, 2000);
        }

        invalidate();
    }

    private int getDataSize() {
        LineData data = getData();
        int size = data.getXValCount();
        return size;
    }

    private void removeLastEntry() {

        LineData data = getData();

        if(data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set != null) {

                Entry e = set.getEntryForXIndex(set.getEntryCount() - 1);

                data.removeEntry(e, 0);
                // or remove by index
                // mData.removeEntry(xIndex, dataSetIndex);

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
