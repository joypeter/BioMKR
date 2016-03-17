package com.glucopred;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.glucopred.service.EstimatorService;

public class SplashActivity extends Activity {

	private static final int SPLASH_SCREEN_TIME_IN_MILLIS = 500;
	private Handler mHhandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	    //Credentials u = Utils.createUserObject(pref);
	    
	    //if (u.username == null)
	    	//goToSettingsScreen();
	    //else
	    	//new LoginTask().execute(u); 
	    
	    goToMainScreen();
	    
	    // http://static.springsource.org/spring-android/docs/1.0.x/reference/html/rest-template.html
	    // http://wiki.fasterxml.com/JacksonDownload
	    // https://developer.android.com/reference/android/os/AsyncTask.html
	}
	
//	private class LoginTask extends AsyncTask<Credentials, Integer, Boolean> {
//		
//	     protected Boolean doInBackground(Credentials... users) {
//	    	 // Try to log in to Precise
//	    	 if (ServiceSession.login(users[0])) 
//	    		 if (ServiceSession.getApp( users[0].configid, users[0].autoupdate ))   // Use the CONFIGID from the settings
//	    			 if (ServiceSession.getPatient(false) != null) { // Get patient for this LDAP session
//			    		 ServiceSession.getModels(false);
//			    		 ServiceSession.getPatients(false);
//			    		 ServiceSession.getExperiments(false);
//			    		 return true;
//	    		 }
//    		 return false;
//	     }
//
//		protected void onProgressUpdate(Integer... progress) {
//	         //setProgressPercent(progress[0]);
//	     }
//
//	     protected void onPostExecute(Boolean result) {
//	    	 if (result) {
//	    		 if (ServiceSession.autoupdate) {
//	    			 PackageInfo pinfo;
//					try {
//						pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//						
//						int versionNumber = pinfo.versionCode;
//		    			String versionName = pinfo.versionName;
//		    		
//		    			// If ConfigID does not exits, do not start app!
//		    			if (ServiceSession.mobileapp == null) {
//		    				toast("Configuration ID does not exist on server!");
//		   	    		 	goToSettingsScreen();
//		    			} else {
//			    			// Check current app version
//		    				if (ServiceSession.mobileapp.versionCode != versionNumber) {
//		    					toast("App must update");
//		    					goToUpgradeScreen();
//		    				} else {
//		    					// Version ok
//		    					goToMainScreen();
//		    				}
//		    			}
//		    			
//					} catch (NameNotFoundException e) {
//						e.printStackTrace();
//						toast(e.getMessage());
//						goToMainScreen();
//					}
//	    		 } else {
//	    			 goToMainScreen();
//	    		 }
//	    	 } else {
////	    		 if (ServiceSession.patient == null) 
////	    			 toast("The user name is not linked to any patient! Contact system administrator");  
////    			 else
//    				 toast("Could not log in - check settings or LDAP user's patient link"); // TODO SOP
//	    		 goToSettingsScreen();
//	    	 }
//	     }
//	 }
	 
	protected void goToUpgradeScreen() {
		// http://stackoverflow.com/questions/11081998/allow-automatic-updating-feature-for-private-distributed-android-app
		 // http://stackoverflow.com/questions/3057771/is-there-a-way-to-automatically-update-application-on-android/3057965#3057965
		
		//Intent intent = new Intent(Intent.ACTION_VIEW ,Uri.parse(ServiceSession.GetAPKLink()));
		//startActivity(intent);    
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setDataAndType(Uri.parse(ServiceSession.GetAPKLink()), "application/vnd.android.package-archive");
//		startActivity(intent);
	}
	
	protected void goToMainScreen() {
		// Start the estimator service
		if (!isMyServiceRunning())
            startService(new Intent(this, EstimatorService.class));
		
		// Start the main screen
		startActivity(new Intent(this, MainActivity.class));
	}
	
	protected void goToSettingsScreen() {
		startActivity(new Intent(this, PrefsActivity.class));
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
	
	protected void toast(final String message) {
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
