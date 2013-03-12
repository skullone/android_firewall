/**
 * Export Rules Dialog activity.
 * This screen is displayed to change the custom scripts.
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * This screen is displayed to show the Export Rules dialog.
 */
public class ExportRulesDialog extends Activity implements OnClickListener {
	private EditText user_input;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = getLayoutInflater().inflate(R.layout.export_rules,
				null);
		((Button) view.findViewById(R.id.exportrules_ok))
				.setOnClickListener(this);
		((Button) view.findViewById(R.id.exportrules_cancel))
				.setOnClickListener(this);
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		this.user_input = (EditText) view.findViewById(R.id.exportrules);
		this.user_input.setText(prefs.getString(Api.PREF_EXPORTNAME, ""));
		setTitle(R.string.enterfilename);
		setContentView(view);
	}

	/**
	 * Set the activity result to RESULT_OK and terminate this activity.
	 */
	private void resultOk() {
		final Intent response = new Intent(Api.PREF_REFRESH);
		response.putExtra(Api.EXPORT_EXTRA, user_input.getText().toString());
		setResult(RESULT_OK, response);
		finish();
	}

	// @Override
	public void onClick(View v) {
		if (v.getId() == R.id.exportrules_ok) {
			resultOk();
			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(user_input.getWindowToken(), 0);
		}
		if (v.getId() == R.id.exportrules_cancel) {
			setResult(RESULT_CANCELED);
			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(user_input.getWindowToken(), 0);
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
