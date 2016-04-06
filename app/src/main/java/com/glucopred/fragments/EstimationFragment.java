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

import android.widget.TextView;
import android.widget.Toast;

import com.glucopred.R;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;
import com.glucopred.view.DialChartView;
import com.glucopred.view.TrendChartView;

import java.text.DecimalFormat;

public class EstimationFragment extends Fragment implements FragmentEvent {
	
	private TextView device_name_textview = null;
	private TextView connection_status_textview = null;
	DialChartView dial_chart = null;
	TrendChartView trend_chart = null;

	private double _estCurrent = 0;
	private String connection_status = EstimatorService.STATE_DISCONNECTED;
	private String device_name = "";
	private String device_address = "";
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	
        	if (intent.getAction().equals(Utils.BLUETOOTH_NEWDATA)) {
        		_estCurrent = extras.getFloat("g7");
				_estCurrent = Utils.roundOneDecimal(_estCurrent);
        		UpdateUI(); 
        	} else if (intent.getAction().equals(EstimatorService.ACTION_CONNECTION_STATUS)) {
				connection_status = extras.getString(EstimatorService.EXTRAS_DEVICE_CONN_STATUS);
				device_name = extras.getString(EstimatorService.EXTRAS_DEVICE_NAME);
				device_address = extras.getString(EstimatorService.EXTRAS_DEVICE_ADDRESS);
				UpdateConnectionStatus();
			}
        }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_glucoseestimation, container, false);

		device_name_textview = (TextView)view.findViewById(R.id.target_device_name);
		connection_status_textview = (TextView)view.findViewById(R.id.connection_status);

		dial_chart = (DialChartView)view.findViewById(R.id.dial_chart);
		trend_chart = (TrendChartView)view.findViewById(R.id.trend_chart);
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BLUETOOTH_NEWDATA);
		filter.addAction(EstimatorService.ACTION_CONNECTION_STATUS);
        getActivity().getApplicationContext().registerReceiver(mReceiver, filter);

		UpdateConnectionStatus();
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

	private void UpdateConnectionStatus() {
		connection_status_textview.setText(connection_status);
		if (!device_name.isEmpty()) {
			device_name_textview.setText(device_name + "(" + device_address + ")");
		} else {
			device_name_textview.setText("");
		}
	}

	private void UpdateUI() {
		if (Double.isNaN(_estCurrent)) {
			dial_chart.setCurrentStatus(0f);
			trend_chart.clear();
			trend_chart.initChart();
			return;
		}

		if (_estCurrent != 0) {
			dial_chart.setCurrentStatus((float) _estCurrent);
			dial_chart.invalidate();
			trend_chart.addEntry((float) _estCurrent);
		}
	}
}
