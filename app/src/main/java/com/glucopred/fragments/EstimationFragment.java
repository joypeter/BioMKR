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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
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

import java.text.DecimalFormat;

public class EstimationFragment extends Fragment implements FragmentEvent {
	private final static String TAG = EstimationFragment.class.getSimpleName();
	private TextView device_name_textview = null;
	private TextView connection_status_textview = null;
	DialChartView dial_chart = null;
	TrendChartView trend_chart = null;

	private double _estCurrent = 0;
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
				_estCurrent = Utils.roundOneDecimal(_estCurrent);
        		UpdateUI(_estCurrent);

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
				if (checkedId == radioWeek.getId()) {
					trend_chart.drawAverageData(mHistorianAgent.getWeekAverageData());
				} else if (checkedId == radioYesterday.getId()) {
					//trend_chart.drawYesterdayData(mHistorianAgent.getYesterdayData());
					trend_chart.drawAverageData(mHistorianAgent.getYesterdayAverageData());
				} else if (checkedId == radioToday.getId()) {
					trend_chart.drawTodayData(mHistorianAgent.getTodayData());
				} else if (checkedId == radioRealtime.getId()) {
					trend_chart.drawRealtimeData(mHistorianAgent.getRealtimeData());
				}
			}
		});

		trendMode = TrendMode.REALTIME;
		radioRealtime.setChecked(true);
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

	private void UpdateUI(final double value) {
		if (Double.isNaN(value)) {
			return;
		}

		if (value != 0) {
			dial_chart.setCurrentStatus((float) value);
			dial_chart.invalidate();

			Animation anim = new AlphaAnimation(0.1f, 1.0f);
			anim.setDuration(200);
			anim.setStartOffset(20);
			anim.setInterpolator(new BounceInterpolator());
			anim.setRepeatMode(Animation.REVERSE);
			anim.setRepeatCount(0);
			dial_chart.startAnimation(anim);


			trend_chart.pushCurrentData((float) value);
			mHistorianAgent.pushCurrent(value);
		}
	}
}
