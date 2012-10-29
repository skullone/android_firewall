/**
 * Broadcast receiver responsible for removing rules that affect uninstalled apps.
 * 
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
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
 * @version 1.0
 */
package com.jtschohl.androidfirewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver responsible for removing rules that affect uninstalled apps.
 */
public class PackageBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			// Ignore application updates
			final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			if (!replacing) {
				// Update the Firewall if necessary
				final int uid = intent.getIntExtra(Intent.EXTRA_UID, -123);
				Api.applicationRemoved(context, uid);
				// Force app list reload next time
				Api.applications = null;
			}
		} else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			// Force app list reload next time
			Api.applications = null;
		}
	}

}
