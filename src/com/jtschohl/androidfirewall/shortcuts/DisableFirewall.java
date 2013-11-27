/**
 * Shortcut for Disabling the firewall via MacroDroid or Llama
 * 
 * Copyright (C) 2012-2013	Jason Tschohl
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

package com.jtschohl.androidfirewall.shortcuts;

import com.jtschohl.androidfirewall.Api;
import com.jtschohl.androidfirewall.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class DisableFirewall extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.arg1 != 0)
					Toast.makeText(getApplicationContext(), msg.arg1,
							Toast.LENGTH_SHORT).show();
			}
		};
		final Message msg = new Message();
		final SharedPreferences prefs2 = getApplicationContext()
				.getSharedPreferences(Api.PREFS_NAME, 0);
		final String oldPwd = prefs2.getString(Api.PREF_PASSWORD, "");
		final String newPwd = getApplicationContext().getSharedPreferences(
				Api.PREFS_NAME, 0).getString("validationPassword", "");
		if (oldPwd.length() == 0 && newPwd.length() == 0) {
			if (Api.purgeIptables(getApplicationContext(), false)) {

				msg.arg1 = R.string.toast_disabled;
				handler.sendMessage(msg);
				Api.setEnabled(getApplicationContext(), false);
			} else {
				msg.arg1 = R.string.toast_error_disabling;
				handler.sendMessage(msg);
			}
		} else {
			msg.arg1 = R.string.widget_fail;
			handler.sendMessage(msg);
		}
		finish();
	}
}