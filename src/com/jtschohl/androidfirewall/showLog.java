/**
 * Dialog displayed when the "Show Log" menu option is selected
 * 
 * Copyright (C) 2012-2014	Jason Tschohl
 *
 * This program is free sftware: you can redistribute it and/or modify
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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class showLog extends Activity {

	protected String dataText;

	protected void setData(String data) {
		this.dataText = data;
		TextView text = (TextView) findViewById(R.id.showlogs);
		text.setText(data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.logs_layout);

		final String logtarget = getApplicationContext().getSharedPreferences(
				Api.PREFS_NAME, 0).getString(Api.PREF_LOGTARGET, "");
		if (logtarget.equals("LOG")) {
			String logs = Api.showLog(getApplicationContext());
			TextView text = (TextView) findViewById(R.id.showlogs);
			Log.d(logs, "debugaf");
			text.setText(logs);
		}
		if (logtarget.equals("NFLOG")){
			setData("");
	        populateData(this);
		}
	}

	protected void parseAndSet(Context ctx, String raw){
		String logstring = Api.parseLog(ctx, raw);
		if (logstring == null){
			setData(getString(R.string.log_parse_error));
		} else {
			setData(logstring);
		}
	}
	
	protected void populateData(final Context ctx) {
			parseAndSet(ctx, NflogService.fetchLogs());
			return;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}