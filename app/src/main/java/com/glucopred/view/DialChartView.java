package com.glucopred.view;

/**
 * Created by peter on 3/25/16.
 */

import java.util.ArrayList;
import java.util.List;

import org.xclcharts.chart.DialChart;
import org.xclcharts.common.MathHelper;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.plot.PlotAttrInfo;
import org.xclcharts.renderer.plot.Pointer;
import org.xclcharts.view.GraphicalView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @ClassName DialChart例子
 * @Description  仪表盘例子
 * @author XiongChuanLiang<br/>(xcl_168@aliyun.com)
 *
 */
public class DialChartView extends GraphicalView {

    private String TAG = "DialChartView";

    private DialChart chart = new DialChart();
    private float mPercentage = 0.0f;

    public DialChartView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView();
    }

    public DialChartView(Context context, AttributeSet attrs){
        super(context, attrs);
        initView();
    }

    public DialChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }


    private void initView()
    {
        chartRender();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chart.setChartRange(w ,h );
    }

    public void chartRender()
    {
        try {

            //设置标题背景
            //chart.setApplyBackgroundColor(true);
            //chart.setBackgroundColor(Color.rgb(0x20, 0xA7, 0x85));
            //绘制边框
            //chart.showRoundBorder();

            // total angle
            chart.setTotalAngle(270f);
            //chart.setStartAngle(0f);

            //设置当前百分比
            chart.getPointer().setPercentage(mPercentage/16f);

            //增加轴
            addAxis();

            //增加指针
            addPointer();

            //设置附加信息
            addAttrInfo();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
        }

    }

    public void addAxis()
    {

        List<Float> ringPercentage = new ArrayList<Float>();
        float rper = MathHelper.getInstance().div(1, 4);
        ringPercentage.add(rper);
        ringPercentage.add(rper);
        ringPercentage.add(rper);
        ringPercentage.add(rper);

        List<Integer> rcolor  = new ArrayList<Integer>();
        rcolor.add(Color.rgb(242, 110, 131));
        rcolor.add(Color.rgb(238, 204, 71));
        rcolor.add(Color.rgb(42, 231, 250));
        rcolor.add(Color.rgb(140, 196, 27));
        chart.addStrokeRingAxis(0.95f, 0.8f, ringPercentage, rcolor);
        //chart.getPlotAxis().get(0).getFillAxisPaint().setColor(Color.rgb(0x20, 0xA7, 0x85));

        List<String> rlabels  = new ArrayList<String>();
        rlabels.add("0");
        rlabels.add("2");
        rlabels.add("4");
        rlabels.add("6");
        rlabels.add("8");
        rlabels.add("10");
        rlabels.add("12");
        rlabels.add("14");
        rlabels.add("16");
        chart.addInnerTicksAxis(0.8f, rlabels);
        chart.getPlotAxis().get(1).getAxisPaint().setColor(Color.rgb(0x20, 0xA7, 0x85));
        chart.getPlotAxis().get(1).getTickLabelPaint().setColor(Color.rgb(0x20, 0xA7, 0x85));
        chart.getPlotAxis().get(1).getTickLabelPaint().setTextSize(30);
        chart.getPlotAxis().get(1).getTickMarksPaint().setColor(Color.rgb(0x20, 0xA7, 0x85));
        chart.getPlotAxis().get(1).hideAxisLine();
        chart.getPlotAxis().get(1).setDetailModeSteps(3);

/*        List<Float> ringPercentage1 = new ArrayList<Float>();
        List<Integer> rcolor1  = new ArrayList<Integer>();
        ringPercentage1.clear();
        ringPercentage1.add(1.0f);
        rcolor1.clear();
        rcolor1.add(Color.rgb(0x20, 0xA7, 0x85));
        chart.addStrokeRingAxis(0.7f, 0f, ringPercentage1, rcolor1);*/
    }

    //增加指针
    public void addPointer()
    {
        chart.addPointer();
        List<Pointer> mp = chart.getPlotPointer();
        mp.get(0).setPercentage(mPercentage/16f);
        //设置指针长度
        mp.get(0).setLength(0.75f);
        mp.get(0).getPointerPaint().setColor(Color.YELLOW);
        mp.get(0).setPointerStyle(XEnum.PointerStyle.TRIANGLE);
        //mp.get(0).hideBaseCircle();

        //设置指针长度
        chart.getPointer().setLength(0.75f);
        chart.getPointer().setPointerStyle(XEnum.PointerStyle.TRIANGLE);
        chart.getPointer().getPointerPaint().setStrokeWidth(3);
        chart.getPointer().getPointerPaint().setStyle(Style.FILL);

        chart.getPointer().getPointerPaint().setColor(Color.rgb(0x20, 0xA7, 0x85));
        chart.getPointer().getBaseCirclePaint().setColor(Color.rgb(0x20, 0xA7, 0x85));
        //chart.getPointer().setBaseRadius(10f);
    }


    private void addAttrInfo()
    {
        /////////////////////////////////////////////////////////////
        PlotAttrInfo plotAttrInfo = chart.getPlotAttrInfo();
        //设置附加信息
        Paint paintTB = new Paint();
        paintTB.setColor(Color.WHITE);
        paintTB.setTextAlign(Align.CENTER);
        paintTB.setTextSize(30);
        paintTB.setAntiAlias(true);
        paintTB.setColor(Color.rgb(0x20, 0xA7, 0x85));
        plotAttrInfo.addAttributeInfo(XEnum.Location.TOP, "Glucose Level", 0.3f, paintTB);

        Paint paintBT = new Paint();
        paintBT.setColor(Color.WHITE);
        paintBT.setTextAlign(Align.CENTER);
        paintBT.setTextSize(50);
        //paintBT.setFakeBoldText(true);
        //paintBT.setAntiAlias(true);
        paintBT.setColor(Color.rgb(0x20, 0xA7, 0x85));
        plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM,
                Float.toString(mPercentage), 0.3f, paintBT);

        Paint paintBT2 = new Paint();
        paintBT2.setColor(Color.WHITE);
        paintBT2.setTextAlign(Align.CENTER);
        paintBT2.setTextSize(30);
        paintBT2.setColor(Color.rgb(0x20, 0xA7, 0x85));
        //paintBT2.setFakeBoldText(true);
        //paintBT2.setAntiAlias(true);
        plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM, "mmol/l", 0.4f, paintBT2);
    }

    public void setCurrentStatus(float percentage)
    {
        mPercentage =  percentage;
        chart.clearAll();

        //设置当前百分比
        chart.getPointer().setPercentage(mPercentage/16f);
        addAxis();
        addPointer();
        addAttrInfo();
    }


    @Override
    public void render(Canvas canvas) {
        // TODO Auto-generated method stub
        try{
            chart.render(canvas);
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

}

