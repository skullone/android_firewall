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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SaveSettingsToProfile extends ListActivity {
	private File filepath = new File(Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/androidfirewall/");
	private static final String filetype = ".rules";

	/**
	 * opens the ListView of the saved rules files and allows users to select
	 * which one to assign to the profile.
	 */
	public void onCreate(Bundle ruleslist) {
		super.onCreate(ruleslist);
		String[] rulesfiles;

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				File sel = new File(dir, filename);
				return filename.contains(filetype) || sel.isDirectory();
			}
		};
		rulesfiles = filepath.list(filter);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.rules_dialog, R.id.label, rulesfiles);
		setListAdapter(adapter);
	}

	private void resultOk() {
		final Intent response = new Intent(Api.PREF_PROFILES);
		setResult(RESULT_OK, response);
		Toast.makeText(this, R.string.profile_created,
				Toast.LENGTH_SHORT).show();
		finish();
	}

	/**
	 * Takes the ListItem click and matches it up with the profile chosen from
	 * previous screen
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void onListItemClick(ListView items, View v, int position, long id) {
		Intent intent = getIntent();
		int profileChoice = intent.getIntExtra("profileChoice", 0);

		if (profileChoice == 1) {
			final SharedPreferences prefs = getSharedPreferences(
					Api.PREF_PROFILE, Context.MODE_PRIVATE);
			File file = new File(filepath + "/"
					+ getListAdapter().getItem(position));
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				final Editor editRules = prefs.edit();
				editRules.clear();
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object rule = entry.getValue();
					String keys = entry.getKey();
					if (rule instanceof Boolean)
						editRules.putBoolean(keys,
								((Boolean) rule).booleanValue());
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
			} catch (IOException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.rules_file_missing,
						Toast.LENGTH_SHORT).show();
			} catch (ClassNotFoundException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.error_accessing_class,
						Toast.LENGTH_SHORT).show();
			} finally {

				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException errors) {
					errors.printStackTrace();
				}
			}
		}
		if (profileChoice == 2) {
			final SharedPreferences prefs = getSharedPreferences(
					Api.PREF_PROFILE1, Context.MODE_PRIVATE);
			File file = new File(filepath + "/"
					+ getListAdapter().getItem(position));
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				final Editor editRules = prefs.edit();
				editRules.clear();
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object rule = entry.getValue();
					String keys = entry.getKey();
					if (rule instanceof Boolean)
						editRules.putBoolean(keys,
								((Boolean) rule).booleanValue());
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
			} catch (IOException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.rules_file_missing,
						Toast.LENGTH_SHORT).show();
			} catch (ClassNotFoundException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.error_accessing_class,
						Toast.LENGTH_SHORT).show();
			} finally {

				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException errors) {
					errors.printStackTrace();
				}
			}
		}
		if (profileChoice == 3) {
			final SharedPreferences prefs = getSharedPreferences(
					Api.PREF_PROFILE2, Context.MODE_PRIVATE);
			File file = new File(filepath + "/"
					+ getListAdapter().getItem(position));
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				final Editor editRules = prefs.edit();
				editRules.clear();
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object rule = entry.getValue();
					String keys = entry.getKey();
					if (rule instanceof Boolean)
						editRules.putBoolean(keys,
								((Boolean) rule).booleanValue());
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
			} catch (IOException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.rules_file_missing,
						Toast.LENGTH_SHORT).show();
			} catch (ClassNotFoundException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.error_accessing_class,
						Toast.LENGTH_SHORT).show();
			} finally {

				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException errors) {
					errors.printStackTrace();
				}
			}
		}
		if (profileChoice == 4) {
			final SharedPreferences prefs = getSharedPreferences(
					Api.PREF_PROFILE3, Context.MODE_PRIVATE);
			File file = new File(filepath + "/"
					+ getListAdapter().getItem(position));
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				final Editor editRules = prefs.edit();
				editRules.clear();
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object rule = entry.getValue();
					String keys = entry.getKey();
					if (rule instanceof Boolean)
						editRules.putBoolean(keys,
								((Boolean) rule).booleanValue());
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
			} catch (IOException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.rules_file_missing,
						Toast.LENGTH_SHORT).show();
			} catch (ClassNotFoundException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.error_accessing_class,
						Toast.LENGTH_SHORT).show();
			} finally {

				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException errors) {
					errors.printStackTrace();
				}
			}
		}
		if (profileChoice == 5) {
			final SharedPreferences prefs = getSharedPreferences(
					Api.PREF_PROFILE4, Context.MODE_PRIVATE);
			File file = new File(filepath + "/"
					+ getListAdapter().getItem(position));
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				final Editor editRules = prefs.edit();
				editRules.clear();
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object rule = entry.getValue();
					String keys = entry.getKey();
					if (rule instanceof Boolean)
						editRules.putBoolean(keys,
								((Boolean) rule).booleanValue());
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
			} catch (IOException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.rules_file_missing,
						Toast.LENGTH_SHORT).show();
			} catch (ClassNotFoundException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.error_accessing_class,
						Toast.LENGTH_SHORT).show();
			} finally {

				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException errors) {
					errors.printStackTrace();
				}
			}
		}
		if (profileChoice == 6) {
			final SharedPreferences prefs = getSharedPreferences(
					Api.PREF_PROFILE5, Context.MODE_PRIVATE);
			File file = new File(filepath + "/"
					+ getListAdapter().getItem(position));
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				final Editor editRules = prefs.edit();
				editRules.clear();
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object rule = entry.getValue();
					String keys = entry.getKey();
					if (rule instanceof Boolean)
						editRules.putBoolean(keys,
								((Boolean) rule).booleanValue());
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
			} catch (IOException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.rules_file_missing,
						Toast.LENGTH_SHORT).show();
			} catch (ClassNotFoundException error) {
				error.printStackTrace();
				Toast.makeText(this, R.string.error_accessing_class,
						Toast.LENGTH_SHORT).show();
			} finally {

				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException errors) {
					errors.printStackTrace();
				}
			}
		}
	}
}