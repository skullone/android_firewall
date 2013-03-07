/**
 * Broadcast receiver responsible for removing rules that affect uninstalled apps.
 * 
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
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
 * @author Rodrigo Zechin Rosauro
 * @author Jason Tschohl
 * @version 1.0
 */
package com.jtschohl.androidfirewall;

import java.util.Date;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

/**
 * Broadcast receiver responsible for removing rules that affect uninstalled
 * apps.
 */
public class PackageBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			// Ignore application updates
			final boolean replacing = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);
			if (!replacing) {
				// Update the Firewall if necessary
				final int uid = intent.getIntExtra(Intent.EXTRA_UID, -123);
				Api.applicationRemoved(context, uid);
				// Force app list reload next time
				Api.applications = null;
			}
		} else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			final boolean appExists = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);

			if (appExists) {
				// do nothing
			} else {
				// Force app list reload next time
				Api.applications = null;
				// check to see if Notifications are enabled
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				boolean NotifyEnabled = prefs.getBoolean("notifyenabled", false);
				if (NotifyEnabled) {
					String new_app_installed = intent.getData()
							.getSchemeSpecificPart();
					if (PackageManager.PERMISSION_GRANTED == context
							.getPackageManager().checkPermission(
									Manifest.permission.INTERNET,
									new_app_installed)) {
						// notify the User that a new app has been installed
						notifyUserOfAppInstall(context, new_app_installed);
					}
				}
			}
		}
	}

	/**
	 * Send notification to the notification bar
	 * 
	 */
	@SuppressWarnings("deprecation")
	public void notifyUserOfAppInstall(Context context, String new_app_installed) {

		final int notifyMsg = 2187;
		int icon = R.drawable.notify_icon;
		long time_stamp = new Date().getTime();
		String notifyService = Context.NOTIFICATION_SERVICE;
		Intent intent = new Intent(context, MainActivity.class);
		CharSequence notifyname = "Open Android Firewall";

		NotificationManager ManageNotification = (NotificationManager) context
				.getSystemService(notifyService);
		Notification notification = new Notification(icon, notifyname,
				time_stamp);
		notification.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_SHOW_LIGHTS;

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		notification.setLatestEventInfo(context, notifyname,
				context.getString(R.string.new_app_installed), contentIntent);
		ManageNotification.notify(notifyMsg, notification);

	}

}
