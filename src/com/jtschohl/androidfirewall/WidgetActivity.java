/**
 * Toggle Widget implementation
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

import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class WidgetActivity extends Activity implements OnClickListener {

	final static String TAG = "{AF}";
	private Button enableFirewall;
	private Button disableFirewall;
	private Button defaultprofile;
	private Button profile1;
	private Button profile2;
	private Button profile3;
	private Button profile4;
	private Button profile5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_menu);

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

		enableFirewall = (Button) this.findViewById(R.id.enableFirewall);
		enableFirewall.setOnClickListener(this);
		disableFirewall = (Button) this.findViewById(R.id.disableFirewall);
		disableFirewall.setOnClickListener(this);
		defaultprofile = (Button) this.findViewById(R.id.DefaultProfile);
		defaultprofile.setText(defaultProfile);
		defaultprofile.setOnClickListener(this);
		profile1 = (Button) this.findViewById(R.id.Profile1);
		profile1.setText(Profile1);
		profile1.setOnClickListener(this);
		profile2 = (Button) this.findViewById(R.id.Profile2);
		profile2.setText(Profile2);
		profile2.setOnClickListener(this);
		profile3 = (Button) this.findViewById(R.id.Profile3);
		profile3.setText(Profile3);
		profile3.setOnClickListener(this);
		profile4 = (Button) this.findViewById(R.id.Profile4);
		profile4.setText(Profile4);
		profile4.setOnClickListener(this);
		profile5 = (Button) this.findViewById(R.id.Profile5);
		profile5.setText(Profile5);
		profile5.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.enableFirewall) {
			if (Api.applySavedIptablesRules(getApplicationContext(), false)) {
				Toast.makeText(getApplicationContext(), R.string.toast_enabled,
						Toast.LENGTH_SHORT).show();
				Api.setEnabled(getApplicationContext(), true);
			} else {
				Toast.makeText(getApplicationContext(),
						R.string.toast_error_enabling, Toast.LENGTH_SHORT)
						.show();
			}
			finish();
		}
		if (v.getId() == R.id.disableFirewall) {
			final SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, 0);
			final boolean enabled = !prefs.getBoolean(Api.PREF_ENABLED, true);
			SharedPreferences prefs2 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			final String pwd = prefs2.getString("password", "");
			if (!enabled && pwd.length() != 0) {
				Toast.makeText(getApplicationContext(), R.string.widget_fail,
						Toast.LENGTH_SHORT).show();
			} else {
				if (Api.purgeIptables(getApplicationContext(), false)) {
					Toast.makeText(getApplicationContext(),
							R.string.toast_disabled, Toast.LENGTH_SHORT).show();
					Api.setEnabled(getApplicationContext(), false);
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.toast_error_disabling, Toast.LENGTH_SHORT)
							.show();
				}
			}
			finish();
		}
		if (v.getId() == R.id.DefaultProfile) {
			SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getApplicationContext()
					.getSharedPreferences(Api.PREF_PROFILE,
							Context.MODE_PRIVATE);
			SharedPreferences prefs3 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs3.edit();
			final Editor editRules = prefs.edit();
			editRules.clear();
			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			Api.applications = null;
			editor.putInt("itemPosition", 0);
			editor.commit();
			toggleUserSettings(getApplicationContext());
			boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, false);
			final String pwd = prefs3.getString("password", "");
			if (enabled) {
				Api.applyIptablesRules(getApplicationContext(), true);
				Api.setEnabled(getApplicationContext(), true);
			}
			if (!enabled) {
				if (pwd.length() != 0) {
					Toast.makeText(getApplicationContext(),
							R.string.widget_fail, Toast.LENGTH_SHORT).show();
				} else {
					Api.saveRules(getApplicationContext());
					Api.purgeIptables(getApplicationContext(), true);
					Api.setEnabled(getApplicationContext(), false);
				}
			}
			finish();
		}
		if (v.getId() == R.id.Profile1) {
			SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getApplicationContext()
					.getSharedPreferences(Api.PREF_PROFILE1,
							Context.MODE_PRIVATE);
			SharedPreferences prefs3 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs3.edit();
			final Editor editRules = prefs.edit();
			editRules.clear();
			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			Api.applications = null;
			editor.putInt("itemPosition", 1);
			editor.commit();
			toggleUserSettings(getApplicationContext());
			boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, false);
			final String pwd = prefs3.getString("password", "");
			if (enabled) {
				Api.applyIptablesRules(getApplicationContext(), true);
				Api.setEnabled(getApplicationContext(), true);
			}
			if (!enabled) {
				if (pwd.length() != 0) {
					Toast.makeText(getApplicationContext(),
							R.string.widget_fail, Toast.LENGTH_SHORT).show();
				} else {
					Api.saveRules(getApplicationContext());
					Api.purgeIptables(getApplicationContext(), true);
					Api.setEnabled(getApplicationContext(), false);
				}
			}
			finish();
		}
		if (v.getId() == R.id.Profile2) {
			SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getApplicationContext()
					.getSharedPreferences(Api.PREF_PROFILE2,
							Context.MODE_PRIVATE);
			SharedPreferences prefs3 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs3.edit();
			final Editor editRules = prefs.edit();
			editRules.clear();
			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			Api.applications = null;
			editor.putInt("itemPosition", 2);
			editor.commit();
			toggleUserSettings(getApplicationContext());
			boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, false);
			final String pwd = prefs3.getString("password", "");
			if (enabled) {
				Api.applyIptablesRules(getApplicationContext(), true);
				Api.setEnabled(getApplicationContext(), true);
			}
			if (!enabled) {
				if (pwd.length() != 0) {
					Toast.makeText(getApplicationContext(),
							R.string.widget_fail, Toast.LENGTH_SHORT).show();
				} else {
					Api.saveRules(getApplicationContext());
					Api.purgeIptables(getApplicationContext(), true);
					Api.setEnabled(getApplicationContext(), false);
				}
			}
			finish();
		}
		if (v.getId() == R.id.Profile3) {
			SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getApplicationContext()
					.getSharedPreferences(Api.PREF_PROFILE3,
							Context.MODE_PRIVATE);
			SharedPreferences prefs3 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs3.edit();
			final Editor editRules = prefs.edit();
			editRules.clear();
			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			Api.applications = null;
			editor.putInt("itemPosition", 3);
			editor.commit();
			toggleUserSettings(getApplicationContext());
			boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, false);
			final String pwd = prefs3.getString("password", "");
			if (enabled) {
				Api.applyIptablesRules(getApplicationContext(), true);
				Api.setEnabled(getApplicationContext(), true);
			}
			if (!enabled) {
				if (pwd.length() != 0) {
					Toast.makeText(getApplicationContext(),
							R.string.widget_fail, Toast.LENGTH_SHORT).show();
				} else {
					Api.saveRules(getApplicationContext());
					Api.purgeIptables(getApplicationContext(), true);
					Api.setEnabled(getApplicationContext(), false);
				}
			}
			finish();
		}
		if (v.getId() == R.id.Profile4) {
			SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getApplicationContext()
					.getSharedPreferences(Api.PREF_PROFILE4,
							Context.MODE_PRIVATE);
			SharedPreferences prefs3 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs3.edit();
			final Editor editRules = prefs.edit();
			editRules.clear();
			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			Api.applications = null;
			editor.putInt("itemPosition", 4);
			editor.commit();
			toggleUserSettings(getApplicationContext());
			boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, false);
			final String pwd = prefs3.getString("password", "");
			if (enabled) {
				Api.applyIptablesRules(getApplicationContext(), true);
				Api.setEnabled(getApplicationContext(), true);
			}
			if (!enabled) {
				if (pwd.length() != 0) {
					Toast.makeText(getApplicationContext(),
							R.string.widget_fail, Toast.LENGTH_SHORT).show();
				} else {
					Api.saveRules(getApplicationContext());
					Api.purgeIptables(getApplicationContext(), true);
					Api.setEnabled(getApplicationContext(), false);
				}
			}
			finish();
		}
		if (v.getId() == R.id.Profile5) {
			SharedPreferences prefs = getApplicationContext()
					.getSharedPreferences(Api.PREFS_NAME, Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getApplicationContext()
					.getSharedPreferences(Api.PREF_PROFILE5,
							Context.MODE_PRIVATE);
			SharedPreferences prefs3 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs3.edit();
			final Editor editRules = prefs.edit();
			editRules.clear();
			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			Api.applications = null;
			editor.putInt("itemPosition", 5);
			editor.commit();
			toggleUserSettings(getApplicationContext());
			boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, false);
			final String pwd = prefs3.getString("password", "");
			if (enabled) {
				Api.applyIptablesRules(getApplicationContext(), true);
				Api.setEnabled(getApplicationContext(), true);
			}
			if (!enabled) {
				if (pwd.length() != 0) {
					Toast.makeText(getApplicationContext(),
							R.string.widget_fail, Toast.LENGTH_SHORT).show();
				} else {
					Api.saveRules(getApplicationContext());
					Api.purgeIptables(getApplicationContext(), true);
					Api.setEnabled(getApplicationContext(), false);
				}
			}
			finish();
		}
	}

	private void toggleUserSettings(Context ctx) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		boolean ipv6support = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_IP6TABLES, false);
		boolean logsupport = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LOGENABLED, false);
		boolean logacceptenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LOGACCEPTENABLED, false);
		boolean notifysupport = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_NOTIFY, false);
		boolean taskerenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TASKERNOTIFY, false);
	/*	boolean sdcard = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_SDCARD, false); */
		boolean vpnenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_VPNENABLED, false);
		boolean roamenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_ROAMENABLED, false);
		boolean lanenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LANENABLED, false);
		boolean autorules = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_AUTORULES, false);
		boolean tetherenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TETHER, false);
		boolean multiuserenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_MULTIUSER, false);
		boolean inputenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_INPUTENABLED, false);
		if (ipv6support) {
			editor.putBoolean("ipv6enabled", true);
			editor.commit();
		} else {
			editor.putBoolean("ipv6enabled", false);
			editor.commit();
		}
		if (logsupport) {
			editor.putBoolean("logenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("logenabled", false);
			editor.commit();
		}
		if (notifysupport) {
			editor.putBoolean("notifyenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("notifyenabled", false);
			editor.commit();
		}
		if (taskerenabled) {
			editor.putBoolean("taskertoastenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("taskertoastenabled", false);
			editor.commit();
		}
	/*	if (sdcard) {
			editor.putBoolean("sdcard", true);
			editor.commit();
		} else {
			editor.putBoolean("sdcard", false);
			editor.commit();
		} */
		if (vpnenabled) {
			editor.putBoolean("vpnsupport", true);
			editor.commit();
		} else {
			editor.putBoolean("vpnsupport", false);
			editor.commit();
		}
		if (roamenabled) {
			editor.putBoolean("roamingsupport", true);
			editor.commit();
		} else {
			editor.putBoolean("roamingsupport", false);
			editor.commit();
		}
		if (lanenabled) {
			editor.putBoolean("lansupport", true);
			editor.commit();
		} else {
			editor.putBoolean("lansupport", false);
			editor.commit();
		}
		if (autorules) {
			editor.putBoolean("connectchangerules", true);
			editor.commit();
		} else {
			editor.putBoolean("connectchangerules", false);
			editor.commit();
		}
		if (tetherenabled) {
			editor.putBoolean("tetheringsupport", true);
			editor.commit();
		} else {
			editor.putBoolean("tetheringsupport", false);
			editor.commit();
		}
		if (multiuserenabled) {
			editor.putBoolean("multiuser", true);
			editor.commit();
		} else {
			editor.putBoolean("multiuser", false);
			editor.commit();
		}
		if (inputenabled) {
			editor.putBoolean("inputenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("inputenabled", false);
			editor.commit();
		}	
		if (logacceptenabled) {
			editor.putBoolean("logacceptenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("logacceptenabled", false);
			editor.commit();
		}	
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}

}
