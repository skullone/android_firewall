/**
 * Main application activity.
 * This is the screen displayed when you open the application
 * 
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
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
 * @author Rodrigo Zechin Rosauro
 * @version 1.0
 */

package com.jtschohl.androidfirewall;

import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jtschohl.androidfirewall.Api.DroidApp;

/**
 * Main application activity.
 * This is the screen displayed when you open the application
 */
public class MainActivity extends Activity implements OnCheckedChangeListener, OnClickListener {
	
	// Menu options
	private static final int MENU_DISABLE	= 0;
	private static final int MENU_TOGGLELOG	= 1;
	private static final int MENU_APPLY		= 2;
	private static final int MENU_EXIT		= 3;
	private static final int MENU_HELP		= 4;
	private static final int MENU_SHOWLOG	= 5;
	private static final int MENU_SHOWRULES	= 6;
	private static final int MENU_CLEARLOG	= 7;
	private static final int MENU_SETPWD	= 8;
	private static final int MENU_SETCUSTOM = 9;
	
	/** progress dialog instance */
	private ListView listview = null;
	/** indicates if the view has been modified and not yet saved */
	private boolean dirty = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		try {
	        /* enable hardware acceleration on Android >= 3.0 */
			final int FLAG_HARDWARE_ACCELERATED = WindowManager.LayoutParams.class.getDeclaredField("FLAG_HARDWARE_ACCELERATED").getInt(null);
			getWindow().setFlags(FLAG_HARDWARE_ACCELERATED, FLAG_HARDWARE_ACCELERATED);
		} catch (Exception e) {
		}
        checkPreferences();
		setContentView(R.layout.main);
		this.findViewById(R.id.label_mode).setOnClickListener(this);
		Api.assertBinaries(this, true);
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	if (this.listview == null) {
    		this.listview = (ListView) this.findViewById(R.id.listview);
    	}
    	refreshHeader();
		final String pwd = getSharedPreferences(Api.PREFS_NAME, 0).getString(Api.PREF_PASSWORD, "");
		if (pwd.length() == 0) {
			// No password lock
			showOrLoadApplications();
		} else {
			// Check the password
			requestPassword(pwd);
		}
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	this.listview.setAdapter(null);
    }
    /**
     * Check if the stored preferences are OK
     */
    private void checkPreferences() {
    	final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
    	final Editor editor = prefs.edit();
    	boolean changed = false;
    	if (prefs.getString(Api.PREF_MODE, "").length() == 0) {
    		editor.putString(Api.PREF_MODE, Api.MODE_WHITELIST);
    		changed = true;
    	}
    	/* delete the old preference names */
    	if (prefs.contains("AllowedUids")) {
    		editor.remove("AllowedUids");
    		changed = true;
    	}
    	if (prefs.contains("Interfaces")) {
    		editor.remove("Interfaces");
    		changed = true;
    	}
    	if (changed) editor.commit();
    }
    /**
     * Refresh informative header
     */
    private void refreshHeader() {
    	final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
    	final String mode = prefs.getString(Api.PREF_MODE, Api.MODE_WHITELIST);
		final TextView labelmode = (TextView) this.findViewById(R.id.label_mode);
    	final Resources res = getResources();
		int resid = (mode.equals(Api.MODE_WHITELIST) ? R.string.mode_whitelist : R.string.mode_blacklist);
		labelmode.setText(res.getString(R.string.mode_header, res.getString(resid)));
		resid = (Api.isEnabled(this) ? R.string.title_enabled : R.string.title_disabled);
		setTitle(res.getString(resid, Api.VERSION));
    }
    /**
     * Displays a dialog box to select the operation mode (black or white list)
     */
    private void selectMode() {
    	final Resources res = getResources();
    	new AlertDialog.Builder(this).setItems(new String[]{res.getString(R.string.mode_whitelist),res.getString(R.string.mode_blacklist)}, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				final String mode = (which==0 ? Api.MODE_WHITELIST : Api.MODE_BLACKLIST);
				final Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
				editor.putString(Api.PREF_MODE, mode);
				editor.commit();
				refreshHeader();
			}
    	}).setTitle("Select mode:")
    	.show();
    }
    /**
     * Set a new password lock
     * @param pwd new password (empty to remove the lock)
     */
	private void setPassword(String pwd) {
    	final Resources res = getResources();
		final Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
		editor.putString(Api.PREF_PASSWORD, pwd);
		String msg;
		if (editor.commit()) {
			if (pwd.length() > 0) {
				msg = res.getString(R.string.passdefined);
			} else {
				msg = res.getString(R.string.passremoved);
			}
		} else {
			msg = res.getString(R.string.passerror);
		}
		Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * Request the password lock before displayed the main screen.
	 */
	private void requestPassword(final String pwd) {
		new PassDialog(this, false, new android.os.Handler.Callback() {
			public boolean handleMessage(Message msg) {
				if (msg.obj == null) {
					MainActivity.this.finish();
					android.os.Process.killProcess(android.os.Process.myPid());
					return false;
				}
				if (!pwd.equals(msg.obj)) {
					requestPassword(pwd);
					return false;
				}
				// Password correct
				showOrLoadApplications();
				return false;
			}
		}).show();
	}
	/**
	 * Toggle iptables log enabled/disabled
	 */
	private void toggleLogEnabled() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean enabled = !prefs.getBoolean(Api.PREF_LOGENABLED, false);
		final Editor editor = prefs.edit();
		editor.putBoolean(Api.PREF_LOGENABLED, enabled);
		editor.commit();
		if (Api.isEnabled(this)) {
			Api.applySavedIptablesRules(this, true);
		}
		Toast.makeText(MainActivity.this, (enabled?R.string.log_was_enabled:R.string.log_was_disabled), Toast.LENGTH_SHORT).show();
	}
	/**
	 * If the applications are cached, just show them, otherwise load and show
	 */
	private void showOrLoadApplications() {
    	final Resources res = getResources();
    	if (Api.applications == null) {
    		// The applications are not cached.. so lets display the progress dialog
    		final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working), res.getString(R.string.reading_apps), true);
        	new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
        			Api.getApps(MainActivity.this);
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
        			try {progress.dismiss();} catch(Exception ex){}
        			showApplications();
				}
        	}.execute();
    	} else {
    		// the applications are cached, just show the list
        	showApplications();
    	}
	}
    /**
     * Show the list of applications
     */
    private void showApplications() {
    	this.dirty = false;
        final DroidApp[] apps = Api.getApps(this);
        // Sort applications - selected first, then alphabetically
        Arrays.sort(apps, new Comparator<DroidApp>() {
			@Override
			public int compare(DroidApp o1, DroidApp o2) {
				if (o1.firstseem != o2.firstseem) {
					return (o1.firstseem ? -1 : 1);
				}
				if ((o1.selected_wifi|o1.selected_3g) == (o2.selected_wifi|o2.selected_3g)) {
					return String.CASE_INSENSITIVE_ORDER.compare(o1.names[0], o2.names[0]);
				}
				if (o1.selected_wifi || o1.selected_3g) return -1;
				return 1;
			}
        });
        final LayoutInflater inflater = getLayoutInflater();
		final ListAdapter adapter = new ArrayAdapter<DroidApp>(this,R.layout.listitem,R.id.itemtext,apps) {
        	@Override
        	public View getView(final int position, View convertView, ViewGroup parent) {
       			ListEntry entry;
        		if (convertView == null) {
        			// Inflate a new view
        			convertView = inflater.inflate(R.layout.listitem, parent, false);
            		Log.d("Android Firewall", ">> inflate("+convertView+")");
       				entry = new ListEntry();
       				entry.box_wifi = (CheckBox) convertView.findViewById(R.id.itemcheck_wifi);
       				entry.box_3g = (CheckBox) convertView.findViewById(R.id.itemcheck_3g);
       				entry.text = (TextView) convertView.findViewById(R.id.itemtext);
       				entry.icon = (ImageView) convertView.findViewById(R.id.itemicon);
       				entry.box_wifi.setOnCheckedChangeListener(MainActivity.this);
       				entry.box_3g.setOnCheckedChangeListener(MainActivity.this);
       				convertView.setTag(entry);
        		} else {
        			// Convert an existing view
        			entry = (ListEntry) convertView.getTag();
        		}
        		final DroidApp app = apps[position];
        		entry.app = app;
        		entry.text.setText(app.toString());
        		entry.icon.setImageDrawable(app.cached_icon);
        		if (!app.icon_loaded && app.appinfo!=null) {
        			// this icon has not been loaded yet - load it on a separated thread
            		new LoadIconTask().execute(app, getPackageManager(), convertView);
        		}
        		final CheckBox box_wifi = entry.box_wifi;
        		box_wifi.setTag(app);
        		box_wifi.setChecked(app.selected_wifi);
        		final CheckBox box_3g = entry.box_3g;
        		box_3g.setTag(app);
        		box_3g.setChecked(app.selected_3g);
       			return convertView;
        	}
        };
        this.listview.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_DISABLE, 0, R.string.fw_enabled).setIcon(android.R.drawable.button_onoff_indicator_on);
    	menu.add(0, MENU_TOGGLELOG, 0, R.string.log_enabled).setIcon(android.R.drawable.button_onoff_indicator_on);
    	menu.add(0, MENU_APPLY, 0, R.string.applyrules).setIcon(R.drawable.apply);
    	menu.add(0, MENU_EXIT, 0, R.string.exit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	menu.add(0, MENU_HELP, 0, R.string.help).setIcon(android.R.drawable.ic_menu_help);
    	menu.add(0, MENU_SHOWLOG, 0, R.string.show_log).setIcon(R.drawable.show);
    	menu.add(0, MENU_SHOWRULES, 0, R.string.showrules).setIcon(R.drawable.show);
    	menu.add(0, MENU_CLEARLOG, 0, R.string.clear_log).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    	menu.add(0, MENU_SETPWD, 0, R.string.setpwd).setIcon(android.R.drawable.ic_lock_lock);
    	menu.add(0, MENU_SETCUSTOM, 0, R.string.set_custom_script);
    	
    	return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	final MenuItem item_onoff = menu.getItem(MENU_DISABLE);
    	final MenuItem item_apply = menu.getItem(MENU_APPLY);
    	final boolean enabled = Api.isEnabled(this);
    	if (enabled) {
    		item_onoff.setIcon(android.R.drawable.button_onoff_indicator_on);
    		item_onoff.setTitle(R.string.fw_enabled);
    		item_apply.setTitle(R.string.applyrules);
    	} else {
    		item_onoff.setIcon(android.R.drawable.button_onoff_indicator_off);
    		item_onoff.setTitle(R.string.fw_disabled);
    		item_apply.setTitle(R.string.saverules);
    	}
    	final MenuItem item_log = menu.getItem(MENU_TOGGLELOG);
    	final boolean logenabled = getSharedPreferences(Api.PREFS_NAME, 0).getBoolean(Api.PREF_LOGENABLED, false);
    	if (logenabled) {
    		item_log.setIcon(android.R.drawable.button_onoff_indicator_on);
    		item_log.setTitle(R.string.log_enabled);
    	} else {
    		item_log.setIcon(android.R.drawable.button_onoff_indicator_off);
    		item_log.setTitle(R.string.log_disabled);
    	}
    	return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_DISABLE:
    		disableOrEnable();
    		return true;
    	case MENU_TOGGLELOG:
    		toggleLogEnabled();
    		return true;
    	case MENU_APPLY:
    		applyOrSaveRules();
    		return true;
    	case MENU_EXIT:
    		finish();
    		System.exit(0);
    		return true;
    	case MENU_HELP:
    		new HelpDialog(this).show();
    		return true;
    	case MENU_SETPWD:
    		setPassword();
    		return true;
    	case MENU_SHOWLOG:
    		showLog();
    		return true;
    	case MENU_SHOWRULES:
    		showRules();
    		return true;
    	case MENU_CLEARLOG:
    		clearLog();
    		return true;
    	case MENU_SETCUSTOM:
    		setCustomScript();
    		return true;
    	}
    	return false;
    }
    /**
     * Enables or disables the firewall
     */
	private void disableOrEnable() {
		final boolean enabled = !Api.isEnabled(this);
		Log.d("Android Firewall", "Changing enabled status to: " + enabled);
		Api.setEnabled(this, enabled);
		if (enabled) {
			applyOrSaveRules();
		} else {
			purgeRules();
		}
		refreshHeader();
	}
	/**
	 * Set a new lock password
	 */
	private void setPassword() {
		new PassDialog(this, true, new android.os.Handler.Callback() {
			public boolean handleMessage(Message msg) {
				if (msg.obj != null) {
					setPassword((String)msg.obj);
				}
				return false;
			}
		}).show();
	}
	
	/**
	 * Set a new init script
	 */
	private void setCustomScript(){
		Intent intent = new Intent();
		intent.setClass(this, CustomScriptActivity.class);
		startActivityForResult(intent, 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && Api.CUSTOM_SCRIPT_MSG.equals(data.getAction())) {
			final String script = data.getStringExtra(Api.SCRIPT_EXTRA);
			final String script2 = data.getStringExtra(Api.SCRIPT2_EXTRA);
			setCustomScript(script, script2);
		}
	}
	
    /**
     * Set a new init script
     * @param script new script (empty to remove)
     * @param script2 new "shutdown" script  (empty to remove)
     */
	private void setCustomScript(String script, String script2) {
		final Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
		// Remove unnecessary white-spaces, also replace '\r\n' if necessary
		script = script.trim().replace("\r\n", "\n");
		script2 = script2.trim().replace("\r\n", "\n");
		editor.putString(Api.PREF_CUSTOMSCRIPT, script);
		editor.putString(Api.PREF_CUSTOMSCRIPT2, script2);
		int msgid;
		if (editor.commit()) {
			if (script.length() > 0 || script2.length() > 0) {
				msgid = R.string.custom_script_defined;
			} else {
				msgid = R.string.custom_script_removed;
			}
		} else {
			msgid = R.string.custom_script_error;
		}
		Toast.makeText(MainActivity.this, msgid, Toast.LENGTH_SHORT).show();
		if (Api.isEnabled(this)) {
			// If the firewall is enabled, re-apply the rules
			applyOrSaveRules();
		}
	}
	
	/**
	 * Show iptable rules on a dialog
	 */
	private void showRules() {
    	final Resources res = getResources();
		final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working), res.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
    			try {progress.dismiss();} catch(Exception ex){}
				if (!Api.hasRootAccess(MainActivity.this, true)) return;
				Api.showIptablesRules(MainActivity.this);
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}
	/**
	 * Show logs on a dialog
	 */
	private void showLog() {
    	final Resources res = getResources();
		final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working), res.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
    			try {progress.dismiss();} catch(Exception ex){}
				Api.showLog(MainActivity.this);
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}
	/**
	 * Clear logs
	 */
	private void clearLog() {
    	final Resources res = getResources();
		final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working), res.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
    			try {progress.dismiss();} catch(Exception ex){}
				if (!Api.hasRootAccess(MainActivity.this, true)) return;
				if (Api.clearLog(MainActivity.this)) {
					Toast.makeText(MainActivity.this, R.string.log_cleared, Toast.LENGTH_SHORT).show();
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}
	/**
	 * Apply or save iptable rules, showing a visual indication
	 */
	private void applyOrSaveRules() {
    	final Resources res = getResources();
		final boolean enabled = Api.isEnabled(this);
		final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working), res.getString(enabled?R.string.applying_rules:R.string.saving_rules), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
    			try {progress.dismiss();} catch(Exception ex){}
				if (enabled) {
					Log.d("Android Firewall", "Applying rules.");
					if (Api.hasRootAccess(MainActivity.this, true) && Api.applyIptablesRules(MainActivity.this, true)) {
						Toast.makeText(MainActivity.this, R.string.rules_applied, Toast.LENGTH_SHORT).show();
					} else {
						Log.d("Android Firewall", "Failed - Disabling firewall.");
						Api.setEnabled(MainActivity.this, false);
					}
				} else {
					Log.d("Android Firewall", "Saving rules.");
					Api.saveRules(MainActivity.this);
					Toast.makeText(MainActivity.this, R.string.rules_saved, Toast.LENGTH_SHORT).show();
				}
				MainActivity.this.dirty = false;
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}
	/**
	 * Purge iptable rules, showing a visual indication
	 */
	private void purgeRules() {
    	final Resources res = getResources();
		final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working), res.getString(R.string.deleting_rules), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
    			try {progress.dismiss();} catch(Exception ex){}
				if (!Api.hasRootAccess(MainActivity.this, true)) return;
				if (Api.purgeIptables(MainActivity.this, true)) {
					Toast.makeText(MainActivity.this, R.string.rules_deleted, Toast.LENGTH_SHORT).show();
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}
	/**
	 * Called an application is check/unchecked
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final DroidApp app = (DroidApp) buttonView.getTag();
		if (app != null) {
			switch (buttonView.getId()) {
				case R.id.itemcheck_wifi:
					if (app.selected_wifi != isChecked) {
						app.selected_wifi = isChecked;
						this.dirty = true;
					}
					break;
				case R.id.itemcheck_3g:
					if (app.selected_3g != isChecked) {
						app.selected_3g = isChecked;
						this.dirty = true;
					}
					break;
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.label_mode:
			selectMode();
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		// Handle the back button when dirty
		if (this.dirty && (keyCode == KeyEvent.KEYCODE_BACK)) {
			final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						applyOrSaveRules();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// Propagate the event back to perform the desired action
						MainActivity.super.onKeyDown(keyCode, event);
						break;
					}
				}
			};
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.unsaved_changes).setMessage(R.string.unsaved_changes_message)
					.setPositiveButton(R.string.apply, dialogClickListener)
					.setNegativeButton(R.string.discard, dialogClickListener).show();
			// Say that we've consumed the event
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * Asynchronous task used to load icons in a background thread.
	 */
	private static class LoadIconTask extends AsyncTask<Object, Void, View> {
		@Override
		protected View doInBackground(Object... params) {
			try {
				final DroidApp app = (DroidApp) params[0];
				final PackageManager pkgMgr = (PackageManager) params[1];
				final View viewToUpdate = (View) params[2];
				if (!app.icon_loaded) {
					app.cached_icon = pkgMgr.getApplicationIcon(app.appinfo);
					app.icon_loaded = true;
				}
				// Return the view to update at "onPostExecute"
				// Note that we cannot be sure that this view still references "app"
				return viewToUpdate;
			} catch (Exception e) {
				Log.e("Android Firewall", "Error loading icon", e);
				return null;
			}
		}
		protected void onPostExecute(View viewToUpdate) {
			try {
				// This is executed in the UI thread, so it is safe to use viewToUpdate.getTag()
				// and modify the UI
				final ListEntry entryToUpdate = (ListEntry) viewToUpdate.getTag();
				entryToUpdate.icon.setImageDrawable(entryToUpdate.app.cached_icon);
			} catch (Exception e) {
				Log.e("Android Firewall", "Error showing icon", e);
			}
		};
	}

	/**
	 * Entry representing an application in the screen
	 */
	private static class ListEntry {
		private CheckBox box_wifi;
		private CheckBox box_3g;
		private TextView text;
		private ImageView icon;
		private DroidApp app;
	}
}
