package com.glucopred.service;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.glucopred.R;
import com.glucopred.api.Profile;
import com.glucopred.utils.Unsigned;
import com.glucopred.utils.Utils;

public class EstimatorService extends Service {
	private final static String TAG = EstimatorService.class.getSimpleName();
	private SharedPreferences mPrefs;

	// http://stackoverflow.com/questions/17870189/android-4-3-bluetooth-low-energy-unstable
	
	// Notification bar
	private NotificationManager _nm = null;
	private Notification.Builder _builder = null;
    private int mNotificationId = 2;    
    
    // Bluetooth
    private BluetoothManager mBluetoothManager;
 	private BluetoothAdapter mBluetoothAdapter;
 	private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_BATTERY_LEVEL =
            UUID.fromString(SampleGattAttributes.BATTERY_LEVEL);
    public final static UUID UUID_SENSOR_DATA_RX =
            UUID.fromString(SampleGattAttributes.SENSOR_DATA_RX);
    public final static UUID UUID_SENSOR_DATA_TX =
            UUID.fromString(SampleGattAttributes.SENSOR_DATA_TX);
    
 	private boolean mScanning;
    private Handler mHandler;
    
    public BluetoothGattCharacteristic _batteryLevel = null;
    public BluetoothGattCharacteristic _glucoseLevel = null;
    public BluetoothGattCharacteristic _rx_sensorData = null;
    public BluetoothGattCharacteristic _tx_sensorData = null;

    // Stops scanning after 4 seconds.
    private static final long SCAN_PERIOD = 4000;
    
