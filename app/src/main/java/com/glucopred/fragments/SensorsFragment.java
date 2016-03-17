package com.glucopred.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.glucopred.R;
import com.glucopred.adapters.SensorSpinAdapter;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;

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
	
	// Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 3000;
	
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
 
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	mEstimatorService = ((EstimatorService.LocalBinder) service).getService();
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
        	mEstimatorService = null;
        }
    };
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sensors, container, false);
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
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
 
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
		        	mProgress = ProgressDialog.show(getActivity(), "Precise", "Disconnecting", true);
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
		        	mProgress = ProgressDialog.show(getActivity(), "Precise", "Connecting", true);
		        	mEstimatorService.connect(device.getAddress()); // Send the select address to the EstimatorService and let it connect
		        }
			}
		});
		
		onInvalidateData();
		
		Intent gattServiceIntent = new Intent(getActivity(), EstimatorService.class);
		getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);
		
		return view;
	}
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
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
            }
            
            buttonScan.setEnabled(!mConnected);
            spinSensors.setEnabled(!mConnected);
        }
    };
	
	@Override
	public void onInvalidateData() {
		try {
        	//if (bluetoothAddress != null && !mEstimatorService.isConnected())
        		//mEstimatorService.connect(bluetoothAddress);
//			BluetoothDevice[] arraystuff = new BluetoothDevice[1];
//        	arraystuff[0] = mEstimatorService.connectedDevice();
//            _adapter_sensors = new SensorSpinAdapter(getActivity().getApplicationContext(), 0, arraystuff);
			
        	if (_adapter_sensors != null) {
        		spinSensors.setAdapter(_adapter_sensors);
        		//spinSensors.setSelection(_adapter_sensors.getPosition(bluetoothAddress));
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
	
	// Scan for nearby devices
	private void scanLeDevice(final boolean enable) {
        if (enable) {
        	mDevices.clear();
        	_adapter_sensors = null;
        	mProgress = ProgressDialog.show(getActivity(), "Precise", "Scanning for sensors", true);
        	
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mProgress.dismiss();
                    
                    BluetoothDevice[] arraystuff = new BluetoothDevice[mDevices.size()];
                    int i= 0;
                    for (BluetoothDevice device : mDevices)
                    	arraystuff[i++] = device;
                    _adapter_sensors = new SensorSpinAdapter(getActivity().getApplicationContext(), 0, arraystuff);
                    
                    onInvalidateData();
                }
            }, SCAN_PERIOD);
 
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        onInvalidateData();
    }
	
	// Device scan callback.
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