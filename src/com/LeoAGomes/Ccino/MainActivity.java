package com.LeoAGomes.Ccino;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.content.*;

public class MainActivity extends Activity {
	int baudrate, timeout, buffersize;
	EditText sendtext;
	TextView cons;
	Button send;
	
	String TAG = "Ccino";

    /**
     * The device currently in use, or {@code null}.
     */
    private UsbSerialDriver mSerialDevice;

    /**
     * The system's USB service.
     */
    private UsbManager mUsbManager;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.updateReceivedData(data);
                }
            });
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		send = (Button)findViewById(R.id.button1);
		cons = (TextView)findViewById(R.id.textView1);
		sendtext = (EditText)findViewById(R.id.editText2);
		mScrollView  = (ScrollView)findViewById(R.id.demoScroller);
		
		send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Send(sendtext.getText().toString());
				sendtext.setText("");
			}
		});
		
		sendtext.setImeActionLabel("Send", KeyEvent.KEYCODE_ENTER);
		sendtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				Send(sendtext.getText().toString());
				sendtext.setText("");
				return false;
			}
		});
		
		//Probably FAIL
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.menu_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void Send(String data) {
		// Get UsbManager from Android.
		UsbManager manager1 = (UsbManager) getSystemService(Context.USB_SERVICE);

		// Find the first available driver.
		UsbSerialDriver driver1 = UsbSerialProber.acquire(manager1);

		if (driver1 != null) {
			try {
				driver1.open();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				driver1.setBaudRate(baudrate);

				byte[] send = data.getBytes();
				driver1.write(send, timeout);


			} catch (IOException e) {
				// Deal with error.
			} finally {
				try {
					driver1.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
		}
	}
    
	@Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (mSerialDevice != null) {
            try {
                mSerialDevice.close();
            } catch (IOException e) {
                // Ignore.
            }
            mSerialDevice = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Meu
        SharedPreferences prefs = this.getSharedPreferences("com.LeoAGomes.Ccino", Context.MODE_PRIVATE);
		baudrate = prefs.getInt("baudrate", 0);
		if (baudrate == 0) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("baudrate", 9600);
			editor.putInt("timeout", 1000);
			editor.putInt("buffersize", 160);
			editor.commit();
		}
		baudrate = prefs.getInt("baudrate", 0);
		timeout = prefs.getInt("timeout", 0);
		buffersize = prefs.getInt("buffersize", 0);
        
        //Cara's
        mSerialDevice = UsbSerialProber.acquire(mUsbManager);
        Log.d(TAG, "Resumed, mSerialDevice=" + mSerialDevice);
        if (mSerialDevice == null) {
            cons.setText("No serial device.");
        } else {
            try {
                mSerialDevice.open();
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                cons.setText("Error opening device: " + e.getMessage());
                try {
                    mSerialDevice.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialDevice = null;
                return;
            }
            cons.setText("Serial device: " + mSerialDevice + "\n");
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mSerialDevice != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mSerialDevice, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        //final String message = "Read " + data.length + " bytes: \n"
        //        + HexDump.dumpHexString(data) + "\n\n";
    	
    	String message = new String(data);
        cons.append(message);
        
        mScrollView.scrollTo(0, cons.getBottom());
    }
}
