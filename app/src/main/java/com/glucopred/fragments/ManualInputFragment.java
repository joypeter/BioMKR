package com.glucopred.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Profile;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.glucopred.R;

public class ManualInputFragment extends Fragment implements FragmentEvent {
	
	private SharedPreferences mPrefs;
	
	private NumberPicker nbInteger;
	private NumberPicker nbFloater;
	private NumberPicker numberPicker21;
	private NumberPicker numberPicker22;
	private NumberPicker numberPicker23;
	private NumberPicker numberPicker24;
	private NumberPicker numberPicker25;
	private Button buttonRegisterBG;
	private Button buttonRegisterCarbs;
	private Button buttonRegisterInsulin;
	private static Button buttonDate;
	private static Button buttonTime;
	private CheckBox chkSolid;
	private CheckBox chkRapid;
	private Timer _periodicTimer; 
	
	private static Calendar mCal;
	private static boolean _timeChanged;
	ProgressDialog mProgress;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_manualinput, container, false);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		nbInteger = (NumberPicker)view.findViewById(R.id.numberPicker1);
		nbFloater = (NumberPicker)view.findViewById(R.id.numberPicker2);
		numberPicker21 = (NumberPicker)view.findViewById(R.id.numberPicker21);
		numberPicker22 = (NumberPicker)view.findViewById(R.id.numberPicker22);
		numberPicker23 = (NumberPicker)view.findViewById(R.id.numberPicker23);
		numberPicker24 = (NumberPicker)view.findViewById(R.id.numberPicker24);
		numberPicker25 = (NumberPicker)view.findViewById(R.id.numberPicker25);
		chkSolid = (CheckBox)view.findViewById(R.id.chkSolid);
		chkRapid = (CheckBox)view.findViewById(R.id.chkRapid);
		
		mCal = Calendar.getInstance();
		_timeChanged = false;
		
		nbInteger.setMaxValue(30);
		nbInteger.setMinValue(1);
		nbInteger.setValue(5);
		nbInteger.setWrapSelectorWheel(false);
		nbFloater.setMaxValue(9);
		nbFloater.setMinValue(0);
		
		numberPicker21.setMaxValue(9);
		numberPicker21.setMinValue(0);
		numberPicker21.setValue(0);
		
		numberPicker22.setMaxValue(9);
		numberPicker22.setMinValue(0);
		numberPicker22.setValue(1);
		
		numberPicker23.setMaxValue(9);
		numberPicker23.setMinValue(0);
		numberPicker23.setValue(0);
		
		numberPicker24.setMaxValue(9);
		numberPicker24.setMinValue(0);
		numberPicker24.setValue(0);
		
		numberPicker25.setMaxValue(9);
		numberPicker25.setMinValue(0);
		numberPicker25.setValue(0);
		
		// 	Når brukeren kommer inn i skjermbildet/fragmentet, vil klokke/dato oppdateres til NOW kontinuerlig (hvert minutt e.l.) 
		// 	Hvis brukeren velger et tidspunkt/dato i egen dialog, oppdateres ikke feltene automatisk lenger og valgt dato/tid står
		// 	Når brukeren har sendt data til serveren, gjenopptas automatisk oppdatering av dato/tid
		// 	Hvis brukeren går ut av fragmentet og inn igjen, gjenopptas automatisk oppdatering
		
		 _periodicTimer = new Timer();
		 _periodicTimer.schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	if (!_timeChanged) {
	            		mCal = Calendar.getInstance();
	            		
	            		getActivity().runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                        	UpdateUI();
	                        }
	                    });
	            		
	            	}
	            }
	        }, 0, 1000); 
		
		buttonTime = (Button)view.findViewById(R.id.buttonTime);
		buttonTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DialogFragment newFragment = new TimePickerFragment();
			    newFragment.show(getActivity().getFragmentManager(), "timePicker");
			}
		});
		
		buttonDate = (Button)view.findViewById(R.id.buttonDate);
		buttonDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getActivity().getFragmentManager(), "datePicker");
			}
		});
		
		buttonRegisterBG = (Button)view.findViewById(R.id.buttonRegisterBG);
		buttonRegisterBG.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		    	float  bloodGlucose = ((float)nbInteger.getValue()) +  (((float)nbFloater.getValue()) / 10.0f );

