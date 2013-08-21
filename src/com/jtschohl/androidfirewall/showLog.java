/**
 * Dialog displayed when the "Show Log" menu option is selected
 * 
 * Copyright (C) 2011-2013	Kevin Cernekee
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
 * @author Kevin Cernekee
 * 
 * @author Jason Tschohl - modified code to work in AF
 * 
 * @version 1.0
 */

/**
 * 
 * Many thanks to Kevin Cernekee and Ukanth for figuring out how to get NFLOG working.
 * All the code is due to them.  This includes RootShell and any code in API and
 * UserSettings to allow the new logs to function.
 * 
 */

package com.jtschohl.androidfirewall;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.jtschohl.androidfirewall.RootShell.RootCommand;

public class showLog extends SherlockActivity {

	protected String dataText;

	protected void setData(String data) {
		this.dataText = data;
		((EditText) findViewById(R.id.showlogs)).setText(data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		populateData(this);
		this.setContentView(R.layout.logs_layout);
		setTitle(getString(R.string.log_title));
		((EditText) findViewById(R.id.showlogs)).setKeyListener(null);
		setData("");

	}

	public void populateData(final Context ctx) {
		Api.fetchDmesg(
				ctx,
				new RootCommand().setLogging(true).setReopenShell(true)
						.setFailureToast(R.string.log_fetch_error)
						.setCallback(new RootCommand.Callback() {
							public void cbFunc(RootCommand state) {
								if (state.exitCode != 0) {
									setData(getString(R.string.log_fetch_error));
								} else {
									String data = Api.showLog(ctx,
											state.res.toString());
									if (data == null) {
										setData(getString(R.string.log_parse_error));
									} else {
										setData(data);
									}
								}
							}
						}));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
