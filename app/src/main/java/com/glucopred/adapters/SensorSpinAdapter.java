package com.glucopred.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.glucopred.R;

import java.util.ArrayList;

public class SensorSpinAdapter extends ArrayAdapter<BluetoothDevice>{

    private Context context;
    private ArrayList<BluetoothDevice> values;

    public SensorSpinAdapter(Context context, int textViewResourceId, ArrayList<BluetoothDevice> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount(){
       return values.size();
    }

    public BluetoothDevice getItem(int position){
       return values.get(position);
    }

    public long getItemId(int position){
       return position;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

         View layout = inflater.inflate(R.layout.customspinner_imageview, parent, false);
         TextView tv = (TextView) layout.findViewById(R.id.tv);
         BluetoothDevice device = values.get(position);
         if (device != null ) {
             tv.setText(device.getName() + "(" + device.getAddress() + ")");

             ImageView img = (ImageView) layout.findViewById(R.id.img);
             img.setImageResource(R.drawable.cardio);

             tv.setTextSize(17f);
             tv.setTextColor(Color.BLACK);
         }
    	 
	     return layout;
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
    	return getCustomView(position, convertView, parent);
    }
    
    public int getPosition(String id) {
		int position = 0;
		
		if (id != null) {
			for (int i = 0; i<getCount(); i++)
				if (getItem(i).getAddress().equals(id))
					position = i;
		}
		return position;
	}
}