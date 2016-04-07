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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.widget.TextView;
import android.widget.Toast;


import com.glucopred.MainActivity;
import com.glucopred.R;
import com.glucopred.model.HistorianAgent;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;
import com.glucopred.view.DialChartView;
import com.glucopred.view.TrendChartView;
import com.glucopred.model.TrendData;

import java.util.ArrayList;
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

	private RadioGroup radioGroup;
	private RadioButton radioWeek,radioYesterday,radioToday,radioRuntime;
	private int periodmode = 0;   //0:runtime; 1:today; 2: yester: 3:week

	private MainActivity mActivity;
	private HistorianAgent mHistorianAgent;

	double roundOneDecimal(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		return Double.valueOf(twoDForm.format(d));
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	
        	if (intent.getAction().equals(Utils.BLUETOOTH_NEWDATA)) {
        		_estCurrent = extras.getFloat("g7");
				_estCurrent = roundOneDecimal(_estCurrent);

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

		mActivity = (MainActivity)  getActivity();
		mHistorianAgent = mActivity.getHistorianAgent();

		UpdateConnectionStatus();
        UpdateUI();

		radioGroup=(RadioGroup)view.findViewById(R.id.radiogroup);
		radioWeek = (RadioButton)view.findViewById(R.id.radioButtonWeek);
		radioYesterday = (RadioButton)view.findViewById(R.id.radioButtonYesterday);
		radioToday = (RadioButton)view.findViewById(R.id.radioButtonToday);
		radioRuntime = (RadioButton)view.findViewById(R.id.radioButtonRuntime);

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				ArrayList<TrendData> trendData = new ArrayList<TrendData>();
				if (checkedId == radioWeek.getId()) {
					trendData = mHistorianAgent.getWeekData();
					periodmode = 3;
				} else if (checkedId == radioYesterday.getId()) {
					trendData = mHistorianAgent.getYesterdayData();
					periodmode = 2;
				} else if (checkedId == radioToday.getId()) {
					trendData = mHistorianAgent.getTodayData();
					periodmode = 1;
				} else if (checkedId == radioRuntime.getId()) {
					trendData = mHistorianAgent.getCurrentData(600);
					periodmode = 0;
				}

				trend_chart.initChart();
				for (int i = 0; i < trendData.size(); i++) {
					TrendData td = (TrendData) trendData.get(i);
					trend_chart.addEntry(td.getTimeString(), (float) td.getValue());
				}
			}
		});

		periodmode = 0;
		radioRuntime.setChecked(true);
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
			//txtEstimated.setText(String.format("%.01f", _estCurrent));
			dial_chart.setCurrentStatus((float) _estCurrent);
			dial_chart.invalidate();

			if (periodmode == 0) {
				trend_chart.pushCurrentData((float) _estCurrent);
			}
			mHistorianAgent.pushCurrent(_estCurrent);
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
