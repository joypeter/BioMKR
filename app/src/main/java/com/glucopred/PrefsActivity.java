package com.glucopred;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class PrefsActivity extends PreferenceActivity {
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      addPreferencesFromResource(R.xml.preferences);
	      getActionBar().setDisplayHomeAsUpEnabled(true);
	  }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
	    case android.R.id.home: 
	        onBackPressed();
	        break;

	    default:
	        return super.onOptionsItemSelected(item);
	    }
	    return true;
	}
	
}
