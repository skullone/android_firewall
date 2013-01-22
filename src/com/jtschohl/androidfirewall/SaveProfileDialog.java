/**
 * Dialog displayed to save a profile.
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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SaveProfileDialog extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = getLayoutInflater().inflate(
				R.layout.save_profile_buttons, null);
		((Button) view.findViewById(R.id.defaultprofile))
				.setOnClickListener(this);
		((Button) view.findViewById(R.id.profile1)).setOnClickListener(this);
		((Button) view.findViewById(R.id.profile2)).setOnClickListener(this);
		((Button) view.findViewById(R.id.profile3)).setOnClickListener(this);
		((Button) view.findViewById(R.id.profile4)).setOnClickListener(this);
		((Button) view.findViewById(R.id.profile5)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.defaultprofile) {
			resultOk();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}

	}

	/**
	 * Set the activity result to RESULT_OK and terminate this activity.
	 */
	private void resultOk() {
		final Intent response = new Intent(Api.PREF_PROFILE);
		setResult(RESULT_OK, response);
		finish();
	}

}