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

import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jtschohl.androidfirewall.Api;
import com.jtschohl.androidfirewall.R;
import com.jtschohl.androidfirewall.plugin.BundleScrubber;

/**
 * Many thanks to the Tasker and Locale Development teams for great products and
 * good examples on how to create these plugins.
 * 
 * @author jason
 */

public final class FireReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		/*
		 * Always be strict on input parameters! A malicious third-party app
		 * could send a malformed Intent.
		 */

		if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent
				.getAction())) {
			return;
		}

		BundleScrubber.scrub(intent);
		BundleScrubber.scrub(intent
				.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));
		int i = intent.getIntExtra("storeposition", 0);
		Log.d(getClass().getName(), "value for FireReceiver = " + i);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		if (i == 0) {
			editor.putInt("itemPosition", 0);
			editor.commit();
			LoadDefaultProfile(context);
		}
		if (i == 1) {
			editor.putInt("itemPosition", 1);
			editor.commit();
			LoadProfile1(context);
		}
		if (i == 2) {
			editor.putInt("itemPosition", 2);
			editor.commit();
			LoadProfile2(context);
		}
		if (i == 3) {
			editor.putInt("itemPosition", 3);
			editor.commit();
			LoadProfile3(context);
		}
		if (i == 4) {
			editor.putInt("itemPosition", 4);
			editor.commit();
			LoadProfile4(context);
		}
		if (i == 5) {
			editor.putInt("itemPosition", 5);
			editor.commit();
			LoadProfile5(context);
		}
	}

	private void LoadDefaultProfile(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE, Context.MODE_PRIVATE);
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
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			Toast.makeText(context, R.string.tasker_profile, Toast.LENGTH_LONG)
					.show();
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			Toast.makeText(context, R.string.tasker_profile_disabled,
					Toast.LENGTH_LONG).show();
		}
	}

	private void LoadProfile1(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE1, Context.MODE_PRIVATE);
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
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			Toast.makeText(context, R.string.tasker_profile1, Toast.LENGTH_LONG)
					.show();
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			Toast.makeText(context, R.string.tasker_profile_disabled,
					Toast.LENGTH_LONG).show();
		}
	}

	private void LoadProfile2(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE2, Context.MODE_PRIVATE);
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
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			Toast.makeText(context, R.string.tasker_profile2, Toast.LENGTH_LONG)
					.show();
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			Toast.makeText(context, R.string.tasker_profile_disabled,
					Toast.LENGTH_LONG).show();
		}
	}

	private void LoadProfile3(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE3, Context.MODE_PRIVATE);
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
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			Toast.makeText(context, R.string.tasker_profile3, Toast.LENGTH_LONG)
					.show();
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			Toast.makeText(context, R.string.tasker_profile_disabled,
					Toast.LENGTH_LONG).show();
		}
	}

	private void LoadProfile4(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE4, Context.MODE_PRIVATE);
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
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			Toast.makeText(context, R.string.tasker_profile4, Toast.LENGTH_LONG)
					.show();
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			Toast.makeText(context, R.string.tasker_profile_disabled,
					Toast.LENGTH_LONG).show();
		}
	}

	private void LoadProfile5(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE5, Context.MODE_PRIVATE);
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
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			Toast.makeText(context, R.string.tasker_profile5, Toast.LENGTH_LONG)
					.show();
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			Toast.makeText(context, R.string.tasker_profile_disabled,
					Toast.LENGTH_LONG).show();
		}
	}
}