package com.glucopred.adapters;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.glucopred.R;
import com.glucopred.fragments.EstimationFragment;
import com.glucopred.fragments.FragmentEvent;
import com.glucopred.fragments.ManualInputFragment;
import com.glucopred.fragments.SensorsFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
	
	Context context;
	Fragment[] fragments;

	public SectionsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.context = context;
		fragments = new Fragment[3];
		fragments[0] = new EstimationFragment();
		fragments[1] = new ManualInputFragment();
		fragments[2] = new SensorsFragment();
	}

	public void invalidateFragments() {
		for (int i=0; i<fragments.length;i++)
			((FragmentEvent)fragments[i]).onInvalidateData();
	}
	
	@Override
	public Fragment getItem(int position) {
		Fragment fragment = fragments[position];
		
		Bundle args = new Bundle();
		//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public int getCount() {
		return fragments.length;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return context.getString(R.string.title_section0).toUpperCase(l);
		case 1:
			return context.getString(R.string.title_section1).toUpperCase(l);
		case 2:
			return context.getString(R.string.title_section2).toUpperCase(l);
		}
		return null;
	}
}
