/**
 * Main application activity.
 * This is the screen displayed when you open the application
 *
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
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
 * @author Rodrigo Zechin Rosauro
 * @author Jason Tschohl
 * @version 1.0
 */

package com.jtschohl.androidfirewall;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.RejectedExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jtschohl.androidfirewall.Api.DroidApp;
import com.jtschohl.androidfirewall.RootShell.RootCommand;

/**
 * Main application activity. This is the screen displayed when you open the
 * application
 */

@SuppressLint("DefaultLocale")
public class MainActivity extends SherlockActivity implements
		OnCheckedChangeListener, OnClickListener {

	/** progress dialog instance */
	private ListView listview = null;
	/** indicates if the view has been modified and not yet saved */
	private boolean dirty = false;
	/**
	 * variables for profile names
	 */
	private String[] profileposition;

	private Menu abs_menu;

	/**
	 * Variable for userid
	 */
	int userid;

	/** tag for logcat */
	public static final String TAG = "{AF}";

	/**
	 * Variables for spinner
	 */
	private IcsSpinner spinner;
	public ArrayAdapter<String> adapter1;

	/**
	 * Navigation drawer variables
	 */

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ArrayList<String> mMenuTitles;
	private ArrayList<Integer> mIcons;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			/* enable hardware acceleration on Android >= 3.0 */
			final int FLAG_HARDWARE_ACCELERATED = WindowManager.LayoutParams.class
					.getDeclaredField("FLAG_HARDWARE_ACCELERATED").getInt(null);
			getWindow().setFlags(FLAG_HARDWARE_ACCELERATED,
					FLAG_HARDWARE_ACCELERATED);
		} catch (Exception e) {
		}
		checkPreferences();

		setContentView(R.layout.main);

		// set language
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		// use "" as default
		String language = prefs.getString("locale", "");
		Api.changeLanguage(getApplicationContext(), language);

		// set Nav drawer
		mTitle = mDrawerTitle = getTitle();
		mMenuTitles = new ArrayList<String>();
		Resources res = getResources();
		Collections
				.addAll(mMenuTitles, res.getStringArray(R.array.drawer_menu));
		mIcons = new ArrayList<Integer>();
		mIcons.add(R.drawable.ic_export);
		mIcons.add(R.drawable.ic_import);
		mIcons.add(R.drawable.ic_filemgmt);
		mIcons.add(R.drawable.ic_create);
		mIcons.add(R.drawable.ic_load);
		mIcons.add(R.drawable.ic_editnames);
		mIcons.add(R.drawable.ic_logs);
		mIcons.add(R.drawable.ic_logs);
		mIcons.add(R.drawable.ic_clearlogs);
		mIcons.add(R.drawable.ic_logs);
		mIcons.add(R.drawable.ic_pwd);
		mIcons.add(R.drawable.ic_custom);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// mDrawerList.setAdapter(new ArrayAdapter<String>(this,
		// R.layout.drawer_list, mMenuTitles));
		mDrawerList.setAdapter(new DrawerAdapter(this, mMenuTitles, mIcons));

		// Capture button clicks on side menu
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		this.findViewById(R.id.label_mode).setOnClickListener(this);
		this.findViewById(R.id.label_clear).setOnClickListener(this);
		this.findViewById(R.id.label_data).setOnClickListener(this);
		this.findViewById(R.id.label_wifi).setOnClickListener(this);
		this.findViewById(R.id.label_roam).setOnClickListener(this);
		this.findViewById(R.id.label_vpn).setOnClickListener(this);
		this.findViewById(R.id.label_invert).setOnClickListener(this);
		this.findViewById(R.id.label_lan).setOnClickListener(this);
		this.findViewById(R.id.label_input_wifi).setOnClickListener(this);

		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());

		toggleLogtarget();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			isCurrentUserOwner(getApplicationContext());
			SharedPreferences prefs2 = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			final String chainName = prefs2.getString("chainName", "");
			Log.d(TAG, "Executed isCurrentUserOwner " + chainName);
		} else {
			final SharedPreferences prefs2 = getSharedPreferences(
					Api.PREFS_NAME, 0);
			final Editor editor = prefs2.edit();
			editor.putString("chainName", "droidwall");
			editor.commit();
			Log.d(TAG,
					"Skipping isCurrentUserOwner "
							+ prefs2.getString("chainName", ""));
		}

		toggleUser();

		// create the spinner
		spinner = (IcsSpinner) findViewById(R.id.spinner);

		// profile names for spinner
		final List<String> profilestring = new ArrayList<String>();
		profilestring.add(prefs.getString("default",
				getString(R.string.defaultprofile)));
		profilestring.add(prefs.getString("profile1",
				getString(R.string.profile1)));
		profilestring.add(prefs.getString("profile2",
				getString(R.string.profile2)));
		profilestring.add(prefs.getString("profile3",
				getString(R.string.profile3)));
		profilestring.add(prefs.getString("profile4",
				getString(R.string.profile4)));
		profilestring.add(prefs.getString("profile5",
				getString(R.string.profile5)));
		profileposition = profilestring
				.toArray(new String[profilestring.size()]);

		// adapter for spinner
		adapter1 = new ArrayAdapter<String>(this,
				R.layout.sherlock_spinner_dropdown_item, profileposition);

		adapter1.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		spinner.setAdapter(adapter1);
		spinner.setSelection(prefs.getInt("itemPosition", 0));
		spinner.post(new Runnable() {
			public void run() {
				spinner.setOnItemSelectedListener(new IcsAdapterView.OnItemSelectedListener() {
					public void onItemSelected(IcsAdapterView<?> parent,
							View view, int position, long id) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = prefs.edit();
						int index = parent.getSelectedItemPosition();
						if (index == 0) {
							editor.putInt("itemPosition", index);
							editor.commit();
							LoadDefaultProfile();
						}
						if (index == 1) {
							editor.putInt("itemPosition", index);
							editor.commit();
							LoadProfile1();
						}
						if (index == 2) {
							editor.putInt("itemPosition", index);
							editor.commit();
							LoadProfile2();
						}
						if (index == 3) {
							editor.putInt("itemPosition", index);
							editor.commit();
							LoadProfile3();
						}
						if (index == 4) {
							editor.putInt("itemPosition", index);
							editor.commit();
							LoadProfile4();
						}
						if (index == 5) {
							editor.putInt("itemPosition", index);
							editor.commit();
							LoadProfile5();
						}
					}

					public void onNothingSelected(IcsAdapterView<?> parent) {
						// do nothing
					}
				});
			}
		});

		/**
		 * Search function call
		 */
		searchapps();

		Api.assertBinaries(this, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.listview == null) {
			this.listview = (ListView) this.findViewById(R.id.listview);
		}
		refreshHeader();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		final String pwd = prefs.getString("password", "");
		if (pwd.length() == 0) {
			// No password lock
			showOrLoadApplications();
		} else {
			// Check the password
			requestPassword(pwd);
		}
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		isCurrentUserOwner(getApplicationContext());
		toggleUser();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.listview.setAdapter(null);
	}

	/**
	 * The click listener for ListView in the navigation drawer
	 */

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int drawerposition, long id) {
			selectItem(drawerposition);
		}
	}

	private void selectItem(int drawerposition) {
		// Locate Position
		switch (drawerposition) {
		case 0:
			exportRules();
			break;
		case 1:
			importRules();
			break;
		case 2:
			manageRuleFiles();
			break;
		case 3:
			saveProfile();
			break;
		case 4:
			selectProfile();
			break;
		case 5:
			editProfileNames();
			break;
		case 6:
			showLog();
			break;
		case 7:
			showAcceptLog();
			break;
		case 8:
			clearLog();
			break;
		case 9:
			showRules();
			break;
		case 10:
			setPassword();
			break;
		case 11:
			setCustomScript();
			break;
		}
		mDrawerList.setItemChecked(drawerposition, true);
		// Close drawer
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	public static class DrawerFragment extends android.support.v4.app.Fragment {
		public static final String item_number = "item_number";

		public DrawerFragment() {
			// empty but required for fragment subclass
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.drawer_fragment, container,
					false);
			int i = getArguments().getInt(item_number);
			return view;
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * search function
	 */

	public void searchapps() {
		final EditText filterText = (EditText) findViewById(R.id.search);
		filterText.addTextChangedListener(filterTextWatcher);
		filterText.setTextColor(Color.WHITE);
		filterText.post(new Runnable() {
			@Override
			public void run() {
				filterText.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(filterText, InputMethodManager.SHOW_IMPLICIT);
			}
		});
	}

	/**
	 * update spinner with changed profile names
	 */
	public void updateSpinner() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		final List<String> profilestring = new ArrayList<String>();
		profilestring.add(prefs.getString("default",
				getString(R.string.defaultprofile)));
		profilestring.add(prefs.getString("profile1",
				getString(R.string.profile1)));
		profilestring.add(prefs.getString("profile2",
				getString(R.string.profile2)));
		profilestring.add(prefs.getString("profile3",
				getString(R.string.profile3)));
		profilestring.add(prefs.getString("profile4",
				getString(R.string.profile4)));
		profilestring.add(prefs.getString("profile5",
				getString(R.string.profile5)));
		profileposition = profilestring
				.toArray(new String[profilestring.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.sherlock_spinner_dropdown_item, profileposition);
		adapter.notifyDataSetChanged();
		spinner.setAdapter(adapter);
		spinner.setSelection(prefs.getInt("itemPosition", 0));
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
		if (changed)
			editor.commit();
	}

	/**
	 * Refresh informative header
	 */
	private void refreshHeader() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		final String mode = prefs.getString(Api.PREF_MODE, Api.MODE_WHITELIST);
		final TextView labelmode = (TextView) this
				.findViewById(R.id.label_mode);
		final Resources res = getResources();
		int resid = (mode.equals(Api.MODE_WHITELIST) ? R.string.mode_whitelist
				: R.string.mode_blacklist);
		labelmode.setText(res.getString(R.string.mode_header,
				res.getString(resid)));
		resid = (Api.isEnabled(this) ? R.string.title_enabled
				: R.string.title_disabled);
		setTitle(res.getString(resid));
	}

	/**
	 * refresh the spinner
	 */
	private void refreshSpinner() {
		final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
		spinner.setSelection(prefs.getInt("itemPosition", 0));
	}

	/**
	 * Displays a dialog box to select the operation mode (black or white list)
	 */
	private void selectMode() {
		final Resources res = getResources();
		new AlertDialog.Builder(this)
				.setItems(
						new String[] { res.getString(R.string.mode_whitelist),
								res.getString(R.string.mode_blacklist) },
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								final String mode = (which == 0 ? Api.MODE_WHITELIST
										: Api.MODE_BLACKLIST);
								final Editor editor = getSharedPreferences(
										Api.PREFS_NAME, 0).edit();
								editor.putString(Api.PREF_MODE, mode);
								editor.commit();
								refreshHeader();
							}
						}).setTitle(R.string.select_mode).show();
	}

	/**
	 * Set a new password lock
	 * 
	 * @param pwd
	 *            new password (empty to remove the lock)
	 */
	private void setPassword(String pwd) {
		final Resources res = getResources();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		String msg;
		String hash = md5(pwd);
		if (pwd.length() > 0) {
			editor.putString("password", hash);
			if (editor.commit()) {
				msg = res.getString(R.string.passdefined);
				getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			} else {
				msg = res.getString(R.string.passerror);
			}
		} else {
			editor.putString("password", pwd);
			editor.commit();
			msg = res.getString(R.string.passremoved);
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
				if (msg.obj != null) {
					String encrypted = ((String) msg.obj);
					String hash = md5(encrypted);
					if (!pwd.equals(hash)) {
						requestPassword(pwd);
						return false;
					}
				}
				// Password correct
				showOrLoadApplications();
				return false;
			}
		}).show();
	}

	/**
	 * Hash the password
	 */

	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void toggleVPNbutton(Context ctx) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		boolean vpnenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_VPNENABLED, false);
		Button vpn = (Button) findViewById(R.id.label_vpn);
		if (vpnenabled) {
			vpn.setVisibility(View.VISIBLE);
			editor.putBoolean("vpnsupport", true);
			editor.commit();
		} else {
			vpn.setVisibility(View.GONE);
			editor.putBoolean("vpnsupport", false);
			editor.commit();
		}
	}

	private void toggleRoambutton(Context ctx) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		boolean roamenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_ROAMENABLED, false);
		Button roam = (Button) findViewById(R.id.label_roam);
		if (roamenabled) {
			roam.setVisibility(View.VISIBLE);
			editor.putBoolean("roamingsupport", true);
			editor.commit();
		} else {
			roam.setVisibility(View.GONE);
			editor.putBoolean("roamingsupport", false);
			editor.commit();
		}
	}

	private void toggleLANbutton(Context ctx) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		boolean lanenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LANENABLED, false);
		Button lan = (Button) findViewById(R.id.label_lan);
		if (lanenabled) {
			lan.setVisibility(View.VISIBLE);
			editor.putBoolean("lansupport", true);
			editor.commit();
		} else {
			lan.setVisibility(View.GONE);
			editor.putBoolean("lansupport", false);
			editor.commit();
		}
	}

	private void toggleInputWifiButton(Context ctx) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		boolean inputwifienabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_INPUTENABLED, false);
		Button inputwifi = (Button) findViewById(R.id.label_input_wifi);
		if (inputwifienabled) {
			inputwifi.setVisibility(View.VISIBLE);
			editor.putBoolean("inputwifisupport", true);
			editor.commit();
		} else {
			inputwifi.setVisibility(View.GONE);
			editor.putBoolean("inputwifisupport", false);
			editor.commit();
		}
	}

	private void toggleUserSettings(Context ctx) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		boolean ipv6support = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_IP6TABLES, false);
		boolean logsupport = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LOGENABLED, false);
		boolean logacceptenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LOGACCEPTENABLED, false);
		boolean notifysupport = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_NOTIFY, false);
		boolean taskerenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TASKERNOTIFY, false);
	/*	boolean sdcard = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_SDCARD, false);*/
		boolean vpnenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_VPNENABLED, false);
		boolean roamenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_ROAMENABLED, false);
		boolean lanenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LANENABLED, false);
		boolean autorules = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_AUTORULES, false);
		boolean tetherenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TETHER, false);
		boolean multiuserenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_MULTIUSER, false);
		boolean inputenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_INPUTENABLED, false);
		boolean colorenabled = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_APPCOLOR, false);
		if (ipv6support) {
			editor.putBoolean("ipv6enabled", true);
			editor.commit();
		} else {
			editor.putBoolean("ipv6enabled", false);
			editor.commit();
		}
		if (logsupport) {
			editor.putBoolean("logenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("logenabled", false);
			editor.commit();
		}
		if (notifysupport) {
			editor.putBoolean("notifyenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("notifyenabled", false);
			editor.commit();
		}
		if (taskerenabled) {
			editor.putBoolean("taskertoastenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("taskertoastenabled", false);
			editor.commit();
		}
	/*	if (sdcard) {
			editor.putBoolean("sdcard", true);
			editor.commit();
		} else {
			editor.putBoolean("sdcard", false);
			editor.commit();
		}*/
		if (vpnenabled) {
			editor.putBoolean("vpnsupport", true);
			editor.commit();
		} else {
			editor.putBoolean("vpnsupport", false);
			editor.commit();
		}
		if (roamenabled) {
			editor.putBoolean("roamingsupport", true);
			editor.commit();
		} else {
			editor.putBoolean("roamingsupport", false);
			editor.commit();
		}
		if (lanenabled) {
			editor.putBoolean("lansupport", true);
			editor.commit();
		} else {
			editor.putBoolean("lansupport", false);
			editor.commit();
		}
		if (autorules) {
			editor.putBoolean("connectchangerules", true);
			editor.commit();
		} else {
			editor.putBoolean("connectchangerules", false);
			editor.commit();
		}
		if (tetherenabled) {
			editor.putBoolean("tetheringsupport", true);
			editor.commit();
		} else {
			editor.putBoolean("tetheringsupport", false);
			editor.commit();
		}
		if (multiuserenabled) {
			editor.putBoolean("multiuser", true);
			editor.commit();
		} else {
			editor.putBoolean("multiuser", false);
			editor.commit();
		}
		if (inputenabled) {
			editor.putBoolean("inputenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("inputenabled", false);
			editor.commit();
		}
		if (logacceptenabled) {
			editor.putBoolean("logacceptenabled", true);
			editor.commit();
		} else {
			editor.putBoolean("logacceptenabled", false);
			editor.commit();
		}
		if (colorenabled) {
			editor.putBoolean("appcolor", true);
			editor.commit();
		} else {
			editor.putBoolean("appcolor", false);
			editor.commit();
		}
	}

	/**
	 * If the applications are cached, just show them, otherwise load and show
	 */
	public void showOrLoadApplications() {
		final Resources res = getResources();
		final String search = "";
		if (Api.applications == null) {
			// The applications are not cached.. so lets display the progress
			// dialog
			final ProgressDialog progress = ProgressDialog.show(this,
					res.getString(R.string.working),
					res.getString(R.string.reading_apps), true);
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					Api.getApps(MainActivity.this);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					try {
						progress.dismiss();
					} catch (Exception ex) {
						Log.d("{AF} - error in showorloadapplications",
								ex.getMessage());
					}
					createListView(search);
				}
			}.execute();
		} else {
			// the applications are cached, just show the list
			createListView(search);
		}
	}

	/**
	 * sort apps when searched
	 */

	class sortList implements Comparator<DroidApp> {

		@Override
		public int compare(DroidApp o1, DroidApp o2) {
			if (o1.firstseen != o2.firstseen) {
				return (o1.firstseen ? -1 : 1);
			}
			boolean o1_selected = o1.selected_3g || o1.selected_wifi
					|| o1.selected_roaming || o1.selected_vpn
					|| o1.selected_lan || o1.selected_input_wifi;
			boolean o2_selected = o2.selected_3g || o2.selected_wifi
					|| o2.selected_roaming || o2.selected_vpn
					|| o2.selected_lan || o2.selected_input_wifi;

			if (o1_selected == o2_selected) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.names.get(0)
						.toString(), o2.names.get(0).toString());
			}
			if (o1_selected)
				return -1;
			return 1;
		}
	}

	/**
	 * Show the list of applications
	 * 
	 * Thanks to Ukanth for the Search code so I didn't have to reinvent the
	 * wheel
	 * 
	 */
	private void createListView(final String searching) {
		this.dirty = false;
		List<DroidApp> namesearch = new ArrayList<DroidApp>();
		final List<DroidApp> appnames = Api.getApps(this);
		boolean isResultsFound = false;
		if (searching != null && searching.length() > 1) {
			for (DroidApp app : appnames) {
				for (String str : app.names) {
					if (str.contains(searching.toLowerCase())
							|| str.toLowerCase().contains(
									searching.toLowerCase())) {
						namesearch.add(app);
						isResultsFound = true;
					}
				}
			}
		}
		final List<DroidApp> apps = isResultsFound ? namesearch : searching
				.equals("") ? appnames : new ArrayList<Api.DroidApp>();
		// Sort applications - selected first, then alphabetically
		Collections.sort(apps, new sortList());

		final int defaultColor = Color.WHITE;
		final int color = Color.RED;

		final LayoutInflater inflater = getLayoutInflater();
		final ListAdapter adapter = new ArrayAdapter<DroidApp>(this,
				R.layout.listitem, R.id.itemtext, apps) {
			SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
			boolean vpnenabled = prefs.getBoolean(Api.PREF_VPNENABLED, false);
			boolean roamenabled = prefs.getBoolean(Api.PREF_ROAMENABLED, false);
			boolean lanenabled = prefs.getBoolean(Api.PREF_LANENABLED, false);
			boolean inputwifienabled = prefs.getBoolean(Api.PREF_INPUTENABLED,
					false);
			boolean colorenabled = prefs.getBoolean(Api.PREF_APPCOLOR, false);

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				ListEntry entry;
				if (convertView == null) {
					// Inflate a new view
					convertView = inflater.inflate(R.layout.listitem, parent,
							false);
					Log.d(TAG, ">> inflate(" + convertView + ")");
					entry = new ListEntry();
					entry.box_wifi = (CheckBox) convertView
							.findViewById(R.id.itemcheck_wifi);
					entry.box_3g = (CheckBox) convertView
							.findViewById(R.id.itemcheck_3g);
					entry.box_roaming = (CheckBox) convertView
							.findViewById(R.id.itemcheck_roam);
					entry.box_vpn = (CheckBox) convertView
							.findViewById(R.id.itemcheck_vpn);
					entry.box_lan = (CheckBox) convertView
							.findViewById(R.id.itemcheck_lan);
					entry.box_input_wifi = (CheckBox) convertView
							.findViewById(R.id.itemcheck_input_wifi);
					if (vpnenabled) {
						entry.box_vpn.setVisibility(View.VISIBLE);
					}
					if (roamenabled) {
						entry.box_roaming.setVisibility(View.VISIBLE);
					}
					if (lanenabled) {
						entry.box_lan.setVisibility(View.VISIBLE);
					}
					if (inputwifienabled) {
						entry.box_input_wifi.setVisibility(View.VISIBLE);
					}
					entry.text = (TextView) convertView
							.findViewById(R.id.itemtext);
					entry.icon = (ImageView) convertView
							.findViewById(R.id.itemicon);
					entry.box_wifi
							.setOnCheckedChangeListener(MainActivity.this);
					entry.box_3g.setOnCheckedChangeListener(MainActivity.this);
					entry.box_roaming
							.setOnCheckedChangeListener(MainActivity.this);
					entry.box_vpn.setOnCheckedChangeListener(MainActivity.this);
					entry.box_lan.setOnCheckedChangeListener(MainActivity.this);
					entry.box_input_wifi
							.setOnCheckedChangeListener(MainActivity.this);
					convertView.setTag(entry);
				} else {
					// Convert an existing view
					entry = (ListEntry) convertView.getTag();
					entry.box_wifi = (CheckBox) convertView
							.findViewById(R.id.itemcheck_wifi);
					entry.box_3g = (CheckBox) convertView
							.findViewById(R.id.itemcheck_3g);
					if (vpnenabled) {
						entry.box_vpn.setVisibility(View.VISIBLE);
					}
					if (roamenabled) {
						entry.box_roaming.setVisibility(View.VISIBLE);
					}
					if (lanenabled) {
						entry.box_lan.setVisibility(View.VISIBLE);
					}
					if (inputwifienabled) {
						entry.box_input_wifi.setVisibility(View.VISIBLE);
					}
					entry.box_roaming = (CheckBox) convertView
							.findViewById(R.id.itemcheck_roam);
					entry.box_vpn = (CheckBox) convertView
							.findViewById(R.id.itemcheck_vpn);
					entry.box_lan = (CheckBox) convertView
							.findViewById(R.id.itemcheck_lan);
					entry.box_input_wifi = (CheckBox) convertView
							.findViewById(R.id.itemcheck_input_wifi);
				}
				entry.app = apps.get(position);
				entry.text.setText(entry.app.toString());

				ApplicationInfo app = entry.app.appinfo;
				if (colorenabled) {
					if (app != null
							&& (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						entry.text.setTextColor(defaultColor);
					} else {
						entry.text.setTextColor(color);
					}
				} else {
					entry.text.setTextColor(defaultColor);
				}

				entry.icon.setImageDrawable(entry.app.cached_icon);
				if (!entry.app.icon_loaded && entry.app.appinfo != null) {
					// this icon has not been loaded yet - load it on a
					// separated thread
					try {
						new LoadIconTask().execute(entry.app,
								getPackageManager(), convertView);
					} catch (RejectedExecutionException r) {

					}
				}
				final CheckBox box_wifi = entry.box_wifi;
				box_wifi.setTag(entry.app);
				box_wifi.setChecked(entry.app.selected_wifi);
				final CheckBox box_3g = entry.box_3g;
				box_3g.setTag(entry.app);
				box_3g.setChecked(entry.app.selected_3g);
				final CheckBox box_roaming = entry.box_roaming;
				box_roaming.setTag(entry.app);
				box_roaming.setChecked(entry.app.selected_roaming);
				final CheckBox box_vpn = entry.box_vpn;
				box_vpn.setTag(entry.app);
				box_vpn.setChecked(entry.app.selected_vpn);
				final CheckBox box_lan = entry.box_lan;
				box_lan.setTag(entry.app);
				box_lan.setChecked(entry.app.selected_lan);
				final CheckBox box_input_wifi = entry.box_input_wifi;
				box_input_wifi.setTag(entry.app);
				box_input_wifi.setChecked(entry.app.selected_input_wifi);
				return convertView;
			}
		};
		if (listview == null) {
			Api.applications = null;
			showOrLoadApplications();
		} else {
			this.listview.setAdapter(adapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		abs_menu = menu;
		toggleMenu();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			return true;
		case R.id.enableipv4:
			disableOrEnable();
			return true;
		case R.id.applyrules:
			applyOrSaveRules();
			return true;
		case R.id.exit:
			finish();
			System.exit(0);
			return true;
		case R.id.help:
			HelpDialog();
			return true;
		case R.id.usersettings:
			userSettings();
			return true;
		case R.id.sendreport:
			getReports();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem item_onoff = menu.findItem(R.id.enableipv4);
		final MenuItem item_apply = menu.findItem(R.id.applyrules);
		final boolean enabled = Api.isEnabled(this);
		if (!enabled) {
			item_apply.setTitle(R.string.saverules);
			item_onoff.setChecked(false);
		}
		if (enabled) {
			item_apply.setTitle(R.string.applyrules);
			item_onoff.setChecked(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Enables or disables the firewall
	 */
	private void disableOrEnable() {
		final boolean enabled = !Api.isEnabled(this);
		Log.d(TAG, "Changing enabled status to: " + enabled);
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
					String confirmPwd = (String) msg.obj;
					if (confirmPwd.length() > 0) {
						setConfirmPassword(confirmPwd);
						Toast.makeText(MainActivity.this,
								getString(R.string.password_enter_again),
								Toast.LENGTH_LONG).show();
						checkPassword();
					} else {
						setPassword(confirmPwd);
					}
				}
				return false;
			}
		}).show();
	}

	private void checkPassword() {
		new PassDialog(this, true, new android.os.Handler.Callback() {
			public boolean handleMessage(Message msg) {
				if (msg.obj != null) {
					if (getPassword().equals((String) msg.obj)) {
						setPassword((String) msg.obj);
					} else {
						Toast.makeText(MainActivity.this,
								getString(R.string.password_not_same),
								Toast.LENGTH_LONG).show();
						setPassword();
					}
				}
				return false;
			}
		}).show();
	}

	/**
	 * Ask for password twice and confirm it is the same before setting it to
	 * PREF_PASSWORD
	 */
	private String userPassword = "";

	public String getPassword() {
		return userPassword;
	}

	public void setConfirmPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * Set a new init script
	 */
	private void setCustomScript() {
		Intent intent = new Intent();
		intent.setClass(this, CustomScriptActivity.class);
		startActivityForResult(intent, 0);
	}

	// import rules file
	public void importRules() {
		Intent intent = new Intent();
		intent.setClass(this, RulesDialog.class);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, IMPORT_RULES_REQUEST);
		} else {
			Toast.makeText(this, R.string.no_rules_file, Toast.LENGTH_LONG)
					.show();
		}

	}

	// export rules file
	public void exportRules() {
		Intent intent = new Intent();
		intent.setClass(this, ExportRulesDialog.class);
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			startActivityForResult(intent, EXPORT_RULES_REQUEST);
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Toast.makeText(this, R.string.no_storage, Toast.LENGTH_LONG).show();
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			Toast.makeText(this, R.string.no_storage, Toast.LENGTH_LONG).show();
		}

	}

	// manage Rule files
	public void manageRuleFiles() {
		Intent intent = new Intent();
		intent.setClass(this, DeleteRulesDialog.class);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, MANAGE_RULES_REQUEST);
		} else {
			Toast.makeText(this, R.string.no_rules_file, Toast.LENGTH_LONG)
					.show();
		}

	}

	// open save profiles dialog
	public void saveProfile() {
		Intent intent = new Intent();
		intent.setClass(this, SaveProfileDialog.class);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, 0);
		} else {
			Toast.makeText(this, R.string.no_rules_file, Toast.LENGTH_LONG)
					.show();
		}

	}

	// open load profile dialog

	public void selectProfile() {
		Intent intent = new Intent();
		intent.setClass(this, LoadProfile.class);
		File filepath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/androidfirewall/");
		if (filepath.isDirectory()) {
			startActivityForResult(intent, LOAD_PROFILE_REQUEST);
		} else {
			Toast.makeText(this, R.string.no_rules_file, Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * Edit profile names
	 */
	private void editProfileNames() {
		Intent intent = new Intent();
		intent.setClass(this, EditProfileNames.class);
		startActivityForResult(intent, EDIT_PROFILE_REQUEST);
	}

	/**
	 * User Settings
	 */
	private void userSettings() {
		Intent intent = new Intent();
		intent.setClass(this, UserSettings.class);
		startActivityForResult(intent, USER_SETTINGS_REQUEST);
	}

	// set Request Code for Rules Import
	static final int IMPORT_RULES_REQUEST = 10;
	// set Request code for Rules export
	static final int EXPORT_RULES_REQUEST = 20;
	// set Request Code for Rule Management
	static final int MANAGE_RULES_REQUEST = 30;
	// set Request Code for Profile loading
	static final int LOAD_PROFILE_REQUEST = 40;
	// set Request Code for Edit Profile Names
	static final int EDIT_PROFILE_REQUEST = 50;
	// set Request Code for User Settings
	static final int USER_SETTINGS_REQUEST = 60;
	// set Request Code for Language Change
	static final int CHANGE_LANGUAGE_REQUEST = 70;

	// @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK
				&& Api.CUSTOM_SCRIPT_MSG.equals(data.getAction())) {
			final String script = data.getStringExtra(Api.SCRIPT_EXTRA);
			final String script2 = data.getStringExtra(Api.SCRIPT2_EXTRA);
			setCustomScript(script, script2);
		}
		if (requestCode == IMPORT_RULES_REQUEST && resultCode == RESULT_OK) {
			Toast.makeText(this, R.string.rules_import_successfully,
					Toast.LENGTH_SHORT).show();
			Api.applications = null;
			showOrLoadApplications();
			toggleVPNbutton(getApplicationContext());
			toggleRoambutton(getApplicationContext());
			toggleLANbutton(getApplicationContext());
			toggleUserSettings(getApplicationContext());
		}
		if (requestCode == EXPORT_RULES_REQUEST && resultCode == RESULT_OK) {
			Toast.makeText(this, R.string.rules_export_successfully,
					Toast.LENGTH_SHORT).show();
			String exportedName = data.getStringExtra(Api.EXPORT_EXTRA);
			Api.exportRulesToFile(MainActivity.this, exportedName);

		}
		if (requestCode == MANAGE_RULES_REQUEST && resultCode == RESULT_OK) {
			Toast.makeText(this, R.string.rules_file_deleted,
					Toast.LENGTH_SHORT).show();
			manageRuleFiles();
		}
		if (requestCode == LOAD_PROFILE_REQUEST && resultCode == RESULT_OK) {
			Toast.makeText(this, R.string.profileapplied, Toast.LENGTH_SHORT)
					.show();
			Api.applications = null;
			showOrLoadApplications();
			refreshHeader();
			refreshSpinner();
			toggleVPNbutton(getApplicationContext());
			toggleRoambutton(getApplicationContext());
			toggleLANbutton(getApplicationContext());
			toggleUserSettings(getApplicationContext());
			if (Api.isEnabled(getApplicationContext())) {
				Api.applyIptablesRules(getApplicationContext(), true);
			} else {
				Api.saveRules(getApplicationContext());
			}
		}
		if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
			updateSpinner();
		}
		if (requestCode == USER_SETTINGS_REQUEST && resultCode == RESULT_OK) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			Intent intent = getIntent();
			finish();
			toggleVPNbutton(getApplicationContext());
			toggleRoambutton(getApplicationContext());
			toggleLANbutton(getApplicationContext());
			String language = prefs.getString("locale", Locale.getDefault()
					.getDisplayLanguage());
			Api.changeLanguage(getApplicationContext(), language);
			startActivity(intent);
		}
		// for debugging purposes
		// if (resultCode == RESULT_CANCELED)
		// Toast.makeText(this, "Operation Canceled",
		// Toast.LENGTH_SHORT).show();
	}

	/**
	 * Set a new init script
	 * 
	 * @param script
	 *            new script (empty to remove)
	 * @param script2
	 *            new "shutdown" script (empty to remove)
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
		Intent intent = new Intent();
		intent.setClass(this, showRules.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * Show logs on a dialog
	 */
	private void showLog() {
		Api.dmesgCommand = "dmesg | $GREP \\[AndroidFirewall\\]\n";
		Api.nflogCommand = "[AndroidFirewall]";
		Api.logstring = "[AndroidFirewall]";
		Api.rejectlog = true;
		Intent intent = new Intent();
		intent.setClass(this, showLog.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * Show logs on a dialog
	 */
	private void showAcceptLog() {
		Api.dmesgCommand = "dmesg | $GREP \\[AndroidFirewallAccept\\]\n";
		Api.nflogCommand = "[AndroidFirewallAccept]";
		Api.logstring = "[AndroidFirewallAccept]";
		Api.rejectlog = false;
		Intent intent = new Intent();
		intent.setClass(this, showLog.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * Dispay Help
	 */

	private void HelpDialog() {
		Intent intent = new Intent();
		intent.setClass(this, HelpDialog.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * Clear logs
	 */
	private void clearLog() {
		final Resources res = getResources();
		final ProgressDialog progress = ProgressDialog.show(this,
				res.getString(R.string.working),
				res.getString(R.string.please_wait), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					progress.dismiss();
				} catch (Exception ex) {
				}
				if (!Api.hasRootAccess(MainActivity.this, true))
					return;
				if (Api.clearLog(MainActivity.this)) {
					Toast.makeText(MainActivity.this, R.string.log_cleared,
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 100);
	}

	/**
	 * Sets the menu correctly after a modification by the
	 * Tasker/Locale/Shortcuts
	 */

	private void toggleMenu() {
		final boolean enabled = getApplicationContext().getSharedPreferences(
				Api.PREFS_NAME, 0).getBoolean(Api.PREF_ENABLED, false);
		Log.d(TAG, "toggleMenu has been run");
		if (enabled) {
			Log.d(TAG, "Firewall is enabled");
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
				Log.d(TAG, "toggleMenu has set menu to ENABLED");
			}
		} else {
			Log.d(TAG, "Firewall is disabled");
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
				Log.d(TAG, "toggleMenu has set menu to DISABLED");
			}
		}
	}

	/**
	 * Apply or save iptable rules, showing a visual indication
	 */
	private void applyOrSaveRules() {
		final Resources res = getResources();
		final boolean enabled = Api.isEnabled(this);
		final ProgressDialog progress = ProgressDialog.show(this, res
				.getString(R.string.working), res
				.getString(enabled ? R.string.applying_rules
						: R.string.saving_rules), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				int i;
				try {
					progress.dismiss();
				} catch (Exception ex) {
				}
				if (enabled) {
					Log.d(TAG, "Applying rules.");
					if (Api.hasRootAccess(MainActivity.this, true)
							&& Api.applyIptablesRules(MainActivity.this, true)) {
						Toast.makeText(MainActivity.this,
								R.string.rules_applied, Toast.LENGTH_SHORT)
								.show();
						if (abs_menu != null) {
							final MenuItem item_onoff = abs_menu
									.findItem(R.id.enableipv4);
							final MenuItem item_apply = abs_menu
									.findItem(R.id.applyrules);
							item_apply.setTitle(R.string.applyrules);
							item_onoff.setChecked(true);
						}
						i = prefs.getInt("itemPosition", 0);
						if (i == 0) {
							saveDefaultProfile();
						}
						if (i == 1) {
							saveProfile1();
						}
						if (i == 2) {
							saveProfile2();
						}
						if (i == 3) {
							saveProfile3();
						}
						if (i == 4) {
							saveProfile4();
						}
						if (i == 5) {
							saveProfile5();
						}
					} else {
						Log.d(TAG, "Failed - Disabling firewall.");
						Api.setEnabled(MainActivity.this, false);
						if (abs_menu != null) {
							final MenuItem item_onoff = abs_menu
									.findItem(R.id.enableipv4);
							final MenuItem item_apply = abs_menu
									.findItem(R.id.applyrules);
							item_apply.setTitle(R.string.saverules);
							item_onoff.setChecked(false);
						}
					}

				}

				else {
					Log.d(TAG, "Saving rules.");
					Api.saveRules(MainActivity.this);
					Toast.makeText(MainActivity.this, R.string.rules_saved,
							Toast.LENGTH_SHORT).show();
					i = prefs.getInt("itemPosition", 0);
					if (i == 0) {
						saveDefaultProfile();
					}
					if (i == 1) {
						saveProfile1();
					}
					if (i == 2) {
						saveProfile2();
					}
					if (i == 3) {
						saveProfile3();
					}
					if (i == 4) {
						saveProfile4();
					}
					if (i == 5) {
						saveProfile5();
					}
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
		final ProgressDialog progress = ProgressDialog.show(this,
				res.getString(R.string.working),
				res.getString(R.string.deleting_rules), true);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					progress.dismiss();
				} catch (Exception ex) {
				}
				if (!Api.hasRootAccess(MainActivity.this, true))
					return;
				if (Api.purgeIptables(MainActivity.this, true)) {
					Toast.makeText(MainActivity.this, R.string.rules_deleted,
							Toast.LENGTH_SHORT).show();
					if (abs_menu != null) {
						final MenuItem item_onoff = abs_menu
								.findItem(R.id.enableipv4);
						final MenuItem item_apply = abs_menu
								.findItem(R.id.applyrules);
						item_apply.setTitle(R.string.saverules);
						item_onoff.setChecked(false);
					}
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
			case R.id.itemcheck_roam:
				if (app.selected_roaming != isChecked) {
					app.selected_roaming = isChecked;
					this.dirty = true;
				}
				break;
			case R.id.itemcheck_vpn:
				if (app.selected_vpn != isChecked) {
					app.selected_vpn = isChecked;
					this.dirty = true;
				}
				break;
			case R.id.itemcheck_lan:
				if (app.selected_lan != isChecked) {
					app.selected_lan = isChecked;
					this.dirty = true;
				}
				break;
			case R.id.itemcheck_input_wifi:
				if (app.selected_input_wifi != isChecked) {
					app.selected_input_wifi = isChecked;
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
		case R.id.label_wifi:
			selectAllWiFi();
			break;
		case R.id.label_data:
			selectAllData();
			break;
		case R.id.label_roam:
			selectAllRoam();
			break;
		case R.id.label_clear:
			clearAllEntries();
			break;
		case R.id.label_invert:
			invertApps();
			break;
		case R.id.label_vpn:
			selectAllVpn();
			break;
		case R.id.label_lan:
			selectAllLan();
			break;
		case R.id.label_input_wifi:
			selectAllInputWifi();
			break;
		}
	}

	/**
	 * The following functions are for selecting all of a certain rule
	 */

	private void selectAllData() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_3g = true;
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void selectAllRoam() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_roaming = true;
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void selectAllWiFi() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_wifi = true;
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void clearAllEntries() {
		SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		boolean vpnenabled = prefs.getBoolean(Api.PREF_VPNENABLED, false);
		boolean roamenabled = prefs.getBoolean(Api.PREF_ROAMENABLED, false);
		boolean lanenabled = prefs.getBoolean(Api.PREF_LANENABLED, false);
		boolean inputwifienabled = prefs.getBoolean(Api.PREF_INPUTENABLED,
				false);
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_wifi = false;
			if (roamenabled) {
				app.selected_roaming = false;
			}
			app.selected_3g = false;
			if (vpnenabled) {
				app.selected_vpn = false;
			}
			if (lanenabled) {
				app.selected_lan = false;
			}
			if (inputwifienabled) {
				app.selected_input_wifi = false;
			}
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void invertApps() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		boolean vpnenabled = prefs.getBoolean(Api.PREF_VPNENABLED, false);
		boolean lanenabled = prefs.getBoolean(Api.PREF_LANENABLED, false);
		boolean inputwifienabled = prefs.getBoolean(Api.PREF_INPUTENABLED,
				false);
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_3g = !app.selected_3g;
			app.selected_wifi = !app.selected_wifi;
			if (vpnenabled) {
				app.selected_vpn = !app.selected_vpn;
			}
			if (lanenabled) {
				app.selected_lan = !app.selected_lan;
			}
			if (inputwifienabled) {
				app.selected_input_wifi = !app.selected_input_wifi;
			}
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void selectAllVpn() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_vpn = true;
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void selectAllLan() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_lan = true;
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
	}

	private void selectAllInputWifi() {
		BaseAdapter adapter = (BaseAdapter) listview.getAdapter();
		int count = adapter.getCount();
		for (int item = 0; item < count; item++) {
			DroidApp app = (DroidApp) adapter.getItem(item);
			app.selected_input_wifi = true;
			this.dirty = true;
		}
		adapter.notifyDataSetChanged();
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
						finish();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// Propagate the event back to perform the desired
						// action
						MainActivity.super.onKeyDown(keyCode, event);
						finish();
						break;
					}
				}
			};
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.unsaved_changes)
					.setMessage(R.string.unsaved_changes_message)
					.setPositiveButton(R.string.apply, dialogClickListener)
					.setNegativeButton(R.string.discard, dialogClickListener)
					.show();
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
				// Note that we cannot be sure that this view still references
				// "app"
				return viewToUpdate;
			} catch (Exception e) {
				Log.e(TAG, "Error loading icon", e);
				return null;
			}
		}

		protected void onPostExecute(View viewToUpdate) {
			try {
				// This is executed in the UI thread, so it is safe to use
				// viewToUpdate.getTag()
				// and modify the UI
				final ListEntry entryToUpdate = (ListEntry) viewToUpdate
						.getTag();
				entryToUpdate.icon
						.setImageDrawable(entryToUpdate.app.cached_icon);
			} catch (Exception e) {
				Log.e(TAG, "Error showing icon", e);
			}
		};
	}

	/**
	 * Entry representing an application in the screen
	 */
	private static class ListEntry {
		private CheckBox box_wifi;
		private CheckBox box_3g;
		private CheckBox box_roaming;
		private CheckBox box_vpn;
		private CheckBox box_lan;
		private CheckBox box_input_wifi;
		private TextView text;
		private ImageView icon;
		private DroidApp app;
	}

	private void LoadDefaultProfile() {
		SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs2 = getSharedPreferences(Api.PREF_PROFILE,
				Context.MODE_PRIVATE);
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
		Api.applications = null;
		showOrLoadApplications();
		refreshHeader();
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
			}
		} else {
			Api.saveRules(getApplicationContext());
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
			}
		}
	}

	private void LoadProfile1() {
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
		Api.applications = null;
		showOrLoadApplications();
		refreshHeader();
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
			}
		} else {
			Api.saveRules(getApplicationContext());
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
			}
		}
	}

	private void LoadProfile2() {
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
		Api.applications = null;
		showOrLoadApplications();
		refreshHeader();
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
			}
		} else {
			Api.saveRules(getApplicationContext());
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
			}
		}
	}

	private void LoadProfile3() {
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
		Api.applications = null;
		showOrLoadApplications();
		refreshHeader();
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
			}
		} else {
			Api.saveRules(getApplicationContext());
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
			}
		}
	}

	private void LoadProfile4() {
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
		Api.applications = null;
		showOrLoadApplications();
		refreshHeader();
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
			}
		} else {
			Api.saveRules(getApplicationContext());
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
			}
		}
	}

	private void LoadProfile5() {
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
		Api.applications = null;
		showOrLoadApplications();
		refreshHeader();
		toggleVPNbutton(getApplicationContext());
		toggleRoambutton(getApplicationContext());
		toggleLANbutton(getApplicationContext());
		toggleInputWifiButton(getApplicationContext());
		toggleUserSettings(getApplicationContext());
		if (Api.isEnabled(getApplicationContext())) {
			Api.applyIptablesRules(getApplicationContext(), true);
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.applyrules);
				item_onoff.setChecked(true);
			}
		} else {
			Api.saveRules(getApplicationContext());
			if (abs_menu != null) {
				final MenuItem item_onoff = abs_menu.findItem(R.id.enableipv4);
				final MenuItem item_apply = abs_menu.findItem(R.id.applyrules);
				item_apply.setTitle(R.string.saverules);
				item_onoff.setChecked(false);
			}
		}
	}

	private void saveDefaultProfile() {
		SharedPreferences prefs2 = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs = getSharedPreferences(Api.PREF_PROFILE,
				Context.MODE_PRIVATE);
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
	}

	private void saveProfile1() {
		SharedPreferences prefs2 = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs = getSharedPreferences(Api.PREF_PROFILE1,
				Context.MODE_PRIVATE);
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
	}

	private void saveProfile2() {
		SharedPreferences prefs2 = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs = getSharedPreferences(Api.PREF_PROFILE2,
				Context.MODE_PRIVATE);
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
	}

	private void saveProfile3() {
		SharedPreferences prefs2 = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs = getSharedPreferences(Api.PREF_PROFILE3,
				Context.MODE_PRIVATE);
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
	}

	private void saveProfile4() {
		SharedPreferences prefs2 = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs = getSharedPreferences(Api.PREF_PROFILE4,
				Context.MODE_PRIVATE);
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
	}

	private void saveProfile5() {
		SharedPreferences prefs2 = getSharedPreferences(Api.PREFS_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences prefs = getSharedPreferences(Api.PREF_PROFILE5,
				Context.MODE_PRIVATE);
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
	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			createListView(s.toString());
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			createListView(s.toString());
		}
	};

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				if (abs_menu != null) {
					abs_menu.performIdentifierAction(R.id.menu_items, 0);
					return true;
				}
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * many thanks to Mr. Cernekee for this GPLv3 code Original code located
	 * here:
	 * 
	 * https://github.com/ukanth/afwall/blob/master/src/dev/ukanth/ufirewall/Api
	 * .java
	 * 
	 */

	private void toggleLogtarget() {
		final Context ctx = getApplicationContext();

		LogProbeCallback cb = new LogProbeCallback();
		cb.ctx = ctx;

		new RootCommand().setReopenShell(true)
				.setFailureToast(R.string.log_failed).setCallback(cb)
				.setLogging(true).run(ctx, "cat /proc/net/ip_tables_targets");
	}

	private class LogProbeCallback extends RootCommand.Callback {
		public Context ctx;

		SharedPreferences prefs = getApplicationContext().getSharedPreferences(
				Api.PREFS_NAME, 0);
		final Editor editor = prefs.edit();
		boolean nflog = false;
		boolean log = false;

		public void cbFunc(RootCommand state) {
			if (state.exitCode != 0) {
				return;
			}

			for (String str : state.lastCommandResult.toString().split("\n")) {
				if ("NFLOG".equals(str)) {
					nflog = true;
					Log.d(TAG, "NFLOG fetch " + Api.PREF_LOGTARGET);
				} else if ("LOG".equals(str)) {
					Log.d(TAG, "LOG fetch " + Api.PREF_LOGTARGET);
					log = true;
				}
				if (nflog == true && log == true) {
					editor.putString(Api.PREF_LOGTARGET, "LOG");
					editor.commit();
				}
				if (log == true && nflog == false) {
					editor.putString(Api.PREF_LOGTARGET, "LOG");
					editor.commit();
				}
				if (log == false && nflog == true) {
					editor.putString(Api.PREF_LOGTARGET, "NFLOG");
					editor.commit();
				}
				if (log == false && nflog == false) {
					editor.putString(Api.PREF_LOGTARGET, "");
					editor.commit();
					Log.d(TAG, "Empty fetch " + Api.PREF_LOGTARGET);
				}
			}
		}
	}

	@SuppressLint("NewApi")
	public void isCurrentUserOwner(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			try {
				Method getUserHandle = UserManager.class
						.getMethod("getUserHandle");
				int userHandle = (Integer) getUserHandle.invoke(context
						.getSystemService(Context.USER_SERVICE));
				Log.d(TAG, String.format("Found user value = %d", userHandle));
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong("userID", userHandle);
				editor.commit();
			} catch (Exception e) {
				Log.d(TAG, "Exception on isCurrentUserOwner " + e.getMessage());
			}
		}
	}

	public void toggleUser() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		boolean multiuserenabled = getApplicationContext()
				.getSharedPreferences(Api.PREFS_NAME, 0).getBoolean(
						Api.PREF_MULTIUSER, false);
		if (prefs.getLong("userID", 0) == 0) {
			Log.d(TAG, "userHandle is 0");
			editor.putString("chainName", "droidwall");
			editor.commit();
			Log.d(TAG, "User = " + prefs.getLong("userID", 0)
					+ " and CHAINNAME = " + prefs.getString("chainName", ""));
		} else {
			Log.d(TAG, "userHandle greater than 0");
			if (multiuserenabled) {
				editor.putString("chainName", prefs.getLong("userID", 0)
						+ "droidwall");
				editor.commit();
				Log.d(TAG,
						"User = " + prefs.getLong("userID", 0)
								+ " and CHAINNAME = "
								+ prefs.getString("chainName", ""));
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.multiuser_title);
				builder.setMessage(R.string.multiuser_disabled);
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								userSettings();
							}
						});
				AlertDialog dialog = builder.show();
				TextView msg = (TextView) dialog
						.findViewById(android.R.id.message);
				msg.setGravity(Gravity.CENTER);
			}
		}
	}

	/**
	 * get error reports
	 */

	private void getReports() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			Toast.makeText(MainActivity.this, R.string.generate_reports,
					Toast.LENGTH_SHORT).show();
			getInterfaceInfo();
			Log.d(TAG,
					"Able to read/write to external storage while running reports");
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Toast.makeText(this, R.string.no_storage, Toast.LENGTH_LONG).show();
			Log.d(TAG,
					"Read only access to external storage while trying to run reports");
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			Toast.makeText(this, R.string.no_storage, Toast.LENGTH_LONG).show();
			Log.d(TAG,
					"Something is wrong with access to external storage while trying to run reports");
		}
	}

	/**
	 * get Interface Information
	 */

	private void getInterfaceInfo() {
		final Context ctx = getApplicationContext();

		InterfaceCallback cb = new InterfaceCallback();
		cb.ctx = ctx;

		new RootCommand().setReopenShell(true)
				.setFailureToast(R.string.interface_fail).setCallback(cb)
				.setLogging(true).run(ctx, "ls /sys/class/net");
	}

	private class InterfaceCallback extends RootCommand.Callback {
		public Context ctx;
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "interfaces.txt";
		File file = new File(dir, filename);
		FileOutputStream fout = null;
		OutputStreamWriter output = null;

		public void cbFunc(RootCommand state) {
			if (state.exitCode != 0) {
				return;
			}
			dir.mkdirs();

			try {
				for (String str : state.lastCommandResult.toString().split(
						"\r\n")) {
					fout = new FileOutputStream(file);
					output = new OutputStreamWriter(fout);
					output.write(str, 0, str.length());
				}
			} catch (IOException e) {
				Log.e(TAG, "File write failed: " + e.toString());
			} finally {
				try {
					if (output != null) {
						output.close();
						Log.d(TAG, "OUTPUT Closed");
					}
					if (fout != null) {
						fout.close();
						Log.d(TAG, "FOUT Closed");
						getIptablesInfo();
					}
				} catch (IOException e) {
					Log.e(TAG, String.format("File close failed: %s",
							e.toString()));
				}
			}
		}
	}

	/**
	 * Get iptables information
	 */

	private void getIptablesInfo() {
		final Context ctx = getApplicationContext();
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "iptables.txt";
		File file = new File(dir, filename);
		FileOutputStream fout = null;
		OutputStreamWriter output = null;
		String iptables = Api.showIptablesRules(ctx);

		try {
			for (String str : iptables.split("\r\n")) {
				fout = new FileOutputStream(file);
				output = new OutputStreamWriter(fout);
				output.write(str, 0, str.length());
			}
		} catch (IOException e) {
			Log.e(TAG, "File write failed: " + e.toString());
		} finally {
			try {
				if (output != null) {
					output.close();
					Log.d(TAG, "OUTPUT Closed");
				}
				if (fout != null) {
					fout.close();
					Log.d(TAG, "FOUT Closed");
					//getIfconfigInfo();
					getLogcatInfo();
				}
			} catch (IOException e) {
				Log.e(TAG, String.format("File close failed: %s", e.toString()));
			}
		}
	}

	/**
	 * get ifconfig Information
	 */

	/*private void getIfconfigInfo() {
		final Context ctx = getApplicationContext();
		String ifconfig = Api.getIfconfigPath(ctx);
		IfconfigCallback cb = new IfconfigCallback();
		cb.ctx = ctx;

		new RootCommand().setReopenShell(true)
				.setFailureToast(R.string.ifconfig_fail).setCallback(cb)
				.setLogging(true).run(ctx, ifconfig);
	}

	private class IfconfigCallback extends RootCommand.Callback {
		public Context ctx;
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "ifconfig.txt";
		File file = new File(dir, filename);
		FileOutputStream fout = null;
		OutputStreamWriter output = null;

		public void cbFunc(RootCommand state) {
			if (state.exitCode != 0) {
				return;
			}
			dir.mkdirs();

			try {
				for (String str : state.lastCommandResult.toString().split(
						"\r\n")) {
					fout = new FileOutputStream(file);
					output = new OutputStreamWriter(fout);
					output.write(str, 0, str.length());
				}
			} catch (IOException e) {
				Log.e(TAG, "File write failed: " + e.toString());
			} finally {
				try {
					if (output != null) {
						output.close();
					}
					if (fout != null) {
						fout.close();
						Log.d(TAG, "FOUT Closed");
						getLogcatInfo();
					}
				} catch (IOException e) {
					Log.e(TAG, String.format("File close failed: %s",
							e.toString()));
				}
			}
		}
	} */

	/**
	 * get Logcat Information
	 */

	private void getLogcatInfo() {
		final Context ctx = getApplicationContext();

		LogcatCallback cb = new LogcatCallback();
		cb.ctx = ctx;

		new RootCommand().setReopenShell(true)
				.setFailureToast(R.string.interface_fail).setCallback(cb)
				.setLogging(true).run(ctx, "logcat -d");
	}

	private class LogcatCallback extends RootCommand.Callback {
		public Context ctx;
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "logcat.txt";
		File file = new File(dir, filename);
		FileOutputStream fout = null;
		OutputStreamWriter output = null;

		public void cbFunc(RootCommand state) {
			if (state.exitCode != 0) {
				return;
			}
			dir.mkdirs();

			try {
				for (String str : state.lastCommandResult.toString().split(
						"\r\n")) {
					fout = new FileOutputStream(file);
					output = new OutputStreamWriter(fout);
					output.write(str, 0, str.length());
				}
			} catch (IOException e) {
				Log.e(TAG, "File write failed: " + e.toString());
			} finally {
				try {
					if (output != null) {
						output.close();
						Log.d(TAG, "OUTPUT Closed");
					}
					if (fout != null) {
						fout.close();
						Log.d(TAG, "FOUT Closed");
						getDeviceInfo();
					}
				} catch (IOException e) {
					Log.e(TAG, String.format("File close failed: %s",
							e.toString()));
				}
			}
		}
	}

	/**
	 * get device information
	 */

	public void getDeviceInfo() {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "deviceinfo.txt";
		File file = new File(dir, filename);
		OutputStreamWriter output = null;
		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0);
		final boolean whitelist = prefs.getString(Api.PREF_MODE, Api.MODE_WHITELIST)
				.equals(Api.MODE_WHITELIST);
		final boolean logenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LOGENABLED, false);
		final boolean vpnenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_VPNENABLED, false);
		final boolean lanenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_LANENABLED, false);
		final boolean roamenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_ROAMENABLED, false);
		final boolean ipv6enabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_IP6TABLES, false);
		final boolean enabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_ENABLED, false);
		final boolean tetherenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TETHER, false);
		final boolean inputenabled = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_INPUTENABLED, false);
		final boolean logacceptenabled = getApplicationContext()
				.getSharedPreferences(Api.PREFS_NAME, 0).getBoolean(
						Api.PREF_LOGACCEPTENABLED, false);
		final boolean autorules = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_AUTORULES, false);
		final boolean notify = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_NOTIFY, false);
		final boolean taskernotify = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_TASKERNOTIFY, false);
		final boolean appcolor = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_APPCOLOR, false);
	/*	final boolean sdcard = getApplicationContext().getSharedPreferences(Api.PREFS_NAME, 0)
				.getBoolean(Api.PREF_SDCARD, false);*/

		try {
			output = new OutputStreamWriter(new FileOutputStream(file));
			output.append("Android version: "
					+ android.os.Build.VERSION.RELEASE + "\n");
			output.append("OEM: " + android.os.Build.MANUFACTURER + "\n");
			output.append("Device Model: " + android.os.Build.MODEL + "\n");
			output.append("Device Build: " + android.os.Build.DISPLAY + "\n");
			if (whitelist) {
				output.append("Whitelist Enabled\n");
			} else {
				output.append("Blacklist Enabled\n");
			}
			if (logenabled){
				output.append("Log Enabled\n");
			}
			if (vpnenabled){
				output.append("VPN Enabled\n");
			}
			if (autorules){
				output.append("Automatic Rules Enabled\n");
			}
			if (lanenabled){
				output.append("LAN Enabled\n");
			}
			if (roamenabled){
				output.append("Roaming Enabled\n");
			}
			if (ipv6enabled){
				output.append("IPv6 Enabled\n");
			}
			if (enabled){
				output.append("Firewall Enabled\n");
			}
			if (tetherenabled){
				output.append("Tethering Enabled\n");
			}
			if (inputenabled){
				output.append("INPUT Chains Enabled\n");
			}
			if (logacceptenabled){
				output.append("ACCEPT Log Enabled\n");
			}
		/*	if (sdcard){
				output.append("SDCard Support Enabled\n");
			}*/
			if (notify){
				output.append("Notifications Enabled\n");
			}
			if (taskernotify){
				output.append("Tasker/Locale/Shortcut Notifications Enabled\n");
			}
			if (appcolor){
				output.append("System App Color Enabled\n");
			}
		} catch (IOException error) {
			error.printStackTrace();
			Log.e(TAG, "IOException while creating deviceinfo.txt " + error);
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException errors) {
				errors.printStackTrace();
				Log.e(TAG, "IOException closing deviceinfo.txt " + errors);
			}
		}
		zipFiles();
	}

	/**
	 * Zip error reports
	 */

	public void zipFiles() {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "af_error_reports.zip";
		String[] reports = { dir + "/iptables.txt", dir + "/logcat.txt",
				dir + "/interfaces.txt", /*dir + "/ifconfig.txt",*/
				dir + "/deviceinfo.txt" };
		File file = new File(dir, filename);

		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(file);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			byte data[] = new byte[2048];

			for (int i = 0; i < reports.length; i++) {
				Log.v(TAG, "Compressing folder: " + reports[i]);
				FileInputStream fi = new FileInputStream(reports[i]);
				origin = new BufferedInputStream(fi, 2048);
				ZipEntry entry = new ZipEntry(reports[i].substring(reports[i]
						.lastIndexOf("/") + 1));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, 2048)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
			Toast.makeText(MainActivity.this, R.string.generate_zip,
					Toast.LENGTH_SHORT).show();
			emailErrorReports();
		} catch (Exception e) {
			Log.e(TAG, "Error zipping folder");
			e.printStackTrace();
		}
	}

	/**
	 * Email error reports
	 */

	private void emailErrorReports() {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/af_error_reports/");
		String filename = "af_error_reports.zip";
		File file = new File(dir, filename);
		String af_version;
		try {
			af_version = getApplicationContext()
					.getPackageManager()
					.getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			af_version = "Unknown";
		}
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "androidfirewall.developer@gmail.com" });
		intent.putExtra(Intent.EXTRA_SUBJECT,
				"Android Firewall Error Report for version " + af_version);
		intent.putExtra(Intent.EXTRA_TEXT, "");
		if (!file.exists() || !file.canRead()) {
			Toast.makeText(this, R.string.no_zip, Toast.LENGTH_SHORT).show();
			Log.d(TAG, "No zip file is available");
			finish();
			return;
		}
		Uri uri = Uri.fromFile(file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intent,
				getString(R.string.send_email)));
		return;
	}

	private class DrawerAdapter extends BaseAdapter {
		private ArrayList<String> itemlist;
		private Context context;
		private ArrayList<Integer> iconlist;

		public DrawerAdapter(Context ctx, ArrayList<String> list,
				ArrayList<Integer> list2) {
			this.context = ctx;
			this.itemlist = list;
			this.iconlist = list2;
		}

		@Override
		public int getCount() {
			return itemlist.size();
		}

		@Override
		public String getItem(int i) {
			return itemlist.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder holder;

			if (null == view) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.drawer_layout, viewGroup,
						false);
				holder = new ViewHolder();
				holder.description = (TextView) view
						.findViewById(R.id.description);
				holder.imageView = (ImageView) view
						.findViewById(R.id.imageView);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.description.setText(itemlist.get(i));
			holder.imageView.setImageResource(mIcons.get(i));
			return view;
		}
	}

	private class ViewHolder {
		public TextView description;
		public ImageView imageView;
	}
}
