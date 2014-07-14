/**
 * Applies rules after a boot up and provides the sdcard support.
 * This is necessary because the iptables rules are not persistent.
 * 
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
 * Coypright (C) 2012-2014	Jason Tschohl
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
 * @author Rodrigo Zechin Rosauro
 * @author Jason Tschohl
 * @version 1.0
 */
package com.jtschohl.androidfirewall;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast receiver that set iptables rules on system startup. This is
 * necessary because the rules are not persistent.
 */
public class ApplyRulesOnBoot {

	final static String TAG = "{AF}";

	public static void applyRules(Context context) {
		final Context ctx = context.getApplicationContext();
		final boolean enabled = Api.isEnabled(ctx.getApplicationContext());
		String nflog = Api.PREF_LOGTARGET;
		boolean logenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LOGENABLED, false);
		if (enabled) {
			Log.d(TAG, "Applying rules during boot.");
			if (Api.applySavedIptablesRules(context, false)) {
				if ("NFLOG".equals(nflog)) {
					if (logenabled){
						Intent intent = new Intent(ctx.getApplicationContext(),
								NflogService.class);
						ctx.getApplicationContext().startService(intent);
						Intent intent2 = new Intent(ctx.getApplicationContext(),
								RootShell.class);
						ctx.getApplicationContext().startService(intent2);
					}
					Log.d(TAG, "NFLOG in use starting service after reboot.");
				}
				Log.d(TAG, "Enabled - Firewall successfully enabled on boot.");
			}
		} else {
			Log.d(TAG, "Failed - Disabling firewall during boot.");
			Api.setEnabled(ctx.getApplicationContext(), false);
		}
	}

}
