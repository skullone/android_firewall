/**
 * Dialog displayed to save a profile.
 * 
 * Copyright (C) 2012-2014	Jason Tschohl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jason Tschohl
 * @version 1.0
 */

package com.jtschohl.androidfirewall;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Displays the Create/Modify profile screen
 * 
 * @author jason
 * 
 */
public class SaveProfileDialog extends Activity implements OnClickListener {

	private Button defaultprofile;
	private Button profile1;
	private Button profile2;
	private Button profile3;
	private Button profile4;
	private Button profile5;

	final static String TAG = "{AF}";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.save_profile_buttons);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		String defaultProfile = prefs.getString("default",
				getString(R.string.defaultprofile));
		Log.d(TAG, "defaultProfile value is " + defaultProfile);
		String Profile1 = prefs.getString("profile1",
				getString(R.string.profile1));
		Log.d(TAG, "Profile1 value is " + Profile1);
		String Profile2 = prefs.getString("profile2",
				getString(R.string.profile2));
		Log.d(TAG, "Profile2 value is " + Profile2);
		String Profile3 = prefs.getString("profile3",
				getString(R.string.profile3));
		Log.d(TAG, "Profile3 value is " + Profile3);
		String Profile4 = prefs.getString("profile4",
				getString(R.string.profile4));
		Log.d(TAG, "Profile4 value is " + Profile4);
		String Profile5 = prefs.getString("profile5",
				getString(R.string.profile5));
		Log.d(TAG, "Profile5 value is " + Profile5);

		this.defaultprofile = (Button) this.findViewById(R.id.defaultprofile);
		this.defaultprofile.setText(defaultProfile);
		this.defaultprofile.setOnClickListener(this);
		this.profile1 = (Button) this.findViewById(R.id.profile1);
		this.profile1.setText(Profile1);
		this.profile1.setOnClickListener(this);
		this.profile2 = (Button) this.findViewById(R.id.profile2);
		this.profile2.setText(Profile2);
		this.profile2.setOnClickListener(this);
		this.profile3 = (Button) this.findViewById(R.id.profile3);
		this.profile3.setText(Profile3);
		this.profile3.setOnClickListener(this);
		this.profile4 = (Button) this.findViewById(R.id.profile4);
		this.profile4.setText(Profile4);
		this.profile4.setOnClickListener(this);
		this.profile5 = (Button) this.findViewById(R.id.profile5);
		this.profile5.setText(Profile5);
		this.profile5.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.defaultprofile) {
			saveDefaultProfile();
		}
		if (v.getId() == R.id.profile1) {
			saveProfile1();
		}
		if (v.getId() == R.id.profile2) {
			saveProfile2();
		}
		if (v.getId() == R.id.profile3) {
			saveProfile3();
		}
		if (v.getId() == R.id.profile4) {
			saveProfile4();
		}
		if (v.getId() == R.id.profile5) {
			saveProfile5();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}

	}

	/**
	 * Save Settings to a specific profile
	 */

	public void saveDefaultProfile() {
		Intent intent = new Intent(this, SaveSettingsToProfile.class);
		intent.putExtra("profileChoice", 1);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.error_af_folder, Toast.LENGTH_LONG)
					.show();
		}

	}

	public void saveProfile1() {
		Intent intent = new Intent(this, SaveSettingsToProfile.class);
		intent.putExtra("profileChoice", 2);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.error_af_folder, Toast.LENGTH_LONG)
					.show();
		}

	}

	public void saveProfile2() {
		Intent intent = new Intent(this, SaveSettingsToProfile.class);
		intent.putExtra("profileChoice", 3);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.error_af_folder, Toast.LENGTH_LONG)
					.show();
		}

	}

	public void saveProfile3() {
		Intent intent = new Intent(this, SaveSettingsToProfile.class);
		intent.putExtra("profileChoice", 4);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.error_af_folder, Toast.LENGTH_LONG)
					.show();
		}

	}

	public void saveProfile4() {
		Intent intent = new Intent(this, SaveSettingsToProfile.class);
		intent.putExtra("profileChoice", 5);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.error_af_folder, Toast.LENGTH_LONG)
					.show();
		}

	}

	public void saveProfile5() {
		Intent intent = new Intent(this, SaveSettingsToProfile.class);
		intent.putExtra("profileChoice", 6);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.error_af_folder, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}