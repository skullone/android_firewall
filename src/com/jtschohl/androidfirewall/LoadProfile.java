/**
 * Class that loads a saved profiles and applies the rules
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

import java.util.Map.Entry;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoadProfile extends Activity implements OnClickListener {

	private Button defaultprofile;
	private Button profile1;
	private Button profile2;
	private Button profile3;
	private Button profile4;
	private Button profile5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.load_profile_buttons);

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
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
					Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREF_PROFILE, Context.MODE_PRIVATE);
			final Editor editRules = prefs.edit();
			editRules.clear();

			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			resultOk();
			return;
		}
		if (v.getId() == R.id.profile1) {
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
					Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREF_PROFILE1, Context.MODE_PRIVATE);
			final Editor editRules = prefs.edit();
			editRules.clear();

			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			resultOk();
			return;
		}
		if (v.getId() == R.id.profile2) {
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
					Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREF_PROFILE2, Context.MODE_PRIVATE);
			final Editor editRules = prefs.edit();
			editRules.clear();

			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			resultOk();
			return;
		}
		if (v.getId() == R.id.profile3) {
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
					Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREF_PROFILE3, Context.MODE_PRIVATE);
			final Editor editRules = prefs.edit();
			editRules.clear();

			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			resultOk();
			return;
		}
		if (v.getId() == R.id.profile4) {
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
					Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREF_PROFILE4, Context.MODE_PRIVATE);
			final Editor editRules = prefs.edit();
			editRules.clear();

			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			resultOk();
			return;
		}
		if (v.getId() == R.id.profile5) {
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
					Context.MODE_PRIVATE);
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREF_PROFILE5, Context.MODE_PRIVATE);
			final Editor editRules = prefs.edit();

			editRules.clear();

			for (Entry<String, ?> entry : prefs2.getAll().entrySet()) {
				Object rule = entry.getValue();
				String keys = entry.getKey();
				if (rule instanceof Boolean)
					editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
				else if (rule instanceof Float)
					editRules.putFloat(keys, ((Float) rule).floatValue());
				else if (rule instanceof String)
					editRules.putString(keys, ((String) rule));
				else if (rule instanceof Long)
					editRules.putLong(keys, ((Long) rule).longValue());
				else if (rule instanceof Integer)
					editRules.putInt(keys, ((Integer) rule).intValue());
			}
			editRules.commit();
			resultOk();
			return;
		}
	}

	/**
	 * Set the activity result to RESULT_OK and terminate this activity.
	 */
	private void resultOk() {
		final Intent response = new Intent(Api.PREF_PROFILES);
		setResult(RESULT_OK, response);
		finish();
	}
}