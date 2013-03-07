/**
 * Class that allows user to modify settings of the app
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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.KeyEvent;
import android.widget.Toast;

public class UserSettings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.user_settings);
		/*PreferenceManager.setDefaultValues(UserSettings.this,
				R.layout.user_settings, false);*/

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePrefSummary(findPreference(key));
		
		if (key.equals("ipv6enabled")) {
			boolean ipv6 = sharedPreferences.getBoolean("ipv6enabled", false);
			if (ipv6){
				//resultOk();
			} else {
				purgeIp6Rules();
			}
		}
	}
	
	private void purgeIp6Rules() {
		final Resources res = getResources();
		final ProgressDialog progress = ProgressDialog.show(this,
				res.getString(R.string.working),
				res.getString(R.string.deleting_rules), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					progress.dismiss();
				} catch (Exception ex) {
				}
				if (!Api.hasRootAccess(getApplicationContext(), true))
					return;
				if (Api.purgeIp6tables(getApplicationContext(), true)) {
					Toast.makeText(getApplicationContext(), R.string.rules_deleted,
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}

	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			p.setSummary(listPref.getEntry());
		}
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			p.setSummary(editTextPref.getText());
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			resultOk();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Set the activity result to RESULT_OK and terminate this activity.
	 */
	private void resultOk() {
		final Intent response = new Intent(Api.PREF_PROFILES);
		setResult(RESULT_OK, response);
		finish();
	}
}