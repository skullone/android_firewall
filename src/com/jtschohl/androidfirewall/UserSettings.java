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
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.Toast;

public class UserSettings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.user_settings);
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

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		boolean ipv6 = sharedPreferences.getBoolean("ipv6enabled", false);
		if (key.equals("ipv6enabled")) {
			if (ipv6) {
				toggleIPv6enabled();
			} else {
				purgeIp6Rules();
			}
		}
		if (key.equals("logenabled")) {
			toggleLogenabled();
		}
		if (key.equals("sdcard")) {
			sdcardSupport();
		}
		if (key.equals("vpnsupport")) {
			toggleVPNenabled();
		}
		if (key.equals("roamingsupport")) {
			toggleRoamenabled();
		}
		if (key.equals("notifyenabled")) {
			toggleNotifyenabled();
		}
		if (key.equals("taskertoastenabled")) {
			toggleTaskerNotifyenabled();
		}
		if (key.equals("locale")) {
			Api.applications = null;
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
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
					Toast.makeText(getApplicationContext(),
							R.string.rules_deleted, Toast.LENGTH_SHORT).show();
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}

	/**
	 * Toggle log on/off
	 */
	private void toggleLogenabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean enabled = !prefs.getBoolean(Api.PREF_LOGENABLED, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_LOGENABLED, enabled);
		editor.commit();
		if (Api.isEnabled(this)) {
			Api.applySavedIptablesRules(this, true);
		}
	}

	/**
	 * Toggle ipv6 on/off
	 */
	private void toggleIPv6enabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean enabled = !prefs.getBoolean(Api.PREF_IP6TABLES, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_IP6TABLES, enabled);
		editor.commit();
		if (Api.isEnabled(this)) {
			Api.applySavedIptablesRules(this, true);
		}
	}

	/**
	 * Toggle VPN support on/off
	 */
	private void toggleVPNenabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		boolean vpnenabled = !prefs.getBoolean(Api.PREF_VPNENABLED, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_VPNENABLED, vpnenabled);
		editor.commit();
	}

	/**
	 * Toggle Roaming support on/off
	 */
	private void toggleRoamenabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		boolean roamenabled = !prefs.getBoolean(Api.PREF_ROAMENABLED, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_ROAMENABLED, roamenabled);
		editor.commit();
	}

	/**
	 * Toggle Notification support on/off
	 */
	private void toggleNotifyenabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean enabled = !prefs.getBoolean(Api.PREF_NOTIFY, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_NOTIFY, enabled);
		editor.commit();
	}

	/**
	 * Toggle apps on SDCard support on/off
	 */
	private void sdcardSupport() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean enabled = !prefs.getBoolean(Api.PREF_SDCARD, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_SDCARD, enabled);
		editor.commit();
	}

	/**
	 * Toggle Tasker Notification support on/off
	 */
	private void toggleTaskerNotifyenabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean enabled = !prefs.getBoolean(Api.PREF_TASKERNOTIFY, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_TASKERNOTIFY, enabled);
		editor.commit();
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