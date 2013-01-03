package com.LeoAGomes.Ccino;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
import android.view.View.OnClickListener;

public class Preferences extends Activity implements OnClickListener{
	EditText brate, to, bs;
	Button save;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.prefs_layout);
		
		brate = (EditText)findViewById(R.id.bratetxt);
		to = (EditText)findViewById(R.id.timouttxt);
		bs = (EditText)findViewById(R.id.buffersizetxt);
		save = (Button)findViewById(R.id.SaveBtn);
		
		SharedPreferences prefs = this.getSharedPreferences("com.LeoAGomes.Ccino", Context.MODE_PRIVATE);
		brate.setText(String.valueOf(prefs.getInt("baudrate", 0)));
		to.setText(String.valueOf(prefs.getInt("timeout", 0)));
		bs.setText(String.valueOf(prefs.getInt("buffersize", 0)));
		
		save.setOnClickListener(this);
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		SharedPreferences prefs = this.getSharedPreferences("com.LeoAGomes.Ccino", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		if (!brate.getText().toString().isEmpty() && !to.getText().toString().isEmpty() && !bs.getText().toString().isEmpty()) {
			try {
				editor.putInt("baudrate", Integer.parseInt(brate.getText().toString()));
				editor.putInt("timeout", Integer.parseInt(to.getText().toString()));
				editor.putInt("buffersize", Integer.parseInt(bs.getText().toString()));
				editor.commit();
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		finish();
	}
}
