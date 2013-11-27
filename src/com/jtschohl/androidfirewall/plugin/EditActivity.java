/**
 * This contains parts of the Tasker/Locale Plugin
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

package com.jtschohl.androidfirewall.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jtschohl.androidfirewall.R;

public class EditActivity extends Activity implements OnClickListener {

	final static String TAG = "{AF}";
	private Button defaultprofile;
	private Button profile1;
	private Button profile2;
	private Button profile3;
	private Button profile4;
	private Button profile5;
	private Button enable;
	private Button disable;
	int i = -1;
	String profile;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		BundleScrubber.scrub(getIntent());
		BundleScrubber.scrub(getIntent().getBundleExtra(
				com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));

		setContentView(R.layout.tasker_profile_buttons);

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
		this.enable = (Button) this.findViewById(R.id.firewallEnable);
		this.enable.setOnClickListener(this);
		this.disable = (Button) this.findViewById(R.id.firewallDisable);
		this.disable.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
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

		if (v.getId() == R.id.defaultprofile) {
			i = 0;
			profile = defaultProfile;
		}
		if (v.getId() == R.id.profile1) {
			i = 1;
			profile = Profile1;
		}
		if (v.getId() == R.id.profile2) {
			i = 2;
			profile = Profile2;
		}
		if (v.getId() == R.id.profile3) {
			i = 3;
			profile = Profile3;
		}
		if (v.getId() == R.id.profile4) {
			i = 4;
			profile = Profile4;
		}
		if (v.getId() == R.id.profile5) {
			i = 5;
			profile = Profile5;
		}
		if (v.getId() == R.id.firewallEnable) {
			i = 6;
			profile = getString(R.string.fw_enabled);
		}
		if (v.getId() == R.id.firewallDisable) {
			i = 7;
			profile = getString(R.string.fw_disabled);
		}
		Log.d(TAG, "value for EditActivity = " + i);
		finish();
	}

	public void finish() {
		Intent intent = new Intent();
		intent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, PluginBundleManager.generateBundle(getApplicationContext(), i + ""));
		intent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
				i + "");
		Log.d(TAG, "Value for storeposition is: " + i);
		Log.d(TAG, "putExtra1 = " + i + "");
		Log.d(TAG, "putExtra2 = " + profile);
		setResult(RESULT_OK, intent);
		super.finish();
	}
	
}