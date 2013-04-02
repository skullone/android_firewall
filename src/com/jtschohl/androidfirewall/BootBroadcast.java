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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

/**
 * Broadcast receiver that set iptables rules on system startup. This is
 * necessary because the rules are not persistent.
 */
public class BootBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
			String mountState = Environment.getExternalStorageState();
			int tries = 15;
			do {
				if (!mountState.equals(Environment.MEDIA_MOUNTED)){
					Log.i("Android Firewall", "SDCard not yet ready. Waiting 2 seconds");
					try{
						Thread.sleep(2000); //sleep for one second
					} catch (InterruptedException e){
						Log.w("Android Firewall", "Interrupted!" + e);
						break;
					}
					mountState = Environment.getExternalStorageState();
				} else {
					Log.i("Android Firewall", "SDCard is ready");
					Intent pushIntent = new Intent(context, setRulesOnBootService.class);
					context.startService(pushIntent);
					break;
				}
			} while (--tries > 0);
			if (tries == 0){
				//give up
				Log.d("Android Firewall", "Tries at 0");
			}
		//	Intent pushIntent = new Intent(context, setRulesOnBootService.class);
			//context.startService(pushIntent);
		}
	}

}
