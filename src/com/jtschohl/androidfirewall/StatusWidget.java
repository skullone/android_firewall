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

 * @author Jason Tschohl
 * @version 1.0
 */

package com.jtschohl.androidfirewall;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class StatusWidget extends AppWidgetProvider {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);
		
		if (Api.STATUS_CHANGED_MSG.equals(intent.getAction())) {
			// Broadcast sent when the firewall status has changed
			final Bundle extras = intent.getExtras();
			if (extras != null && extras.containsKey(Api.STATUS_EXTRA)) {
				final boolean firewallEnabled = extras
						.getBoolean(Api.STATUS_EXTRA);
				final AppWidgetManager manager = AppWidgetManager
						.getInstance(context);
				final int[] widgetIds = manager
						.getAppWidgetIds(new ComponentName(context,
								StatusWidget.class));
				showWidget(context, manager, widgetIds, firewallEnabled);
			}
		} else if (Api.TOGGLE_REQUEST_MSG.equals(intent.getAction())) {
			// Broadcast sent to request toggling firewall status
			final SharedPreferences prefs = context.getSharedPreferences(
					Api.PREFS_NAME, 0);
			final boolean enabled = !prefs.getBoolean(Api.PREF_ENABLED, true);
			//final String pwd = prefs.getString(Api.PREF_PASSWORD, "");
			SharedPreferences prefs2 = PreferenceManager
					.getDefaultSharedPreferences(context);
			final String pwd = prefs2.getString("password", "");
			if (!enabled && pwd.length() != 0) {
				Toast.makeText(context, R.string.widget_fail,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] ints) {
		super.onUpdate(context, appWidgetManager, ints);

		final SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME,  0);
		boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, true);
		showWidget(context, appWidgetManager, ints, enabled);
	}

	private void showWidget(Context context, AppWidgetManager manager,
			int[] widgetIds, boolean enabled) {
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.onoff_widget);
		final int iconId = enabled ? R.drawable.widget_on
				: R.drawable.widget_off;
		views.setImageViewResource(R.id.widgetCanvas, iconId);
		Intent intent = new Intent(context, WidgetActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.widgetCanvas, pendingIntent);
		manager.updateAppWidget(widgetIds, views);
	}
}
