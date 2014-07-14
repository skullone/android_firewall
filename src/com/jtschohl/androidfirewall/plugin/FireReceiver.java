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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jtschohl.androidfirewall.Api;
import com.jtschohl.androidfirewall.R;

/**
 * Many thanks to the Tasker and Locale Development teams for great products and
 * good examples on how to create these plugins.
 * 
 * @author jason
 */

public final class FireReceiver extends BroadcastReceiver {

	final static String TAG = "{AF}";
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		/*
		 * Always be strict on input parameters! A malicious third-party app
		 * could send a malformed Intent.
		 */

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.arg1 != 0)
					Toast.makeText(context, msg.arg1, Toast.LENGTH_SHORT)
							.show();
			}
		};
		final Message msg = new Message();

		if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent
				.getAction())) {
			return;
		}

		BundleScrubber.scrub(intent);
		BundleScrubber.scrub(intent
				.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));
		Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		String storeposition = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
		int i = Integer.parseInt(storeposition);
		Log.d(TAG, "value for FireReceiver = " + i);
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
		if (i == 6) {
			boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME,
					0).getBoolean(Api.PREF_TASKERNOTIFY, false);
			if (Api.applySavedIptablesRules(context, false)) {
				if (toastenabled) {
					msg.arg1 = R.string.toast_enabled;
					handler.sendMessage(msg);
				}
				Api.setEnabled(context, true);
			} else {
				if (toastenabled) {
					msg.arg1 = R.string.toast_error_enabling;
					handler.sendMessage(msg);
				}
			}
		}
		if (i == 7) {
			final SharedPreferences prefs2 = context.getSharedPreferences(
					Api.PREFS_NAME, 0);
			boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME,
					0).getBoolean(Api.PREF_TASKERNOTIFY, false);
			final String oldPwd = prefs2.getString(Api.PREF_PASSWORD, "");
			final String newPwd = context.getSharedPreferences(Api.PREFS_NAME,
					0).getString("validationPassword", "");
			if (oldPwd.length() == 0 && newPwd.length() == 0) {
				if (Api.purgeIptables(context, false)) {
					if (toastenabled) {
						msg.arg1 = R.string.toast_disabled;
						handler.sendMessage(msg);
					}
					Api.setEnabled(context, false);
				} else {
					if (toastenabled) {
						msg.arg1 = R.string.toast_error_disabling;
						handler.sendMessage(msg);
					}
				}
			} else {
				msg.arg1 = R.string.widget_fail;
				handler.sendMessage(msg);
			}
		}
	}

	private void LoadDefaultProfile(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE, Context.MODE_PRIVATE);
		boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME, 0)
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
		toggleUserSettings(context);
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void LoadProfile1(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE1, Context.MODE_PRIVATE);
		boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME, 0)
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
		toggleUserSettings(context);
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void LoadProfile2(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE2, Context.MODE_PRIVATE);
		boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME, 0)
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
		toggleUserSettings(context);
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void LoadProfile3(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE3, Context.MODE_PRIVATE);
		boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME, 0)
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
		toggleUserSettings(context);
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void LoadProfile4(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE4, Context.MODE_PRIVATE);
		boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME, 0)
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
		toggleUserSettings(context);
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void LoadProfile5(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = context.getSharedPreferences(
				Api.PREF_PROFILE5, Context.MODE_PRIVATE);
		boolean toastenabled = context.getSharedPreferences(Api.PREFS_NAME, 0)
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
		toggleUserSettings(context);
		if (Api.isEnabled(context)) {
			Api.applyIptablesRules(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Api.saveRules(context);
			Api.purgeIptables(context, true);
			if (toastenabled) {
				Toast.makeText(context, R.string.tasker_profile_disabled,
						Toast.LENGTH_LONG).show();
			}
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
		boolean colorenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_APPCOLOR, false);
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
		if (colorenabled) {
			editor.putBoolean("appcolor", true);
			editor.commit();
		} else {
			editor.putBoolean("appcolor", false);
			editor.commit();
		}
	}
}