package com.glucopred.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.glucopred.service.EstimatorService;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.FloatMath;

public class Utils {
    static final String LOG_TAG = "PRECISE";
    //public static final String ESTIMATOR_NEWDATA = "com.glucopred.precise.estimator.EstimatorService.action.ESTIMATOR_NEWDATA";
    public static final String BLUETOOTH_NEWDATA = "com.glucopred.service.EstimatorService.action.BLUETOOTH_NEWDATA";

    public static char[] getHexValue(byte[] array){
        char[] symbols="0123456789ABCDEF".toCharArray();
        char[] hexValue =
                new char[array.length * 2];

        for(int i=0;i<array.length;i++)
        {
            //convert the byte to an int
            int current = array[i] & 0xff;
            //determine the Hex symbol for the last 4 bits
            hexValue[i*2+1] = symbols[current & 0x0f];
            //determine the Hex symbol for the first 4 bits
            hexValue[i*2] = symbols[current >> 4];
        }
        return hexValue;
    }
    
    public static byte IntToUByte(int cmd) {
        final int value = (int) cmd & 0xFF;
        return  (byte) (value) ;
    }
    
    public static int UByteToInt(byte b) {
        return (int) b & 0xFF;
    }
    
    public static boolean isExternalStorageAvailable() 
    { 
        // Retrieving the external storage state
        String state = Environment.getExternalStorageState();

        // Check if available
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)); 
     }

    public static boolean isExternalStorageWritable() 
    {       
        // Retrieving the external storage state
        String state = Environment.getExternalStorageState();

        // Check if writable
        return (Environment.MEDIA_MOUNTED.equals(state)); 
     }
    
    // see http://androidsnippets.com/distance-between-two-gps-coordinates-in-meter
    public static double gps2m(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180/3.14169);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        float t1 = FloatMath.cos(a1)*FloatMath.cos(a2)*FloatMath.cos(b1)*FloatMath.cos(b2);
        float t2 = FloatMath.cos(a1)*FloatMath.sin(a2)*FloatMath.cos(b1)*FloatMath.sin(b2);
        float t3 = FloatMath.sin(a1)*FloatMath.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000*tt;
    }
    
	public static double getMaxValue(double[] numbers){  
		double maxValue = numbers[0];  
		for(int i=1;i < numbers.length;i++){  
			if(numbers[i] > maxValue){  
				maxValue = numbers[i];  
			}  
		}  
		return maxValue;  
	}  
	  
	public static double getMinValue(double[] numbers){  
		double minValue = numbers[0];  
		for(int i=1;i<numbers.length;i++){  
			if(numbers[i] < minValue){  
				minValue = numbers[i];  
			}  
		}  
		return minValue;  
	}  
	
	public static float[] toFloatArray(double[] arr) {
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float)arr[i];
		}
		return ret;
	}

	public static Double floatToDouble(float f) {
		Double doubleResult = Double.parseDouble(Float.toString(f));
		//Double doubleResult = new FloatingDecimal(f).doubleValue();  // http://stackoverflow.com/questions/916081/convert-float-to-double-without-losing-precision
		return doubleResult;
	}
	
	public static String getPref(SharedPreferences pref, String key, String defaultvalue) {
    	return pref.getString(key, defaultvalue);
    }
    
    public static void changePref(SharedPreferences pref, String key, String value) {
		SharedPreferences.Editor ed = pref.edit();
        ed.putString(key, value);
        ed.commit(); 
	}
    
    private static final String PREFIX = "json";
    
    private static void saveJSONObject(SharedPreferences pref, String key, JSONObject object) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREFIX+key, object.toString());
        editor.commit();
    }

    private static void saveJSONArray(SharedPreferences pref, String key, JSONArray array) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREFIX+key, array.toString());
        editor.commit();
    }

    private static JSONObject loadJSONObject(SharedPreferences pref, String key) throws JSONException {
        return new JSONObject(pref.getString(PREFIX+key, "{}"));
    }

    private static JSONArray loadJSONArray(SharedPreferences pref, String key) throws JSONException {
        return new JSONArray(pref.getString(PREFIX+key, "[]"));
    }

    public static void removeJSON(SharedPreferences pref, String key) {
        if (pref.contains(PREFIX+key)) {
            SharedPreferences.Editor editor = pref.edit();
            editor.remove(PREFIX+key);
            editor.commit();
        }
    }
    
    public static void changePrefArray(SharedPreferences pref, String key, float[] value) throws JSONException {
    	JSONArray json_arr = new JSONArray();
    	
    	for (int i=0; i<value.length; i++)
    		json_arr.put(i, floatToDouble(value[i]));
    	
    	saveJSONArray(pref, key, json_arr);
    	System.out.println("changePrefArray() " + key + " " + json_arr.toString());
    }
    
    public static float[] getPrefArray(SharedPreferences pref, String key) {
        float[] ret;
        
        try {
	        JSONArray json_arr = loadJSONArray(pref, key);
	        ret = new float[json_arr.length()];
	        for (int i=0; i<json_arr.length(); i++) {
	            ret[i] =  (float)json_arr.getDouble(i);
	        }
	        
	        System.out.println("getPrefArray() " + key + " " + json_arr.toString());	        
        } catch (Exception e) {
        	ret = new float[0];
        }
        
        return ret;
    }
    
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EstimatorService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(EstimatorService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(EstimatorService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(EstimatorService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

//	public static Credentials createUserObject(SharedPreferences pref) {
//		Credentials u = new Credentials();
//		
//		u.username = pref.getString("username", null);
//  	    u.password = pref.getString("userpasswd", null);
//  	    if (pref.getString("usersecret", null) != null)
//  	    	u.otp_verificationcode = GoogleAuthenticator.calculateValidationCode(pref.getString("usersecret", null));
//
//  	    u.useSecureService = !pref.getString("pref_role", "alt").equals("alt"); // use alternative IP as default
//  	    u.useURL = pref.getString("pref_role", "alt");
//  	    u.altIP = pref.getString("pref_comip", "192.168.1.100");
//  	    u.altPort = pref.getString("pref_comport", "8081");
//  	    u.configid = Integer.parseInt(pref.getString("pref_configid", "1"));
//  	    u.autoupdate = pref.getBoolean("pref_autoupdate", true);
//		
//		return u;
//	}
    
}
