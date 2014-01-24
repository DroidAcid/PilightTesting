<<<<<<< HEAD
<<<<<<< HEAD
package by.zatta.pilight.tasker;
=======
package com.frostedkiwi.pilightplugin;
>>>>>>> 5f32e04d34cc4fb985bda2a40e6171d1abeaea2c
=======
package com.frostedkiwi.pilightplugin;
>>>>>>> 5f32e04d34cc4fb985bda2a40e6171d1abeaea2c

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.app.Activity;
import android.content.Intent;
<<<<<<< HEAD
<<<<<<< HEAD
import by.zatta.pilight.R;
=======
>>>>>>> 5f32e04d34cc4fb985bda2a40e6171d1abeaea2c
=======
>>>>>>> 5f32e04d34cc4fb985bda2a40e6171d1abeaea2c

public class EditActivity extends Activity {
	
	String mServer, mPort, mLocation, mDevice, mState, mValue;
	EditText mServerText, mPortText, mLocationText, mDeviceText, mValueText;
	RadioGroup g;
	String[] mExtra;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		
<<<<<<< HEAD
<<<<<<< HEAD
		setContentView(R.layout.tasker_edit_activity_layout);
=======
		setContentView(R.layout.activity_edit);
>>>>>>> 5f32e04d34cc4fb985bda2a40e6171d1abeaea2c
=======
		setContentView(R.layout.activity_edit);
>>>>>>> 5f32e04d34cc4fb985bda2a40e6171d1abeaea2c
		mServerText = (EditText) findViewById(R.id.editTServerAddress);
		mPortText = (EditText) findViewById(R.id.editTPortNum);
		mLocationText = (EditText) findViewById(R.id.editTLocation);
		mDeviceText = (EditText) findViewById(R.id.editTDevice);
		g = (RadioGroup) findViewById(R.id.radioGState);
		mValueText = (EditText) findViewById(R.id.editTValue);
		
		if (savedInstanceState == null) {
			if (localeBundle != null) {
				mExtra = localeBundle.getStringArray("Extra");
				mServerText.setText(mExtra[0]);
				mPortText.setText(mExtra[1]);
				mLocationText.setText(mExtra[2]);
				mDeviceText.setText(mExtra[3]);
				if (mExtra[4] == "off"){
					g.check(R.id.radioOff);
				}
				mValueText.setText(mExtra[5]);
			}
		}
		setTitle("Settings");
	}
	
	public void finishActivity(View view){
		mServer = mServerText.getText().toString();
		mPort = mPortText.getText().toString();
		mLocation = mLocationText.getText().toString();
		mDevice = mDeviceText.getText().toString();
		int selected = g.getCheckedRadioButtonId();
		if (selected == R.id.radioOn){
			mState = "on";
		}
		else {
			mState = "off";
		}
		mValue = mValueText.getText().toString();
		mExtra = new String[6];
		mExtra[0] = mServer;
		mExtra[1] = mPort;
		mExtra[2] = mLocation;
		mExtra[3] = mDevice;
		mExtra[4] = mState;
		mExtra[5] = mValue;
		Bundle extraBundle = new Bundle();
		extraBundle.putStringArray("Extra", mExtra);
		Intent i = new Intent();
		i.putExtra("Server", mServer).putExtra("Port", mPort).putExtra("Location", mLocation).putExtra("Device", mDevice).putExtra("State", mState).putExtra("Value", mValue);
		i.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, extraBundle);
		String blurb = mServer + ":" + mPort + " => " + mLocation + " " + mDevice + " " + mState + " " + mValue;
		i.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);
		setResult(-1,i);
		finish();
	}

}

