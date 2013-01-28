/**
 * Check for change to roaming broadcast receiver.
 * This is the screen displayed when you open the application
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckForChangeToRoaming extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

		ConnectivityManager connectmanager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] roaminfo = connectmanager.getAllNetworkInfo();
		for (NetworkInfo roam : roaminfo) {
			if (roaminfo != null) {
				if (roam.getType() == ConnectivityManager.TYPE_MOBILE)
					if (roam.isConnectedOrConnecting() && roam.isRoaming()) {
						Api.applyIptablesRules(context, false);
					}
			}
		}
	}

}