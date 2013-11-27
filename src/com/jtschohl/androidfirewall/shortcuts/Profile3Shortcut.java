/**
 * Shortcut for Enabling Profile 3 via MacroDroid or Llama
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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.jtschohl.androidfirewall.R;

public class Profile3Shortcut extends Activity {
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String Profile3 = prefs.getString("profile3",
				getString(R.string.profile3));
		
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(this, Profile3.class.getName());
		ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
		
		Intent intent2 = new Intent();
		intent2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		intent2.putExtra(Intent.EXTRA_SHORTCUT_NAME, Profile3);
		intent2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		setResult(RESULT_OK, intent2);
		finish();
	}
}