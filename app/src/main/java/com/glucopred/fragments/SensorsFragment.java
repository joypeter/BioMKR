package com.glucopred.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.glucopred.R;
import com.glucopred.adapters.SensorSpinAdapter;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;

//sensorsFragment class is manager for "fragment_sensors" view
public class SensorsFragment extends Fragment implements FragmentEvent {
	private final static String TAG = SensorsFragment.class.getSimpleName();
	private SharedPreferences mPrefs;
	
	private static Button buttonScan;
	private static Button buttonConnect;
	private Spinner spinSensors;
	private static final int REQUEST_ENABLE_BT = 1;
	private SensorSpinAdapter _adapter_sensors;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private boolean mConnected = false;
    private Handler mHandler;
    private Handler mHandler2;
    private List<BluetoothDevice> mDevices;
    ProgressDialog mProgress;
    private EstimatorService mEstimatorService;
	private IBinder binder;
	
	// Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10 * 1000;
	
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
 
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
			binder = service;
        	mEstimatorService = EstimatorService.getInstance();
            if (!mEstimatorService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mEstimatorService.connect(mDeviceAddress);
            
            // Check connection state if we resume
            mConnected = mEstimatorService.isConnected(); 
            if (mConnected) {
            	buttonConnect.setText("Disconnect");
            	
            	BluetoothDevice[] arraystuff = new BluetoothDevice[1];
            	arraystuff[0] = mEstimatorService.connectedDevice();
                _adapter_sensors = new SensorSpinAdapter(getActivity().getApplicationContext(), 0, arraystuff);
                onInvalidateData();
            }
            
            buttonScan.setEnabled(!mConnected);
            spinSensors.setEnabled(!mConnected);
        }
 
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
        	mEstimatorService = null;
        }
    };
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sensors, container, false);
        TextView textView = (TextView)view.findViewById(R.id.textView1);
        textView.setText(getResources().getString(R.string.app_name) + " devices");
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		spinSensors = (Spinner)view.findViewById(R.id.spinner_sensors);
		mHandler = new Handler();
		mHandler2 = new Handler();
		mDevices = new ArrayList<BluetoothDevice>();
		
		// Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        	toast("Bluetooth LE not supported on handset");
            return null;
        }
 
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        /*final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
        	toast("Bluetooth LE not supported on handset");
            return null;
        }
		
		buttonScan = (Button)view.findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                scanLeDevice(true);
			}
		});
		
		buttonConnect = (Button)view.findViewById(R.id.buttonConnect);
		buttonConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		        if (mConnected) {
		        	mProgress = ProgressDialog.show(getActivity(), getResources().getString(R.string.app_name) , "Disconnecting", true);
		        	mEstimatorService.disconnect();

		        	// If no disconnect message is sent back, we pretend to have disconnected in any case
		        	mHandler2.postDelayed(new Runnable() {
		                @Override
		                public void run() {
		                	mConnected = false;
		                    mProgress.dismiss();
		                    buttonConnect.setText("Connect");
		                    
		                    buttonScan.setEnabled(!mConnected);
		                    spinSensors.setEnabled(!mConnected);
		                }
		            }, SCAN_PERIOD);
		        	
		        } else {
		        	BluetoothDevice device = (BluetoothDevice)spinSensors.getSelectedItem();
			        if (device == null) 
			        	return;
		        	mProgress = ProgressDialog.show(getActivity(), getResources().getString(R.string.app_name), "Connecting", true);
		        	mEstimatorService.connect(device.getName(), device.getAddress());
		        }
			}
		});
		
		onInvalidateData();
		
		Intent gattServiceIntent = new Intent(getActivity(), EstimatorService.class);
		getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);
        getActivity().registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
		
		return view;
	}

	private void addDevices()
	{
		sendMessage(mEstimatorService.MSG_STOP_SCAN);
		mProgress.dismiss();

		BluetoothDevice[] arraystuff = new BluetoothDevice[mDevices.size()];
		int i = 0;
		for (BluetoothDevice device : mDevices)
			arraystuff[i++] = device;
		_adapter_sensors = new SensorSpinAdapter(getActivity().getApplicationContext(), 0, arraystuff);

		onInvalidateData();
	}
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
			Bundle extras = intent.getExtras();
            if (EstimatorService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                if (mProgress != null)
                	mProgress.dismiss();
                buttonConnect.setText("Disconnect");
            } else if (EstimatorService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                if (mProgress != null)
                	mProgress.dismiss();
                buttonConnect.setText("Connect");
            } else if (EstimatorService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (EstimatorService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			} else if (EstimatorService.ACTION_CONNECTION_STATUS.equals(action)) {
				BluetoothDevice device = extras.getParcelable(EstimatorService.EXTRAS_DEVICE);
				if (null != device && !mDevices.contains(device)) {
					mDevices.add(device);
					addDevices();
				}
			}

            
            buttonScan.setEnabled(!mConnected);
            spinSensors.setEnabled(!mConnected);
        }
    };
	
	@Override
	public void onInvalidateData() {
		try {
        	if (_adapter_sensors != null) {
        		spinSensors.setAdapter(_adapter_sensors);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		getActivity().registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
        if (mEstimatorService != null) {
            //final boolean result = mEstimatorService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
		
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
 	}
	
	@Override
	public void onPause() {
		 super.onPause();
		 scanLeDevice(false);
		 getActivity().unregisterReceiver(mGattUpdateReceiver);
	 }
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unbindService(mServiceConnection);
	};

	private void sendMessage(int msgid)
	{
		if (binder != null) {
			Messenger messenger = new Messenger(binder);
			Message msg = Message.obtain(null, msgid);
			try {
				messenger.send(msg);
			}
			catch (RemoteException e) {
				System.out.println("failed to send message to mEstimatorService: " + e.getMessage());
			}
		}
	}

	private ProgressDialog.OnCancelListener mProgressOnCancel = new ProgressDialog.OnCancelListener()
	{
		@Override
		public void onCancel (DialogInterface dialogInterface)
		{
			sendMessage(mEstimatorService.MSG_STOP_SCAN);
		}
	};


	// Scan for nearby bluetooth devices
	private void scanLeDevice(final boolean enable) {
        if (enable) {
        	//mDevices.clear();
        	_adapter_sensors = null;
        	mProgress = ProgressDialog.show(getActivity(), getResources().getString(R.string.app_name), "Scanning for sensors", true, true, mProgressOnCancel);
        	
            // Stops scanning after a pre-defined scan period.
            /*mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					sendMessage(mEstimatorService.MSG_STOP_SCAN);
					mProgress.dismiss();

					BluetoothDevice[] arraystuff = new BluetoothDevice[mDevices.size()];
					int i = 0;
					for (BluetoothDevice device : mDevices)
						arraystuff[i++] = device;
					_adapter_sensors = new SensorSpinAdapter(getActivity().getApplicationContext(), 0, arraystuff);

					onInvalidateData();
				}
			}, SCAN_PERIOD);*/

			sendMessage(mEstimatorService.MSG_START_SCAN);
        } else {
			sendMessage(mEstimatorService.MSG_STOP_SCAN);
        }
        onInvalidateData();
    }
	
	// Device scan callback, when found bluetooth device
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
 
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        	if (device.getName() != null) {
	        	System.out.println("Found BLE device " + device.getName() + " " + device.getAddress());
	        	if (device.getName().equals("BioMKR") && !mDevices.contains(device))
	        		mDevices.add(device);
        	}
        }
    };
	
	private void toast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Context context = getActivity();
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(context, message, duration);
				toast.show();
			}
		});
    }	

	
	
}