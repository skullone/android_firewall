/**
 * This contains parts of the Tasker/Locale Plugin
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

package com.jtschohl.androidfirewall.plugin;

import com.jtschohl.androidfirewall.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EditActivity extends Activity implements OnClickListener {

	private Button defaultprofile;
	private Button profile1;
	private Button profile2;
	private Button profile3;
	private Button profile4;
	private Button profile5;
	int i = -1;
	String profile;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		BundleScrubber.scrub(getIntent());
		BundleScrubber.scrub(getIntent().getBundleExtra(
				com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));

		setContentView(R.layout.tasker_profile_buttons);

		this.defaultprofile = (Button) this.findViewById(R.id.defaultprofile);
		this.defaultprofile.setOnClickListener(this);
		this.profile1 = (Button) this.findViewById(R.id.profile1);
		this.profile1.setOnClickListener(this);
		this.profile2 = (Button) this.findViewById(R.id.profile2);
		this.profile2.setOnClickListener(this);
		this.profile3 = (Button) this.findViewById(R.id.profile3);
		this.profile3.setOnClickListener(this);
		this.profile4 = (Button) this.findViewById(R.id.profile4);
		this.profile4.setOnClickListener(this);
		this.profile5 = (Button) this.findViewById(R.id.profile5);
		this.profile5.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.defaultprofile) {
			i = 0;
			profile = "Default Profile";
		}
		if (v.getId() == R.id.profile1) {
			i = 1;
			profile = "Profile 1";
		}
		if (v.getId() == R.id.profile2) {
			i = 2;
			profile = "Profile 2";
		}
		if (v.getId() == R.id.profile3) {
			i = 3;
			profile = "Profile 3";
		}
		if (v.getId() == R.id.profile4) {
			i = 4;
			profile = "Profile 4";
		}
		if (v.getId() == R.id.profile5) {
			i = 5;
			profile = "Profile 5";
		}
		Log.d(getClass().getName(), "value for EditActivity = " + i);
		finish();
	}

	public void finish() {
		Intent intent = new Intent();
		intent.putExtra("storeposition", i);
		intent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
				profile);
		setResult(RESULT_OK, intent);
		super.finish();
	}
}