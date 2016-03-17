package com.glucopred;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.glucopred.adapters.SectionsPagerAdapter;
import com.glucopred.adapters.SensorSpinAdapter;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;


public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for eŒach of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	SharedPreferences mPrefs;
	OnSharedPreferenceChangeListener mListener;
	
	ProgressDialog mProgress;
	
	private boolean mConnected = false;
	private EstimatorService mEstimatorService;
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (EstimatorService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (EstimatorService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
//                clearUI();
            }
        }
    };
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
 
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	mEstimatorService = ((EstimatorService.LocalBinder) service).getService();
            
            // Check connection state if we resume
            mConnected = mEstimatorService.isConnected(); 
        }
 
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	mEstimatorService = null;
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// Set up the preference manager and a change listener
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Instance field for listener
//		mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//		  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//			  if (key.equals("pref_role"))   
//				  refreshData();
//		  }
//		};
//		mPrefs.registerOnSharedPreferenceChangeListener(mListener);
		
		registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
		Intent gattServiceIntent = new Intent(this, EstimatorService.class);
		bindService(gattServiceIntent, mServiceConnection, this.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		if (!mConnected && isMyServiceRunning())
            stopService(new Intent(this, EstimatorService.class));
		
		unregisterReceiver(mGattUpdateReceiver);
		unbindService(mServiceConnection);
		
		super.onDestroy();
	}
	
	private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (EstimatorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	public void refreshData() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		//Credentials u = Utils.createUserObject(pref);
  	    
  	    //if (u.username != null) {
  	    	//mProgress = ProgressDialog.show(this, "Precise", "Refreshing buffer", true);
  	    	//new RefreshTask().execute(u);
  	    //} 
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case R.id.action_refresh:
        	refreshData();
        	return true;
        case R.id.action_settings:
        	// Launch Preference activity
            startActivity(new Intent(this, PrefsActivity.class)); // http://www.chupamobile.com/tutorial/details/114/Android+Persistence+with+preferences+and+files/
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, message, duration);
                toast.show();
            }
        });
    }
	
//	private class RefreshTask extends AsyncTask<Credentials, Integer, Boolean> {
//		
//	     protected Boolean doInBackground(Credentials... users) {
//	    	// Try to log in to Precise
//	    	 //if (ServiceSession.login(users[0])) 
//	    		 //if (ServiceSession.getPatient(false) != null)
//		    		 //if (ServiceSession.getModels(false) != null)
//		    			 //if (ServiceSession.getPatients(false) != null)
//		    				 //if (ServiceSession.getExperiments(false) != null)
////		    					 if (ServiceSession.clearCache("estimates")) {
////		    						 sendBroadcast(new Intent(Utils.ESTIMATOR_NEWDATA)); 
//		    						 //return true;
////		    					 }
//	    		 
//    		 return false;
//	     }
//
//		protected void onProgressUpdate(Integer... progress) {
//	         //setProgressPercent(progress[0]);
//	     }
//
//	     protected void onPostExecute(Boolean result) {
//	    	 mSectionsPagerAdapter.invalidateFragments();
//	    	 mProgress.dismiss();
//	    	 //if (!result) {
//	    		 //if (ServiceSession.patient == null) 
//	    			 //toast("The user name is not linked to any patient! Contact system administrator");  // TODO SOP
//    			 //else
//    				 //toast("Could not refresh!");
//	    	 //}
//	     }
//	 }
}
