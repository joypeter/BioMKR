package com.glucopred.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.glucopred.R;
import com.glucopred.utils.Utils;
import com.glucopred.view.DialChartView;
import com.glucopred.view.TrendChartView;

import java.util.ArrayList;

public class EstimationFragment extends Fragment implements FragmentEvent {
	
	//private TextView txtEstimated;
	DialChartView dial_chart = null;
	TrendChartView trend_chart = null;

	private double _estCurrent = 0;

	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	
        	if (intent.getAction().equals(Utils.BLUETOOTH_NEWDATA)) {
        		_estCurrent = extras.getFloat("g7");
        		UpdateUI(); 
        	}
        }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_glucoseestimation, container, false);
		
		//txtEstimated = (TextView)view.findViewById(R.id.txtEstimated);
		dial_chart = (DialChartView)view.findViewById(R.id.dial_chart);
		trend_chart = (TrendChartView)view.findViewById(R.id.trend_chart);
		
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


	private void UpdateUI() {
		if (_estCurrent != 0) {
			//txtEstimated.setText(String.format("%.01f", _estCurrent));
			dial_chart.setCurrentStatus((float) _estCurrent);
			dial_chart.invalidate();
			trend_chart.addEntry((float) _estCurrent);
		}
		else
			;//txtEstimated.setText( "--.-");
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
