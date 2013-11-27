/**
 * Keep track of wifi/3G/tethering status and LAN IP ranges.
 *
 * Copyright (C) 2013 Kevin Cernekee
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
 * @author Kevin Cernekee - Original Author
 * @author Jason Tschohl
 * 
 * @version 1.0
 */

/**
 * Many thanks to Kevin Cernekee for the open source code for the LAN support functionality.
 */

package com.jtschohl.androidfirewall;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InterfaceIntent extends IntentService {

	final static String TAG = "{AF}";
	public static final String ACTION_CONNECTIVITY_CHANGED = "connectivity_changed";

	private static Context ctx = null;

	public InterfaceIntent() {
		// If you forget this one, the app will crash
		super("InterfaceIntent");
	}

	public static void performAction(Context context, String action) {
		getappContext(context);
		Intent svc = new Intent(context, InterfaceIntent.class);
		svc.setAction(action);
		context.startService(svc);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (InterfaceTracker.checkForNewCfg(ctx)) {
			if (applyRules(ctx, false) == false) {
				Log.e(TAG, "Unable to apply firewall rules");
				Api.setEnabled(ctx, false);
			}
		}
	}

	protected static void getappContext(Context context) {
		if (ctx == null) {
			ctx = context.getApplicationContext();
		}
	}

	public static boolean applyRules(Context context, boolean showErrors) {
		boolean msg = false;
		if (ctx != null) {
			if (!Api.isEnabled(ctx)) {
				Log.d(TAG,
						"Cannot apply rules.  Firewall is disabled.");
				return true;
			}
			msg = Api.applySavedIptablesRules(ctx, showErrors);
			Log.d(TAG, "applyRules: "
					+ (msg ? "success" : "failed"));
		}
		return msg;
	}
}