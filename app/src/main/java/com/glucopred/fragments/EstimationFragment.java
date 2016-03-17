package com.glucopred.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.glucopred.R;
import com.glucopred.utils.Utils;

public class EstimationFragment extends Fragment implements FragmentEvent {
	
	private TextView txtEstimated;

	// Plotting
//	private GraphicalView mGraphView;
//	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
//    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
//    private TimeSeries mEstimatedGlucose;
//    private TimeSeries mPredictedGlucose;
//    private TimeSeries mActualGlucose;
	private boolean graph_initialised = false;
	private double _estCurrent = 0;
	
	// http://achartengine.org/
	// http://code.google.com/p/achartengine/
	// http://www.javaadvent.com/2012/12/achartengine-charting-library-for.html
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	
        	if (intent.getAction().equals(Utils.BLUETOOTH_NEWDATA)) {
        		_estCurrent = extras.getFloat("g7");
        		//UpdateGraphUI();
        		UpdateUI(); 
        	}
        }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_glucoseestimation, container, false);
		
		//initChart();
		
		txtEstimated = (TextView)view.findViewById(R.id.txtEstimated);
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.prediction_plot);
//		mGraphView = ChartFactory.getTimeChartView(getActivity(), mDataset, mRenderer, "HH:mm"); // "d.M. HH:mm" 
//		layout.addView(mGraphView);
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BLUETOOTH_NEWDATA);
        getActivity().getApplicationContext().registerReceiver(mReceiver, filter);
        
        UpdateUI();

		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().getApplicationContext().unregisterReceiver(mReceiver);
	};
	
	@Override
	public void onInvalidateData() {
		
	}
	
//	private void initChart() {
//		
//		if (!graph_initialised) {
//			mActualGlucose = new TimeSeries("Actual");
//			mEstimatedGlucose = new TimeSeries("Estimated");
//			mPredictedGlucose = new TimeSeries("Predicted");
//			mDataset.addSeries(mActualGlucose);
//	        mDataset.addSeries(mEstimatedGlucose);
//	        mDataset.addSeries(mPredictedGlucose);
//	        
//	//        int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA };
//	//        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE , PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
//	        
//	        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
//	        float val2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, metrics);
//	        float val4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4, metrics);
//	        float val14 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, metrics);
//	        
//	        //mCurrentRenderer = new XYSeriesRenderer();
//	        //mRenderer = buildRenderer(colors, styles);
//	        
//	        //mCurrentRenderer.setColor(colors[0]);
//	        //mCurrentRenderer.setPointStyle(styles[0]);
//	        //mRenderer.addSeriesRenderer(mCurrentRenderer);rend.setPointStyle(styles[0]);
//	        XYSeriesRenderer r1 = new XYSeriesRenderer(); r1.setLineWidth(-1); r1.setColor(Color.GREEN); r1.setPointStyle(PointStyle.CIRCLE); r1.setPointStrokeWidth(val2);
//	        XYSeriesRenderer r2 = new XYSeriesRenderer(); r2.setLineWidth(val2); r2.setColor(Color.BLUE);
//	        XYSeriesRenderer r3 = new XYSeriesRenderer(); r3.setLineWidth(val2); r3.setColor(Color.RED);
//	        
//	        mRenderer.addSeriesRenderer(r1);
//	        mRenderer.addSeriesRenderer(r2);
//	        mRenderer.addSeriesRenderer(r3);
//	        
//	        mRenderer.setAxisTitleTextSize(val14);
//	        mRenderer.setChartTitleTextSize(val14);
//	        mRenderer.setLabelsTextSize(val14);
//	        mRenderer.setLegendTextSize(val14);
//	        mRenderer.setPointSize(val4);
//	        mRenderer.setMargins(new int[]{0,(int)val14*2, (int)val14*2 ,0}); // top left bottom right
//	        mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
//	        mRenderer.setApplyBackgroundColor(true);
//	        mRenderer.setBackgroundColor(Color.TRANSPARENT);
//	        mRenderer.setAxesColor(Color.BLACK);
//	        mRenderer.setLabelsColor(Color.BLACK);
//	        mRenderer.setShowGrid(true);
//	        mRenderer.setGridColor(Color.BLACK);
//	        mRenderer.setXLabelsAlign(Align.CENTER);
//	        mRenderer.setYLabelsAlign(Align.RIGHT);
//	        mRenderer.setZoomButtonsVisible(false);
//	        mRenderer.setXAxisMin(new Date().getTime() - (6*60*60*1000));
//	        mRenderer.setXAxisMax(new Date().getTime() + (1*60*60*1000));
//	        
//	        mRenderer.setYAxisMin(0);
//	        mRenderer.setYAxisMax(15);
//	        
//	        graph_initialised= true;
//		}
//		
//    }
	
//	private void UpdateGraphUI() {
//		try {
//			// Get data from cache, do not necessarily update from network
//			PredictionResult[] pr = ServiceSession.getEstimates(true, false);
//			long now = new Date().getTime();
//			long estNow = Long.MAX_VALUE;
//			double maxEst = 5.0;
//			
//	    	if (pr != null) {
//	    		mEstimatedGlucose.clear();
//	    		mPredictedGlucose.clear();
//	    		mActualGlucose.clear();
//	    		
//				for(int i=0; i<pr.length; i++) {
//					if (pr[i].value > maxEst)
//						maxEst = pr[i].value;
//	
//					if (pr[i].type == 0) { // Predicted or estimated glucose
//						if (pr[i].timestamp < now) {
//							mEstimatedGlucose.add(new Date(pr[i].timestamp), pr[i].value);
//						} else {
//							// Find lowest prediction index
//							if (pr[i].timestamp < estNow) {
//								estNow = pr[i].timestamp;
//								_estCurrent = pr[i].value;
//							}
//							
//							mPredictedGlucose.add(new Date(pr[i].timestamp), pr[i].value);
//						}
//					}
//					
//					if (pr[i].type == 2) { // Actual glucose
//						mActualGlucose.add(new Date(pr[i].timestamp), pr[i].value);
//					}
//				}
//	    	}
//	    	
//	    	UpdateUI(); 
//	    	
//	        if (mGraphView != null) {
//	        	mRenderer.setXAxisMin(new Date().getTime() - (6*60*60*1000));
//		        mRenderer.setXAxisMax(new Date().getTime() + (1*60*60*1000));
//		        mRenderer.setYAxisMin(0);
//		        mRenderer.setYAxisMax(maxEst + 2);
//				mGraphView.repaint();
//	//			mGraphView.zoomReset();
//	        }
//		} catch (HttpClientErrorException e) {
//    		e.printStackTrace();
//    		if (e.getStatusCode() == HttpStatus.FORBIDDEN)
//    			toast("Patient parameters incomplete");
//    		else
//    			toast(e.getMessage());
//		} catch (Exception e) {
//	    	e.printStackTrace();
//	    	toast(e.getMessage());
//		}
//	}
	
	private void UpdateUI() {
		if (_estCurrent != 0)
    		txtEstimated.setText( String.format("%.01f", _estCurrent));
		else
			txtEstimated.setText( "--.-");
	}
	
	private void toast(final String message) {
		getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, message, duration);
                toast.show();
            }
        });
    }

}