    // https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
            	scanLeDevice(false);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                updateNotification("Connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                close();

                // TODO Restart stack
                
                updateNotification("Scanning...");
    			scanLeDevice(true);
            }
        }
 
        // Subscribe for notifications, take note of the interesting characteristics
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	List<BluetoothGattService> services = getSupportedGattServices();
            	if (services != null) {
            		for (BluetoothGattService service : services) {
            			if (service.getCharacteristic(UUID_BATTERY_LEVEL) != null) {
            				_batteryLevel = service.getCharacteristic(UUID_BATTERY_LEVEL);
            				setCharacteristicNotification(_batteryLevel, true);
            			}
            			
            			if (service.getCharacteristic(UUID_SENSOR_DATA_RX) != null) {
            				_rx_sensorData = service.getCharacteristic(UUID_SENSOR_DATA_RX);
            				setCharacteristicNotification(_rx_sensorData, true);
            			}
            			
            			if (service.getCharacteristic(UUID_SENSOR_DATA_TX) != null) {
            				_tx_sensorData = service.getCharacteristic(UUID_SENSOR_DATA_TX);
            			}
            		}
            	}
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
 
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
            	System.out.println("Success");
            System.out.println(status);
        }
        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
 
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    
    ByteArrayOutputStream _sensorDatabuffer = null;
    long _sensorDataBSONsize = 0;
    
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
    	try {
	    	// This is special handling for the Heart Rate Measurement profile.  Data parsing is
	    	// carried out as per profile specifications:
	    	// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
	    	if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	    		int flag = characteristic.getProperties();   //int flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
	    		int format = -1;
	    		if ((flag & 0x01) != 0) {
	    			format = BluetoothGattCharacteristic.FORMAT_UINT16;
	    			Log.d(TAG, "Heart rate format UINT16.");
	    		} else {
	    			format = BluetoothGattCharacteristic.FORMAT_UINT8;
	    			Log.d(TAG, "Heart rate format UINT8.");
	    		}
	    		
	    		final int heartRate = characteristic.getIntValue(format, 1);
	    		Log.d(TAG, String.format("Received heart rate: %d", heartRate));
	    		//intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
	    	} else if (UUID_SENSOR_DATA_RX.equals(characteristic.getUuid())) {
	    		final byte[] data = characteristic.getValue();
	    		handleSensordataFragment(data);
			} else {
				// For all other profiles, writes the data formatted in HEX.
				final byte[] data = characteristic.getValue();
				if (data != null && data.length > 0) {
					System.out.println(data.length);
					
					final StringBuilder stringBuilder = new StringBuilder(data.length);
					for(byte byteChar : data)
						stringBuilder.append(String.format("%02X ", byteChar));
					//intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
					Log.i(TAG, stringBuilder.toString());
				}
			}
	    	//sendBroadcast(intent);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void handleSensordataFragment(final byte[] data) throws Exception {
    	if (data != null && data.length > 0) {
			// Start condition
			if (data.length == 5 && data[0] == -1) { // -1 = 0xFF
				// First packet, first byte is 0xFF, next four bytes is the BSON document size
				_sensorDatabuffer = new ByteArrayOutputStream();
				ByteBuffer bb = ByteBuffer.wrap(data);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				bb.get();
				_sensorDataBSONsize = Unsigned.getUnsignedInt(bb);
			} else {
				if (_sensorDatabuffer != null) {
					_sensorDatabuffer.write(data);
					
					//System.out.println(data.length + " " + _sensorDatabuffer.size() + " " + _sensorDataBSONsize);
				
					// End condition
					if (_sensorDatabuffer.size() == _sensorDataBSONsize) {
						
						Profile prof = new Profile("", "", _sensorDatabuffer.toByteArray());
						
						System.out.println(prof.name + " " + _sensorDataBSONsize);

						if (prof.name.equals("g")) {
							Intent intent = new Intent();
							intent.setAction(Utils.BLUETOOTH_NEWDATA);
							for (int i=0; i<prof.y.size(); i++) {
								intent.putExtra(prof.x.get(i).toString(), prof.y.get(i).floatValue());
//								System.out.println(i + ": " + prof.x.get(i) + " " + prof.y.get(i) + " ");
							}
							sendBroadcast(intent);
							float d = 1.0f / 0.5615f;
							float v = 1 - (prof.y.get(13) * d);
							if (v<0)
								v=0;
							else
								v = v*100;
							
							updateNotification("Glucose " + String.format("%.01f", prof.y.get(12)) + " mmol/l (" + String.format("%.00f", v) + "%)");
							
						}
						
						if (prof.name.equals("r")) {
							Intent intent = new Intent();
							intent.setAction(Utils.BLUETOOTH_NEWDATA);
							for (int i=0; i<prof.y.size(); i++)
								intent.putExtra(prof.x.get(i).toString(), prof.y.get(i).floatValue());
							sendBroadcast(intent);
							updateNotification("Glucose " + String.format("%.01f", prof.y.get(5)) + " mmol/l");
						}
						
						if (prof.name.equals("aux")) {
							
						}
						
						_sensorDatabuffer = null;
						_sensorDataBSONsize = 0;
					} else if (_sensorDatabuffer.size() > _sensorDataBSONsize) {
						// Packet loss, we are probably into the next packet, so drop this
						_sensorDatabuffer = null;
						_sensorDataBSONsize = 0;
					}
				}
			}
		}
	}

	// Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //runOnUiThread(new Runnable() {
        	new Handler().post(new Runnable() {	
               @Override
               public void run() {
            	   String bluetoothAddress = Utils.getPref(mPrefs, "Connected_Device", null);
            	   System.out.println("Found BLE device " + device.getName() + " " + device.getAddress() + " Connected_Device " + bluetoothAddress);
        	
            	   if (bluetoothAddress != null && device.getAddress().equals(bluetoothAddress) && !isConnected()) {
            		   updateNotification("Connecting");
            		   connect(device.getAddress());
            	   }
              }
           });
       }
    };
    
     private void scanLeDevice(final boolean enable) {
         if (enable) {
             // Stops scanning after a pre-defined scan period.
//        	 mHandler = new Handler();
//             mHandler.postDelayed(new Runnable() {
//                 @Override
//                 public void run() {
//                	 if (mScanning) {
//                		 mScanning = false;
//                		 mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                		 scanLeDevice(true);
//                	 }
//                 }
//             }, SCAN_PERIOD);

             mScanning = true;
             mBluetoothAdapter.startLeScan(mLeScanCallback);
         } else {
             mScanning = false;
             mBluetoothAdapter.stopLeScan(mLeScanCallback);
         }
     }
     
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Initialize Bluetooth LE
		if (initialize()) {
			updateNotification("Scanning...");
			scanLeDevice(true);
		} else {
			updateNotification("Bluetooth not enabled");
		}
		
        return Service.START_STICKY;
     }
	
	@Override
	public void onDestroy() {
		if (_nm != null)
			_nm.cancelAll();
		close();
		super.onDestroy();
	}
	 
	public class LocalBinder extends Binder {
        public EstimatorService getService() {
            return EstimatorService.this;
        }
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }
 
    private final IBinder mBinder = new LocalBinder();
	
	private void updateNotification(String message) {
    	if (_builder == null) {
    		_builder = new Notification.Builder(this).
    				setContentTitle("GlucoPred")
    				.setSmallIcon(R.drawable.ic_launcher)
    				.setOnlyAlertOnce(true);
	        
//	        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class)
//	        	.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_CANCEL_CURRENT));  
	      }
    	_builder.setContentText(message);
    	
    	// http://feras.us/blog/custom-rich-notification-view-android/
    	// http://www.framentos.com/en/android-tutorial/2012/02/20/how-to-create-a-custom-notification-on-android/
    	Notification notification = _builder.getNotification();
    	RemoteViews customNotifView = new RemoteViews(EstimatorService.this.getPackageName(), R.layout.notification_view);
    	customNotifView.setTextViewText(R.id.text, "Hello World!");
	
    	_nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	     _nm.notify(mNotificationId, notification);
    }
	
	/**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
            	updateNotification("Unable to initialize BluetoothManager");
            	Log.e(TAG, "Unable to initialize BluetoothManager");
                return false;
            }
        }
 
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
        	updateNotification("Unable to obtain a BluetoothAdapter");
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
 
        return true;
    }
    
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
        	updateNotification("BluetoothAdapter not initialized or unspecified address");
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
 
        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
//        	Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }
 
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
        	updateNotification("Device not found.  Unable to connect.");
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        
        Utils.changePref(mPrefs, "Connected_Device", mBluetoothDeviceAddress);
        
        return true;
    }
    
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        Utils.changePref(mPrefs, "Connected_Device", null);
    }
    
    public boolean isConnected() {
    	return (mConnectionState == STATE_CONNECTED);
    }
    
    public BluetoothDevice connectedDevice() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null || mConnectionState != STATE_CONNECTED) return null;
		
		return mBluetoothGatt.getDevice();
	}
 
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
    	scanLeDevice(false);
    	
    	if (mBluetoothGatt == null) {
            return;
        }
        if (isConnected())
        	mBluetoothGatt.disconnect();
        
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
 
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
 
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
 
        // Write descriptors
        if (UUID_SENSOR_DATA_RX.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        
//        if (UUID_BATTERY_LEVEL.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
        
    }
 
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
 
        return mBluetoothGatt.getServices();
    }
    
    public void stopService() {
    	stopSelf();
    }

}
