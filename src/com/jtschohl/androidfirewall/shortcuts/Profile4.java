/**
 * Shortcut for Enabling profile 4 via MacroDroid or Llama
 * 
 * Copyright (C) 2012-2013	Jason Tschohl
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

package com.jtschohl.androidfirewall.shortcuts;

import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.jtschohl.androidfirewall.Api;
import com.jtschohl.androidfirewall.R;

public class Profile4 extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs3 = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs3.edit();
		editor.putInt("itemPosition", 4);
		editor.commit();
		
		getApplicationContext();
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		getApplicationContext();
		final SharedPreferences prefs2 = getApplicationContext().getSharedPreferences(
				Api.PREF_PROFILE4, Context.MODE_PRIVATE);
		boolean toastenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TASKERNOTIFY, false);
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
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (toastenabled) {
				Toast.makeText(getApplicationContext(), R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(getApplicationContext());
			Api.purgeIptables(getApplicationContext(), true);
			if (toastenabled) {
				Toast.makeText(getApplicationContext(), R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
		}
		finish();
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
}