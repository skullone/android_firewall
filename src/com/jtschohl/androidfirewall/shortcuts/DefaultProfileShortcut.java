/**
 * Shortcut for Enabling the default profile via MacroDroid or Llama
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

public class DefaultProfileShortcut extends Activity {
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String defaultProfile = prefs.getString("default",
				getString(R.string.defaultprofile));
		
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(this, DefaultProfile.class.getName());
		ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
		
		Intent intent2 = new Intent();
		intent2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		intent2.putExtra(Intent.EXTRA_SHORTCUT_NAME, defaultProfile);
		intent2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		setResult(RESULT_OK, intent2);
		finish();
	}
}