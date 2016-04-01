package com.glucopred.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.glucopred.R;

/**
 * Created by peter on 4/1/16.
 */
public class PrefFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
