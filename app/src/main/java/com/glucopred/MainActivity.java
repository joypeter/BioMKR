package com.glucopred;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.glucopred.fragments.EstimationFragment;
import com.glucopred.fragments.ManualInputFragment;
import com.glucopred.fragments.SensorsFragment;
import com.glucopred.service.EstimatorService;
import com.glucopred.utils.Utils;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
    SmartTabLayout viewPagerTab;
	
	SharedPreferences mPrefs;
	
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
        	mEstimatorService = EstimatorService.getInstance();
            
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
        setProgressBarIndeterminateVisibility(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewGroup tab = (ViewGroup) findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.nicer_pager, tab, false));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Manual Input", ManualInputFragment.class)
                .add("Glucose", EstimationFragment.class)
                .add("Sensor", SensorsFragment.class)
                .create());


        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(adapter);
        //set Glucose as default tab
        mViewPager.setCurrentItem(1);

        viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(mViewPager);

		
		// Set up the preference manager and a change listener
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
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
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	public void refreshData() {
        return;
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
            startActivity(new Intent(this, PrefsActivity.class));
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
}
