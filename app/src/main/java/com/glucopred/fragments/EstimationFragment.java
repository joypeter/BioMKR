package com.glucopred.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.widget.TextView;

import com.glucopred.MainActivity;
import com.glucopred.R;
import com.glucopred.model.HistorianAgent;
import com.glucopred.model.TrendMode;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;
import com.glucopred.view.DialChartView;
import com.glucopred.view.TrendChartView;

import java.util.Date;

public class EstimationFragment extends Fragment implements FragmentEvent {
	private final static String TAG = EstimationFragment.class.getSimpleName();
	private TextView device_name_textview = null;
	private TextView connection_status_textview = null;
	DialChartView dial_chart = null;
	TrendChartView trend_chart = null;

	//private double _estCurrent = 0;
	private String connection_status = EstimatorService.STATE_DISCONNECTED;
	private String device_name = "";
	private String device_address = "";


	private RadioGroup radioGroup;
	private RadioButton radioWeek,radioYesterday,radioToday, radioRealtime;
	private TrendMode trendMode = TrendMode.REALTIME;   //0:runtime; 1:today; 2: yester: 3:week

	private MainActivity mActivity;
	private HistorianAgent mHistorianAgent;
	private EstimatorService mEstimatorService;
	private IBinder binder;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	
        	if (intent.getAction().equals(Utils.BLUETOOTH_NEWDATA)) {
        		double glucopred = extras.getFloat("g7");
				double finger = extras.getFloat("g2");
				glucopred = Utils.roundOneDecimal(glucopred);
				finger = Utils.roundOneDecimal(finger);
        		UpdateUI(glucopred, finger);

        	} else if (intent.getAction().equals(EstimatorService.ACTION_CONNECTION_STATUS)) {
				connection_status = extras.getString(EstimatorService.EXTRAS_DEVICE_CONN_STATUS);
				device_name = extras.getString(EstimatorService.EXTRAS_DEVICE_NAME);
				device_address = extras.getString(EstimatorService.EXTRAS_DEVICE_ADDRESS);
				UpdateConnectionStatus();
			}
        }
    };

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			binder = service;
			mEstimatorService = EstimatorService.getInstance();
			if (!mEstimatorService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
			}

			device_address = mEstimatorService.getPreviousDeviceAddress();
			device_name = mEstimatorService.getPreviousDeviceName();
			connection_status = mEstimatorService.getConnectionStatus();
			UpdateConnectionStatus();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
			mEstimatorService = null;
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_glucoseestimation, container, false);

		device_name_textview = (TextView)view.findViewById(R.id.target_device_name);
		connection_status_textview = (TextView)view.findViewById(R.id.connection_status);

		dial_chart = (DialChartView)view.findViewById(R.id.dial_chart);
		trend_chart = (TrendChartView)view.findViewById(R.id.trend_chart);

		mActivity = (MainActivity)  getActivity();
		mHistorianAgent = mActivity.getHistorianAgent();

		UpdateConnectionStatus();
		InitializeUI();

		radioGroup=(RadioGroup)view.findViewById(R.id.radiogroup);
		radioWeek = (RadioButton)view.findViewById(R.id.radioButtonWeek);
		radioYesterday = (RadioButton)view.findViewById(R.id.radioButtonYesterday);
		radioToday = (RadioButton)view.findViewById(R.id.radioButtonToday);
		radioRealtime = (RadioButton)view.findViewById(R.id.radioButtonRealtime);

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				trend_chart.refreshChart();
				TrendMode mode = TrendMode.REALTIME;
				if (checkedId == radioRealtime.getId()) {
					mode = TrendMode.REALTIME;
				}
				else if (checkedId == radioToday.getId()) {
					mode = TrendMode.TODAY;
				}
				else if (checkedId == radioYesterday.getId()) {
					mode = TrendMode.YESTERDAY;
				}
				else if (checkedId == radioWeek.getId()) {
					mode = TrendMode.WEEK;
				}

				trend_chart.drawHistorian(mHistorianAgent.getHistorian(mode));
			}
		});

		trendMode = TrendMode.REALTIME;
		radioRealtime.setChecked(true);

		Intent gattServiceIntent = new Intent(getActivity(), EstimatorService.class);
		getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);
		getActivity().getApplicationContext().registerReceiver(mReceiver, Utils.makeGattUpdateIntentFilter());
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
		if (device_name != null && !device_name.isEmpty()) {
			device_name_textview.setText(device_name + "(" + device_address + ")");
		} else {
			device_name_textview.setText("");
		}
	}

	private void InitializeUI() {
		dial_chart.setCurrentStatus(0f);
		trend_chart.clear();
		trend_chart.initChart();
		return;
	}

	private void UpdateUI(final double value, final double finger) {
		Date now = new Date();

		if (Double.isNaN(value)) {
			dial_chart.invalidate();
			trend_chart.pushGlucopred(now, 0f);
			return;
		}

		if (value != 0) {
			dial_chart.setCurrentStatus((float) value);
			dial_chart.invalidate();

			trend_chart.pushGlucopred(now, (float) value);
			mHistorianAgent.pushGlucopred(now, value);
		}

		if (!Double.isNaN(finger) && finger != 0) {
			mHistorianAgent.pushCurrentFinger(now, finger);
		}
	}
}
