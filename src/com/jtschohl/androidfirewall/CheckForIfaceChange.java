/**
 * Check for change to interface change broadcast receiver.
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

public class CheckForIfaceChange extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		// This gets called during wifi/data/lan changes if Auto Firewall Rules
		// are enabled
		boolean autorules = context.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_AUTORULES, false);
		if (autorules) {
			InterfaceIntent.performAction(context,
					InterfaceIntent.ACTION_CONNECTIVITY_CHANGED);
		}
	}

}