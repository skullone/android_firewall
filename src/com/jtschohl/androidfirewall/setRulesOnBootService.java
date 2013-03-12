/**
 * Broadcast receiver that sets iptables rules on system startup.
 * This is necessary because the iptables rules are not persistent.
 * 
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

 * @author Jason Tschohl
 * @version 1.0
 */
package com.jtschohl.androidfirewall;

import com.jtschohl.androidfirewall.Api;

import android.app.Service;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.content.Intent;

public class setRulesOnBootService extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		new Thread() {
			public void run() {
				Looper.prepare();
				final boolean enabled = Api.isEnabled(getApplicationContext());
				if (enabled) {
					Log.d("Android Firewall", "Applying rules.");
					if (Api.hasRootAccess(getApplicationContext(), true)
							&& Api.applyIptablesRules(getApplicationContext(),
									true)) {
						Log.d("Android Firewall",
								"Enabled - Firewall successfully enabled on boot.");
					}
				} else {
					Log.d("Android Firewall", "Failed - Disabling firewall.");
					Api.setEnabled(getApplicationContext(), false);
				}
			}
		}.start();
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				stopSelf();
			}
		}, 180000);
	};

}