//				mProgress = ProgressDialog.show(getActivity(), "Precise", "Registering manual blood glucose", true);
//	  	    	new RegisterTask().execute(profile_x);
			}
		});
		
		buttonRegisterCarbs = (Button)view.findViewById(R.id.buttonRegisterCarbs);
		buttonRegisterCarbs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String subtype = "Liquid";
				if (chkSolid.isChecked())
					subtype = "Solid";
				
		    	float  carbs = (float)(numberPicker21.getValue() * 100.0f) + (float)(numberPicker22.getValue() * 10.0f) + (float)(numberPicker23.getValue());  
				
//				mProgress = ProgressDialog.show(getActivity(), "Precise", "Registering manual carbohydrates", true);
//	  	    	new RegisterTask().execute(profile_x);
			}
		});
		
		buttonRegisterInsulin = (Button)view.findViewById(R.id.buttonRegisterInsulin);
		buttonRegisterInsulin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String subtype = "Slow";
				if (chkRapid.isChecked())
					subtype = "Rapid";
				
		    	float  insulin = (float)(numberPicker24.getValue() * 10.0f) + (float)(numberPicker25.getValue());
				
//				mProgress = ProgressDialog.show(getActivity(), "Precise", "Registering insulin", true);
//	  	    	new RegisterTask().execute(profile_x);
			}
		});
		
				
		onInvalidateData();
		UpdateUI();
		
		return view;
	}
	
	@Override
	public void onInvalidateData() {
		try {
//			ExperimentSpinAdapter adapter_e = new ExperimentSpinAdapter(getActivity().getApplicationContext(), 0, ServiceSession.experiments);
//			spinExperiment.setAdapter(adapter_e);
//			String experimentid = Utils.getPref(mPrefs, "ManualInputFragment_spinExperiment", null);
//			spinExperiment.setSelection(adapter_e.getPosition(experimentid));
//			
//			Experiment e = (Experiment)spinExperiment.getSelectedItem();
//			ServiceSession.setExperiment(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onPause() {
		 super.onPause();
//		 Utils.changePref(mPrefs, "ManualInputFragment_spinExperiment", ((Experiment)spinExperiment.getSelectedItem()).id);
	 }
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		_periodicTimer.cancel();
		_periodicTimer.purge();
		//System.out.println(spinExperiment.getSelectedItem());
	};
	
	public static void UpdateUI() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd. MMM yyyy");
		
		buttonDate.setText(sdf2.format(mCal.getTime()));
		buttonTime.setText(sdf.format(mCal.getTime()));
	}
	
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

	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, mCal.get(Calendar.HOUR_OF_DAY), mCal.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			_timeChanged = true;
			mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mCal.set(Calendar.MINUTE, minute);
			UpdateUI();
		}
	}
	
	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH));
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			_timeChanged = true;
			mCal.set(Calendar.YEAR, year);
			mCal.set(Calendar.MONTH, month);
			mCal.set(Calendar.DAY_OF_MONTH, day);
			UpdateUI();
		}
	}
	
	private class RegisterTask extends AsyncTask<Profile, Integer, Boolean> {
		
	     protected Boolean doInBackground(Profile... profiles) {
	    	 // Register result
	    	 try {
	    		 //ServiceSession.postProfile(profiles[0]);
	    		 //ServiceSession.updateEstimatorTimestart(profiles[0].date, ServiceSession.patient.id);
	    	 } catch (Exception e) {
	    		 toast(e.getMessage());
	    		 return false;
	    	 }
	    	 
	    	 return true;
	     }

		protected void onProgressUpdate(Integer... progress) {
	         //setProgressPercent(progress[0]);
	     }

	     protected void onPostExecute(Boolean result) {
//	    	 mSectionsPagerAdapter.invalidateFragments();
	    	 mProgress.dismiss();
	    	 if (result)
	    		 toast("Manual input is saved to database");
	    	 else
	    		 toast("ERROR when saving!");
	    	 _timeChanged = false;
	     }
	 }

	
}
