/**
 * Dialog displayed to delete rules files.
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
 * @author Rodrigo Zechin Rosauro
 * @author Jason Tschohl
 * @version 1.0
 */
package com.jtschohl.androidfirewall;

import java.io.File;
import java.io.FilenameFilter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeleteRulesDialog extends ListActivity {
	private File filepath = new File(Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/androidfirewall/");
	private static final String filetype = ".rules";

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
		final Intent response = new Intent(Api.PREF_REFRESH);
		setResult(RESULT_OK, response);
		finish();
	}

	protected void onListItemClick(ListView items, View v, int position, long id) {
		final File file = new File(filepath + "/"
				+ getListAdapter().getItem(position));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.confirm_delete + " " + file)
				.setCancelable(false)
				.setPositiveButton(R.string.delete_rules_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								file.delete();
								resultOk();
							}
						})
				.setNegativeButton(R.string.delete_rules_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}