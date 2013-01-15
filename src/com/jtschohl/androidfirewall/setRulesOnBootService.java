/**
 * Broadcast receiver that set iptable rules on system startup.
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

import android.app.IntentService;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

/**
 * This service needed to properly apply the IPTABLES/IP6TABLES rules upon
 * reboot. Some devices were having problems applying the rules on boot using
 * the old method. Judging by data given to me by my testers some devices were
 * simply taking too long to boot up and the old method was more or less being
 * skipped particularly on devices with large amounts of rules.
 * 
 * By making the Boot up use a service it allows longer running background
 * operations which hopefully should fix this issue.
 * 
 * If you want to learn more on Intents used for a Service, and many other
 * fantastic tutorials for Android development, visit this excellent writeup:
 * 
 * http://www.vogella.com/articles/AndroidServices/article.html
 * 
 * I've visited them a lot during my time with improving AF.
 * 
 */

public class setRulesOnBootService extends IntentService {
	public static final String BOOTUP_COMPLETED = "bootup_completed";
	public static Context context;

	// enables the firewall rules
	protected void BootUpComplete() {
		if (context == null)
			return;
		if (Api.isEnabled(context.getApplicationContext())) {
			if (!Api.applySavedIptablesRules(context.getApplicationContext(),
					false)) {
				Toast.makeText(this, "Unable to apply the firewall rules",
						Toast.LENGTH_SHORT).show();
				Api.setEnabled(context.getApplicationContext(), false);
			}
		}
	}

	// grabs the current Context and passes it back to the function for use
	public Context getContext() {
		return context;
	}

	// called from BootBroadcast
	public static void performAction(Context context, String bootupaction) {
		performAction(context, bootupaction, null);
	}

	// called from the previous performAction in order to be able to pass the
	// correct
	// variables around due to the use of Intents
	public static void performAction(Context context, String bootupaction,
			Bundle extras) {
		if ((context == null) || (bootupaction == null)
				|| bootupaction.equals(""))
			return;

		Intent bootservice = new Intent(context, setRulesOnBootService.class);
		bootservice.setAction(bootupaction);
		setContext(context.getApplicationContext());
		if (extras != null)
			bootservice.putExtras(extras);
		context.startService(bootservice);
	}

	// sets the previously returned Context to a variable for the class to pass.
	public static void setContext(Context context) {
		setRulesOnBootService.context = context;
	}

	// meh
	public setRulesOnBootService() {
		super("setRulesOnBootService");
	}

	// will be called asynchronously
	@Override
	protected void onHandleIntent(Intent intent) {
		String bootupaction = intent.getAction();

		if ((bootupaction == null) || (bootupaction.equals("")))
			return;

		if (bootupaction.equals(BOOTUP_COMPLETED)) {
			BootUpComplete();
		}
	}

}