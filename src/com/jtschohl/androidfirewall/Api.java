/**
 * Contains shared programming interfaces.
 * All iptables "communication" is handled by this class.
 * 
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
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
 * @author Rodrigo Zechin Rosauro
 * @author Jason Tschohl
 * @version 1.0
 */

package com.jtschohl.androidfirewall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import eu.chainfire.libsuperuser.Shell;

/**
 * Contains shared programming interfaces. All iptables "communication" is
 * handled by this class.
 */
public final class Api {

	/** tag for logcat */
	public static final String TAG = "{AF}";

	/** special application UID used to indicate "Any application" */
	public static final int SPECIAL_UID_ANY = -10;
	/** special application UID used to indicate the Linux Kernel */
	public static final int SPECIAL_UID_KERNEL = -11;

	// Preferences
	public static String PREFS_NAME = "AndroidFirewallPrefs";
	public static String PREF_PROFILE = "DefaultProfile";
	public static String PREF_PROFILE1 = "Profile1";
	public static String PREF_PROFILE2 = "Profile2";
	public static String PREF_PROFILE3 = "Profile3";
	public static String PREF_PROFILE4 = "Profile4";
	public static String PREF_PROFILE5 = "Profile5";
	public static String PREF_PROFILES = "ProfileChosen";
	public static final String PREF_3G_UIDS = "AllowedUids3G";
	public static final String PREF_WIFI_UIDS = "AllowedUidsWifi";
	public static final String PREF_ROAMING_UIDS = "AllowedUidsRoaming";
	public static final String PREF_VPN_UIDS = "AllowsUidsVPN";
	public static final String PREF_LAN_UIDS = "AllowedUidsLAN";
	public static final String PREF_INPUT_WIFI_UIDS = "AllowedUidsInputWifi";
	public static final String PREF_PASSWORD = "Password";
	public static final String PREF_CUSTOMSCRIPT = "CustomScript";
	public static final String PREF_CUSTOMSCRIPT2 = "CustomScript2"; // Executed
																		// on
																		// shutdown
	public static final String PREF_MODE = "BlockMode";
	public static final String PREF_ENABLED = "Enabled";
	public static final String PREF_VPNENABLED = "VpnEnabled";
	public static final String PREF_ROAMENABLED = "RoamingEnabled";
	public static final String PREF_LOGENABLED = "LogEnabled";
	public static final String PREF_IP6TABLES = "IPv6Enabled";
	public static final String PREF_REFRESH = "Enabled";
	public static final String PREF_EXPORTNAME = "ExportName";
	public static final String PREF_NOTIFY = "NotifyEnabled";
	public static final String PREF_TASKERNOTIFY = "TaskerNotifyEnabled";
//	public static final String PREF_SDCARD = "SDCard";
	public static final String PREF_LANENABLED = "LanEnabled";
	public static final String PREF_AUTORULES = "AutoRulesEnabled";
	public static final String PREF_TETHER = "TetheringEnabled";
	public static String PREF_LOGTARGET = "";
	public static final String PREF_MULTIUSER = "MultiuserEnabled";
	public static final String PREF_INPUTENABLED = "InputEnabled";
	public static final String PREF_LOGACCEPTENABLED = "LogAcceptEnabled";
	public static final String PREF_APPCOLOR = "AppColor";

	// Modes
	public static final String MODE_WHITELIST = "whitelist";
	public static final String MODE_BLACKLIST = "blacklist";

	// Profiles
	public static final String PROFILE = "default";
	public static final String PROFILE1 = "profile1";
	public static final String PROFILE2 = "profile2";
	public static final String PROFILE3 = "profile3";
	public static final String PROFILE4 = "profile4";
	public static final String PROFILE5 = "profile5";

	// Messages
	public static final String STATUS_CHANGED_MSG = "com.jtschohl.androidfirewall.intent.action.STATUS_CHANGED";
	public static final String TOGGLE_REQUEST_MSG = "com.jtschohl.androidfirewall.intent.action.TOGGLE_REQUEST";
	public static final String CUSTOM_SCRIPT_MSG = "com.jtschohl.androidfirewall.intent.action.CUSTOM_SCRIPT";
	// Message extras (parameters)
	public static final String STATUS_EXTRA = "com.jtschohl.androidfirewall.intent.extra.STATUS";
	public static final String SCRIPT_EXTRA = "com.jtschohl.androidfirewall.intent.extra.SCRIPT";
	public static final String SCRIPT2_EXTRA = "com.jtschohl.androidfirewall.intent.extra.SCRIPT2";
	public static final String EXPORT_EXTRA = "com.jtschohl.androidfirewall.intent.extra.EXPORT";

	private static final String ITFS_WIFI[] = InterfaceTracker.ITFS_WIFI;
	private static final String ITFS_3G[] = InterfaceTracker.ITFS_3G;
	private static final String ITFS_VPN[] = InterfaceTracker.ITFS_VPN;
	private static final String ITFS_TETHER[] = InterfaceTracker.ITFS_TETHER;

	public static String dmesgCommand = "";
	public static String nflogCommand = "";
	public static String logstring = "";
	public static boolean rejectlog;

	// Cached applications
	public static List<DroidApp> applications = null;

	/**
	 * Display a simple alert box
	 * 
	 * @param ctx
	 *            context
	 * @param msg
	 *            message
	 */
	public static void alert(Context ctx, CharSequence msg) {
		if (ctx != null) {
			Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Create the generic shell script header used to determine which iptables
	 * binary to use.
	 * 
	 * @param ctx
	 *            context
	 * @return script header
	 */
	private static String scriptHeader(Context ctx) {
		final String dir = ctx.getDir("bin", 0).getAbsolutePath();
		String arch = System.getProperty("os.arch");
		String myiptables = null;
		final String app_iptables = dir + "/iptables_armv5";
		final String ipv4 = "iptables ";
		int version = Build.VERSION.SDK_INT;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			myiptables = ipv4;
			Log.d(TAG, "Using system iptables because Android is 4.x "
					+ version + " " + arch);
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
			myiptables = app_iptables;
			Log.d(TAG,
					"Using included iptables because Android is 3.x or lower "
							+ version + " " + arch);
		}

		return "" + "IPTABLES=iptables\n" + "IP6TABLES=ip6tables\n"
				+ "BUSYBOX=busybox\n" + "GREP=grep\n" + "ECHO=echo\n"
				+ "# Try to find busybox\n" + "if "
				+ dir
				+ "/busybox_g1 --help >/dev/null 2>/dev/null ; then\n"
				+ "	BUSYBOX="
				+ dir
				+ "/busybox_g1\n"
				+ "	GREP=\"$BUSYBOX grep\"\n"
				+ "	ECHO=\"$BUSYBOX echo\"\n"
				+ "elif busybox --help >/dev/null 2>/dev/null ; then\n"
				+ "	BUSYBOX=busybox\n"
				+ "elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then\n"
				+ "	BUSYBOX=/system/xbin/busybox\n"
				+ "elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then\n"
				+ "	BUSYBOX=/system/bin/busybox\n"
				+ "fi\n"
				+ "# Try to find grep\n"
				+ "if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n"
				+ "	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then\n"
				+ "		GREP=\"$BUSYBOX grep\"\n"
				+ "	fi\n"
				+ "	# Grep is absolutely required\n"
				+ "	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n"
				+ "		$ECHO The grep command is required. Android Firewall will not work.\n"
				+ "		exit 1\n"
				+ "	fi\n"
				+ "fi\n"
				+ "# Try to find iptables\n"
				+ "if "
				+ myiptables
				+ " --version >/dev/null 2>/dev/null ; then\n"
				+ "	IPTABLES="
				+ myiptables + "\n" + "fi\n" + "";
	}

	/**
	 * Copies a raw resource file, given its ID to the given location
	 * 
	 * @param ctx
	 *            context
	 * @param resid
	 *            resource id
	 * @param file
	 *            destination file
	 * @param mode
	 *            file permissions (E.g.: "755")
	 * @throws IOException
	 *             on error
	 * @throws InterruptedException
	 *             when interrupted
	 */
	private static void copyRawFile(Context ctx, int resid, File file,
			String mode) throws IOException, InterruptedException {
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
	}

	/**
	 * Purge and re-add all rules (internal implementation).
	 * 
	 * @param ctx
	 *            application context (mandatory)
	 * @param uidsWifi
	 *            list of selected UIDs for WIFI to allow or disallow (depending
	 *            on the working mode)
	 * @param uids3g
	 *            list of selected UIDs for 2G/3G to allow or disallow
	 *            (depending on the working mode)
	 * @param uids_input_wifi
	 * @param showErrors
	 *            indicates if errors should be alerted
	 * 
	 *            Many thanks to Ventz for his independent work with the VPN
	 *            rules and figuring out how to get the VPN functionality he
	 *            wanted and then forwarding the rules to me to implement in the
	 *            app. Thank you sir, many times over!
	 * 
	 */

	private static boolean applyIptablesRulesImpl(Context ctx,
			List<Integer> uidsWifi, List<Integer> uids3g,
			List<Integer> uidsroaming, List<Integer> uidsvpn,
			List<Integer> uidslan, List<Integer> uidsinputwifi,
			boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		assertBinaries(ctx, showErrors);

		final InterfaceInfo config = InterfaceTracker.getCurrentCfg(ctx);
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		final boolean whitelist = prefs.getString(PREF_MODE, MODE_WHITELIST)
				.equals(MODE_WHITELIST);
		final boolean blacklist = !whitelist;
		final boolean logenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_LOGENABLED, false);
		final boolean vpnenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_VPNENABLED, false);
		final boolean lanenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_LANENABLED, false);
		final boolean roamenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_ROAMENABLED, false);
		final boolean ipv6enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_IP6TABLES, false);
		final boolean enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_ENABLED, false);
		final boolean tetherenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_TETHER, false);
		final String logtarget = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getString(PREF_LOGTARGET, "");
		final boolean inputenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_INPUTENABLED, false);
		final boolean logacceptenabled = ctx
				.getSharedPreferences(PREFS_NAME, 0).getBoolean(
						PREF_LOGACCEPTENABLED, false);
		final String customScript = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getString(Api.PREF_CUSTOMSCRIPT, "");
		SharedPreferences prefs2 = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		final String chainName = prefs2.getString("chainName", "");

		final StringBuilder script = new StringBuilder();
		Log.d(TAG, chainName);
		try {
			int code;
			script.append(scriptHeader(ctx));
			script.append("" + "dmesg -c >/dev/null || exit\n"
					+ "$IPTABLES --version || exit 1\n"
					+ "# Create the droidwall chains if necessary\n"
					+ "$IPTABLES -L "
					+ chainName
					+ " >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ " || exit 3\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-3g >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-3g || exit 4\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-wifi >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-wifi || exit 5\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-reject >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-reject || exit 6\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-accept >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-accept || exit 6\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-input-accept >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-input-accept || exit 6\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-input-drop >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-input-drop || exit 6\n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-vpn >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-vpn || exit 7 \n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-lan >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-lan || exit 7 \n"
					+ "$IPTABLES -L "
					+ chainName
					+ "-input >/dev/null 2>/dev/null || $IPTABLES --new "
					+ chainName
					+ "-input || exit 501\n"
					+ "# Add droidwall chain to OUTPUT chain if necessary\n"
					+ "$IPTABLES -L OUTPUT | $GREP -q "
					+ chainName
					+ " || $IPTABLES -A OUTPUT -j "
					+ chainName
					+ " || exit 11\n"
					+ "# Add droidwall-input chain to INPUT chain if necessary\n"
					+ "$IPTABLES -L INPUT | $GREP -q "
					+ chainName
					+ "-input || $IPTABLES -A INPUT -j "
					+ chainName
					+ "-input || exit 1111\n"
					+ "# Flush existing rules\n"
					+ "$IPTABLES -F "
					+ chainName
					+ " || exit 17\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-3g || exit 18\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-wifi || exit 19\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-reject || exit 20\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-vpn || exit 20\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-lan || exit 20\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-input || exit 503\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-input-drop || exit 503\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-accept || exit 503\n"
					+ "$IPTABLES -F "
					+ chainName
					+ "-input-accept || exit 503\n"
					+ "# Create reject rule and fix for WiFi slow DNS lookups\n"
					+ "$IPTABLES -A "
					+ chainName
					+ " -m owner --uid-owner 0 -p udp --dport 53 -j RETURN || exit 22\n"
					+ "$IPTABLES -A "
					+ chainName
					+ " -m owner --uid-owner 1000 -p udp --dport 123 -j RETURN || exit 22\n"
					+ "$IPTABLES -D INPUT -j "
					+ chainName
					+ "-input || exit 507\n"
					+ "$IPTABLES -I INPUT 1 -j "
					+ chainName
					+ "-input || exit 508\n"
					+ "$IPTABLES -D OUTPUT -j "
					+ chainName
					+ " || exit 11\n"
					+ "$IPTABLES -I OUTPUT 1 -j "
					+ chainName
					+ " || exit 12\n"
					+ "");
			// Check if logging is enabled
			if (logenabled) {
				if (logtarget.equals("LOG")) {
					script.append(""
							+ "$IPTABLES -A "
							+ chainName
							+ "-reject -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewall]\" --log-level 4 --log-uid || exit 299\n"
							+ "$IPTABLES -A " + chainName
							+ "-reject -j REJECT || exit 29\n" + "");
					if (inputenabled) {
						script.append(""
								+ "$IPTABLES -A "
								+ chainName
								+ "-input-drop -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewall]\" --log-level 4 --log-uid || exit 510\n"
								+ "");
						script.append(""
								+ "# Create the input drop rule (log disabled)\n"
								+ "$IPTABLES -A " + chainName
								+ "-input-drop -j REJECT || exit 30\n" + "");
						Log.d(TAG, "LOG code " + logtarget);
					}
				} else if (logtarget.equals("NFLOG")) {
					script.append(""
							+ "$IPTABLES -A "
							+ chainName
							+ "-reject -j NFLOG --nflog-prefix \"[AndroidFirewall]\" --nflog-group 0 || exit 2999\n"
							+ "$IPTABLES -A " + chainName
							+ "-reject -j REJECT || exit 29\n" + "");
					if (inputenabled) {
						script.append(""
								+ "$IPTABLES -A "
								+ chainName
								+ "-input-drop -j NFLOG --nflog-prefix \"[AndroidFirewall]\" --nflog-group 0 || exit 510\n"
								+ "");
						script.append(""
								+ "# Create the input drop rule (log disabled)\n"
								+ "$IPTABLES -A " + chainName
								+ "-input-drop -j REJECT || exit 30\n" + "");
					}
					Log.d(TAG, "NFLOG code " + logtarget);
				} else {
					script.append("" + "$IPTABLES -A " + chainName
							+ "-reject -j REJECT || exit 30\n" + "");
					if (inputenabled) {
						script.append(""
								+ "# Create the input drop rule (log disabled)\n"
								+ "$IPTABLES -A " + chainName
								+ "-input-drop -j REJECT || exit 30\n" + "");
					}
				}
			} else {
				script.append("" + "# Create the reject rule (log disabled)\n"
						+ "$IPTABLES -A " + chainName
						+ "-reject -j REJECT || exit 30\n" + "");
				if (inputenabled) {
					script.append(""
							+ "# Create the input drop rule (log disabled)\n"
							+ "$IPTABLES -A " + chainName
							+ "-input-drop -j REJECT || exit 30\n" + "");
				}
			}
			if (logacceptenabled) {
				if (logtarget.equals("LOG")) {
					script.append(""
							+ "$IPTABLES -A "
							+ chainName
							+ "-accept -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewallAccept]\" --log-level 4 --log-uid || exit 299\n"
							+ "$IPTABLES -A " + chainName
							+ "-accept -j RETURN || exit 29\n" + "");
					if (inputenabled) {
						script.append(""
								+ "$IPTABLES -A "
								+ chainName
								+ "-input-accept -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewallAccept]\" --log-level 4 --log-uid || exit 510\n"
								+ "$IPTABLES -A " + chainName
								+ "-input-accept -j RETURN || exit 29\n" + "");
						Log.d(TAG, "LOG code " + logtarget);
					}
				} else if (logtarget.equals("NFLOG")) {
					script.append(""
							+ "$IPTABLES -A "
							+ chainName
							+ " -j NFLOG --nflog-prefix \"[AndroidFirewallAccept]\" --nflog-group 0 || exit 2999\n"
							+ "$IPTABLES -A " + chainName
							+ "-accept -j RETURN || exit 29\n" + "");
					if (inputenabled) {
						script.append(""
								+ "$IPTABLES -A "
								+ chainName
								+ "-input -j NFLOG --nflog-prefix \"[AndroidFirewallAccept]\" --nflog-group 0 || exit 510\n"
								+ "$IPTABLES -A " + chainName
								+ "-input-accept -j RETURN || exit 29\n" + "");
					}
					Log.d(TAG, "NFLOG code " + logtarget);
				}
			}
			if (tetherenabled) {
				script.append("" + "# Create the tethering rules\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 0 -p udp --sport=67 --dport=68 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 0 -p udp --sport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 0 -p tcp --sport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 0 -p udp --dport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 0 -p tcp --dport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 9999 -p udp --sport=67 --dport=68 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 9999 -p udp --sport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 9999 -p tcp --sport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 9999 -p udp --dport=53 -j RETURN || exit 222\n"
						+ "$IPTABLES -A "
						+ chainName
						+ " -m owner --uid-owner 9999 -p tcp --dport=53 -j RETURN || exit 222\n"
						+ "");
			}
			if (customScript.length() > 0) {
				script.append("\n# BEGIN OF CUSTOM SCRIPT (user-defined)\n");
				script.append(customScript);
				script.append("\n# END OF CUSTOM SCRIPT (user-defined)\n\n");
			}
			script.append("# Main rules (per interface)\n");
			for (final String itf : ITFS_3G) {
				script.append("$IPTABLES -A " + chainName + " -o ").append(itf)
						.append(" -j " + chainName + "-3g || exit 32\n");
			}
			for (final String itf : ITFS_WIFI) {
				if (lanenabled) {
					if (!config.lanipv4.equals("")) {
						script.append("$IPTABLES -A " + chainName + " -d ")
								.append(config.lanipv4)
								.append(" -o ")
								.append(itf)
								.append(" -j " + chainName
										+ "-lan || exit 34\n");
						script.append("$IPTABLES -A " + chainName + " '!' -d ")
								.append(config.lanipv4)
								.append(" -o ")
								.append(itf)
								.append(" -j " + chainName
										+ "-wifi || exit 34\n");
					} else {
						// for preventing leaks after device connects to WiFi
						// When a device gets an ip the intent needs to fire so
						// block connection until Auto rules fire.
						script.append("$IPTABLES -A " + chainName
								+ "-wifi -j REJECT || exit 344\n");
					}
				} else {
					script.append("$IPTABLES -A " + chainName + " -o ")
							.append(itf)
							.append(" -j " + chainName + "-wifi || exit 34\n");
				}
			}
			if (vpnenabled) {
				for (final String itf : ITFS_VPN) {
					script.append("$IPTABLES -A " + chainName + " -o ")
							.append(itf)
							.append(" -j " + chainName + "-vpn || exit 34\n");
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					script.append("$IPTABLES -A " + chainName
							+ " -m mark --mark 0x3c/0xfffc -g " + chainName
							+ "-vpn || exit 4060\n");
					script.append("$IPTABLES -A " + chainName
							+ " -m mark --mark 0x40/0xfff8 -g " + chainName
							+ "-vpn || exit 4061\n");
				}
			}
			if (tetherenabled) {
				for (final String itf : ITFS_TETHER) {
					script.append("$IPTABLES -A " + chainName + " -o ")
							.append(itf).append(" -j RETURN || exit 32\n");
				}
			}
			script.append("# Filtering rules\n");
			final String targetRule = (whitelist ? "RETURN" : "" + chainName
					+ "-reject");
			final boolean any_3g = uids3g.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_wifi = uidsWifi.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_vpn = uidsvpn.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_lan = uidslan.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_inputwifi = uidsinputwifi
					.indexOf(SPECIAL_UID_ANY) >= 0;
			if (whitelist && !any_wifi) {
				// When "white listing" wifi, we need to ensure that the dhcp
				// and wifi users are allowed
				int uid = android.os.Process.getUidForName("dhcp");
				if (uid != -1) {
					script.append("# dhcp user\n");
					script.append(
							"$IPTABLES -A " + chainName
									+ "-wifi -m owner --uid-owner ")
							.append(uid).append(" -j RETURN || exit 36\n");
				}
				uid = android.os.Process.getUidForName("wifi");
				if (uid != -1) {
					script.append("# wifi user\n");
					script.append(
							"$IPTABLES -A " + chainName
									+ "-wifi -m owner --uid-owner ")
							.append(uid).append(" -j RETURN || exit 38\n");
				}
			}
			if (any_3g) {
				if (blacklist) {
					/* block any application on this interface */
					script.append("$IPTABLES -A " + chainName + "-3g -j ")
							.append(targetRule).append(" || exit 40\n");
				} else {
					for (final Integer uid : uids3g) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I " + chainName
											+ "-3g -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
			} else {
				/* release/block individual applications on this interface */
				if (isRoaming(ctx) && roamenabled) {
					for (final Integer uid : uidsroaming) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I " + chainName
											+ "-3g -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 50\n");
					}
				} else {
					for (final Integer uid : uids3g) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I " + chainName
											+ "-3g -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
			}
			script.append("$IPTABLES -I " + chainName
					+ "-3g -m owner --uid-owner 9999 -j RETURN || exit 9999\n");
			if (any_wifi) {
				if (blacklist) {
					/* block any application on this interface */
					script.append("$IPTABLES -A " + chainName + "-wifi -j ")
							.append(targetRule).append(" || exit 44\n");
				} else {
					/* release/block individual applications on this interface */
					for (final Integer uid : uidsWifi) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -A " + chainName
											+ "-wifi -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 46\n");
					}
				}
			} else {
				/* release/block individual applications on this interface */
				for (final Integer uid : uidsWifi) {
					if (uid >= 0)
						script.append(
								"$IPTABLES -A " + chainName
										+ "-wifi -m owner --uid-owner ")
								.append(uid).append(" -j ").append(targetRule)
								.append(" || exit 46\n");
				}
			}
			if (whitelist) {
				if (!any_3g) {
					if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to allow kernel packets on white-list\n");
						if (logacceptenabled) {
							script.append("$IPTABLES -A " + chainName
									+ " -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-accept || exit 54\n");
						}
						script.append("$IPTABLES -A " + chainName
								+ "-3g -m owner --uid-owner 0:999999999 -j "
								+ chainName + "-reject || exit 48\n");
					} else {
						if (logacceptenabled) {
							script.append("$IPTABLES -A " + chainName + " -j "
									+ chainName + "-accept || exit 54\n");
						}
						script.append("$IPTABLES -A " + chainName + "-3g -j "
								+ chainName + "-reject || exit 50\n");
					}
				}
				if (!any_wifi) {
					if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to allow kernel packets on white-list\n");
						if (logacceptenabled) {
							script.append("$IPTABLES -A " + chainName
									+ " -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-accept || exit 54\n");
						}
						script.append("$IPTABLES -A " + chainName
								+ "-wifi -m owner --uid-owner 0:999999999 -j "
								+ chainName + "-reject || exit 52\n");
					} else {
						if (logacceptenabled) {
							script.append("$IPTABLES -A " + chainName + " -j "
									+ chainName + "-accept || exit 54\n");
						}
						script.append("$IPTABLES -A " + chainName + "-wifi -j "
								+ chainName + "-reject || exit 54\n");
					}
				}
			} else {
				if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
					script.append("# hack to BLOCK kernel packets on black-list\n");
					script.append("$IPTABLES -A "
							+ chainName
							+ "-3g -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
					if (logacceptenabled) {
						script.append("$IPTABLES -A " + chainName + " -j "
								+ chainName + "-accept || exit 54\n");
					}
					script.append("$IPTABLES -A " + chainName + "-3g -j "
							+ chainName + "-reject || exit 57\n");
				}
				if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
					script.append("# hack to BLOCK kernel packets on black-list\n");
					script.append("$IPTABLES -A "
							+ chainName
							+ "-wifi -m owner --uid-owner 0:999999999 -j RETURN || exit 60\n");
					if (logacceptenabled) {
						script.append("$IPTABLES -A " + chainName + " -j "
								+ chainName + "-accept || exit 54\n");
					}
					script.append("$IPTABLES -A " + chainName + "-wifi -j "
							+ chainName + "-reject || exit 61\n");
				}
			}
			if (vpnenabled) {
				if (any_vpn && vpnenabled) {
					if (blacklist) {
						/* block any application on this interface */
						script.append("$IPTABLES -A " + chainName + "-vpn -j ")
								.append(targetRule).append(" || exit 40\n");
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidsvpn) {
							if (uid >= 0)
								script.append(
										"$IPTABLES -I " + chainName
												+ "-vpn -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 42\n");
						}
					}
				} else {
					/* release/block individual applications on this interface */
					for (final Integer uid : uidsvpn) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I " + chainName
											+ "-vpn -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
				if (whitelist && vpnenabled) {
					if (!any_vpn) {
						if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IPTABLES -A "
									+ chainName
									+ "-vpn -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-reject || exit 48\n");
						} else {
							script.append("$IPTABLES -A " + chainName
									+ "-vpn -j " + chainName
									+ "-reject || exit 50\n");
						}
					} else {
						script.append("$IPTABLES -A " + chainName + "-vpn -j "
								+ chainName + "-reject || exit 54\n");
					}
				} else {
					if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IPTABLES -A "
								+ chainName
								+ "-vpn -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
						script.append("$IPTABLES -A " + chainName + "-vpn -j "
								+ chainName + "-reject || exit 57\n");
					}
				}
			}
			if (lanenabled) {
				if (any_lan && lanenabled) {
					if (blacklist) {
						/* block any application on this interface */
						script.append("$IPTABLES -A " + chainName + "-lan -j ")
								.append(targetRule).append(" || exit 40\n");
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidslan) {
							if (uid >= 0)
								script.append(
										"$IPTABLES -I " + chainName
												+ "-lan -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 42\n");
						}
					}
				} else {
					/* release/block individual applications on this interface */
					for (final Integer uid : uidslan) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I " + chainName
											+ "-lan -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
				if (whitelist && lanenabled) {
					if (!any_lan) {
						if (uidslan.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IPTABLES -A "
									+ chainName
									+ "-lan -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-reject || exit 48\n");
						} else {
							script.append("$IPTABLES -A " + chainName
									+ "-lan -j " + chainName
									+ "-reject || exit 50\n");
						}
					} else {
						script.append("$IPTABLES -A " + chainName + "-lan -j "
								+ chainName + "-reject || exit 54\n");
					}
				} else {
					if (uidslan.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IPTABLES -A "
								+ chainName
								+ "-lan -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
						script.append("$IPTABLES -A " + chainName + "-lan -j "
								+ chainName + "-reject || exit 57\n");
					}
				}
			}
			if (inputenabled) {
				if (any_inputwifi && inputenabled) {
					if (blacklist) {
						/* block any application on this interface */
						script.append(
								"$IPTABLES -A " + chainName + "-input -j ")
								.append(targetRule).append(" || exit 40\n");
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidsinputwifi) {
							if (uid >= 0)
								script.append(
										"$IPTABLES -I "
												+ chainName
												+ "-input -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 42\n");
						}
					}
				} else {
					/* release/block individual applications on this interface */
					for (final Integer uid : uidsinputwifi) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I " + chainName
											+ "-input -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
				if (whitelist && inputenabled) {
					if (!any_inputwifi) {
						if (uidsinputwifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IPTABLES -A "
									+ chainName
									+ "-input -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-input-drop || exit 48\n");
							if (logacceptenabled) {
								script.append("$IPTABLES -A "
										+ chainName
										+ "-input -m owner --uid-owner 0:999999999 -j "
										+ chainName
										+ "-input-accept || exit 54\n");
							}
						} else {
							script.append("$IPTABLES -A " + chainName
									+ "-input -j " + chainName
									+ "-input-drop || exit 50\n");
							if (logacceptenabled) {
								script.append("$IPTABLES -A " + chainName
										+ "-input -j " + chainName
										+ "-input-accept || exit 54\n");
							}
						}
					} else {
						script.append("$IPTABLES -A " + chainName
								+ "-input-wifi -j " + chainName
								+ "-input-drop || exit 54\n");
						if (logacceptenabled) {
							script.append("$IPTABLES -A " + chainName
									+ "-input -j " + chainName
									+ "-input-accept || exit 54\n");
						}
					}
				} else {
					if (uidsinputwifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IPTABLES -A "
								+ chainName
								+ "-input -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
						script.append("$IPTABLES -A " + chainName
								+ "-input -j " + chainName
								+ "-input-drop || exit 57\n");
						if (logacceptenabled) {
							script.append("$IPTABLES -A " + chainName
									+ "-input -j " + chainName
									+ "-input-accept || exit 54\n");
						}
					}
				}
				script.append("$IPTABLES -I "
						+ chainName
						+ "-input -m owner --uid-owner 0 -j RETURN || exit 4444\n");
				script.append("$IPTABLES -I "
						+ chainName
						+ "-input -m state --state ESTABLISHED,RELATED -j ACCEPT || exit 4455\n");
			}
			if (ipv6enabled) {
				{
					script.append(scriptHeader(ctx));
					script.append("" + "$IP6TABLES --version || exit 60\n"
							+ "# Create the droidwall chains if necessary\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ " >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ " || exit 3\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-input >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-input || exit 500\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-input-accept >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-input-accept || exit 500\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-accept >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-accept || exit 500\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-3g >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-3g || exit 4\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-wifi >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-wifi || exit 5\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-reject >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-reject || exit 6\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-input-drop >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-input-drop || exit 6\n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-vpn >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-vpn || exit 7 \n"
							+ "$IP6TABLES -L "
							+ chainName
							+ "-lan >/dev/null 2>/dev/null || $IP6TABLES --new "
							+ chainName
							+ "-lan || exit 7 \n"
							+ "# Add droidwall chain to OUTPUT chain if necessary\n"
							+ "$IP6TABLES -L OUTPUT | $GREP -q "
							+ chainName
							+ " || $IP6TABLES -A OUTPUT -j "
							+ chainName
							+ " || exit 11\n"
							+ "# Add droidwall-input chain to INPUT chain if necessary\n"
							+ "$IP6TABLES -L INPUT | $GREP -q "
							+ chainName
							+ "-input || $IP6TABLES -A INPUT -j "
							+ chainName
							+ "-input || exit 1111\n"
							+ "# Flush existing rules\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ " || exit 17\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-3g || exit 18\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-wifi || exit 19\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-reject || exit 20\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-input-drop || exit 20\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-vpn || exit 20\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-lan || exit 20\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-input || exit 502\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-input-accept || exit 20\n"
							+ "$IP6TABLES -F "
							+ chainName
							+ "-accept || exit 20\n"
							+ "# Create reject rule and fix for WiFi slow DNS lookups"
							+ "$IP6TABLES -A "
							+ chainName
							+ "-reject -j REJECT || exit 21\n"
							+ "$IP6TABLES -A "
							+ chainName
							+ " -m owner --uid-owner 0 -p udp --dport 53 -j RETURN || exit 22\n"
							+ "$IP6TABLES -A "
							+ chainName
							+ " -m owner --uid-owner 1000 -p udp --dport 123 -j RETURN || exit 22\n"
							+ "$IP6TABLES -D INPUT -j "
							+ chainName
							+ "-input || exit 507\n"
							+ "$IP6TABLES -I INPUT 1 -j "
							+ chainName
							+ "-input || exit 508\n"
							+ "$IP6TABLES -D OUTPUT -j "
							+ chainName
							+ " || exit 11\n"
							+ "$IP6TABLES -I OUTPUT 1 -j "
							+ chainName + " || exit 12\n" + "");
					// Check if logging is enabled
					if (logenabled && ipv6enabled) {
						if (logtarget.equals("LOG")) {
							script.append(""
									+ "# Create the log and reject rules (ignore errors on the LOG target just in case it is not available)\n"
									+ "$IP6TABLES -A "
									+ chainName
									+ "-reject -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewall]\" --log-level 4 --log-uid || exit 3000\n"
									+ "$IP6TABLES -A " + chainName
									+ "-reject -j REJECT || exit 29\n" + "");
							if (inputenabled) {
								script.append(""
										+ "$IP6TABLES -A "
										+ chainName
										+ "-input-drop -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewall]\" --log-level 4 --log-uid || exit 610\n"
										+ "");
								script.append(""
										+ "# Create the input drop rule (log disabled)\n"
										+ "$IP6TABLES -A " + chainName
										+ "-input-drop -j REJECT || exit 30\n"
										+ "");
							}
						} else if (logtarget.equals("NFLOG")) {
							script.append(""
									+ "# Create the log and reject rules (ignore errors on the LOG target just in case it is not available)\n"
									+ "$IP6TABLES -A "
									+ chainName
									+ "-reject -j NFLOG --nflog-prefix \"[AndroidFirewall]\" --nflog-group 0 || exit 3001\n"
									+ "$IP6TABLES -A " + chainName
									+ "-reject -j REJECT || exit 29\n" + "");
							if (inputenabled) {
								script.append(""
										+ "$IP6TABLES -A "
										+ chainName
										+ "-input-drop -j NFLOG --nflog-prefix \"[AndroidFirewall]\" --nflog-group 0 || exit 610\n"
										+ "");
								script.append(""
										+ "# Create the input drop rule (log disabled)\n"
										+ "$IP6TABLES -A " + chainName
										+ "-input-drop -j REJECT || exit 30\n"
										+ "");
							}
						} else {
							script.append(""
									+ "# Create the reject rule (issue getting LOG/NFLOG)\n"
									+ "$IP6TABLES -A " + chainName
									+ "-reject -j REJECT || exit 30\n" + "");
							if (inputenabled) {
								script.append(""
										+ "# Create the input drop rule (log disabled)\n"
										+ "$IP6TABLES -A " + chainName
										+ "-input-drop -j REJECT || exit 30\n"
										+ "");
							}
						}
					} else {
						script.append(""
								+ "# Create the reject rule (log disabled)\n"
								+ "$IP6TABLES -A " + chainName
								+ "-reject -j REJECT || exit 77\n" + "");
						if (inputenabled) {
							script.append(""
									+ "# Create the input drop rule (log disabled)\n"
									+ "$IP6TABLES -A " + chainName
									+ "-input-drop -j REJECT || exit 30\n" + "");
						}
					}
					if (logacceptenabled && ipv6enabled) {
						if (logtarget.equals("LOG")) {
							script.append(""
									+ "$IP6TABLES -A "
									+ chainName
									+ "-accept -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewallAccept]\" --log-level 4 --log-uid || exit 299\n"
									+ "$IP6TABLES -A " + chainName
									+ "-accept -j RETURN || exit 29\n" + "");
							if (inputenabled) {
								script.append(""
										+ "$IP6TABLES -A "
										+ chainName
										+ "-input-accept -m limit --limit 1000/min -j LOG --log-prefix \"[AndroidFirewallAccept]\" --log-level 4 --log-uid || exit 510\n"
										+ "$IP6TABLES -A "
										+ chainName
										+ "-input-accept -j RETURN || exit 29\n"
										+ "");
								Log.d(TAG, "LOG code " + logtarget);
							}
						} else if (logtarget.equals("NFLOG")) {
							script.append(""
									+ "$IP6TABLES -A "
									+ chainName
									+ "-accept -j NFLOG --nflog-prefix \"[AndroidFirewallAccept]\" --nflog-group 0 || exit 2999\n"
									+ "$IP6TABLES -A " + chainName
									+ "-accept -j RETURN || exit 29\n" + "");
							if (inputenabled) {
								script.append(""
										+ "$IP6TABLES -A "
										+ chainName
										+ "-input-accept -j NFLOG --nflog-prefix \"[AndroidFirewallAccept]\" --nflog-group 0 || exit 510\n"
										+ "$IP6TABLES -A "
										+ chainName
										+ "-input-accept -j RETURN || exit 29\n"
										+ "");
							}
							Log.d(TAG, "NFLOG code " + logtarget);
						}
					}
					if (tetherenabled && ipv6enabled) {
						script.append("" + "# Create the tethering rules\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 0 -p udp --sport=67 --dport=68 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 0 -p udp --sport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 0 -p tcp --sport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 0 -p udp --dport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 0 -p tcp --dport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 9999 -p udp --sport=67 --dport=68 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 9999 -p udp --sport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 9999 -p tcp --sport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 9999 -p udp --dport=53 -j RETURN || exit 222\n"
								+ "$IP6TABLES -A "
								+ chainName
								+ " -m owner --uid-owner 9999 -p tcp --dport=53 -j RETURN || exit 222\n"
								+ "");
					}
					script.append("# Main rules (per interface)\n");
					for (final String itf : ITFS_3G) {
						script.append("$IP6TABLES -A " + chainName + " -o ")
								.append(itf)
								.append(" -j " + chainName + "-3g || exit 78\n");

					}
					for (final String itf : ITFS_WIFI) {
						if (lanenabled && ipv6enabled) {
							if (!config.lanipv6.equals("")) {
								script.append(
										"$IP6TABLES -A " + chainName + " -d ")
										.append(config.lanipv6)
										.append(" -o ")
										.append(itf)
										.append(" -j " + chainName
												+ "-lan || exit 34\n");
								script.append(
										"$IP6TABLES -A " + chainName
												+ " '!' -d ")
										.append(config.lanipv6)
										.append(" -o ")
										.append(itf)
										.append(" -j " + chainName
												+ "-wifi || exit 34\n");
							} else {
								// for preventing leaks after device connects to
								// WiFi again.
								// When a device gets an ip the intent needs to
								// fire so
								// block connection until Auto rules fire.
								script.append("$IP6TABLES -A " + chainName
										+ "-wifi -j REJECT || exit 345\n");
							}
						} else {
							script.append("$IP6TABLES -A " + chainName + " -o ")
									.append(itf)
									.append(" -j " + chainName
											+ "-wifi || exit 34\n");
						}
					}
					for (final String itf : ITFS_VPN) {
						script.append("$IP6TABLES -A " + chainName + " -o ")
								.append(itf)
								.append(" -j " + chainName
										+ "-vpn || exit 79\n");
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
							script.append("$IP6TABLES -A " + chainName
									+ " -m mark --mark 0x3c/0xfffc -g "
									+ chainName + "-vpn || exit 4060\n");
							script.append("$IP6TABLES -A " + chainName
									+ " -m mark --mark 0x40/0xfff8 -g "
									+ chainName + "-vpn || exit 4061\n");
						}
					}
					int uid = android.os.Process.getUidForName("dhcp");
					if (uid != -1) {
						script.append("# dhcp user\n");
						script.append(
								"$IP6TABLES -A " + chainName
										+ "-wifi -m owner --uid-owner ")
								.append(uid).append(" -j RETURN || exit 80\n");

					}
					uid = android.os.Process.getUidForName("wifi");
					if (uid != -1) {
						script.append("# wifi user\n");
						script.append(
								"$IP6TABLES -A " + chainName
										+ "-wifi -m owner --uid-owner ")
								.append(uid).append(" -j RETURN || exit 81\n");

					}
				}
				if (any_3g && ipv6enabled) {
					if (blacklist) {
						// block any application on this interface
						script.append("$IP6TABLES -A " + chainName + "-3g -j ")
								.append(targetRule).append(" || exit 82\n");
					} else {
						for (final Integer uid : uids3g) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I " + chainName
												+ "-3g -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 84\n");
						}
					}
				} else {
					/* release/block individual applications on this interface */
					if (isRoaming(ctx) && ipv6enabled && roamenabled) {
						for (final Integer uid : uidsroaming) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I " + chainName
												+ "-3g -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 83\n");
						}
					} else {
						for (final Integer uid : uids3g) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I " + chainName
												+ "-3g -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 84\n");
						}
					}
				}
				script.append("$IP6TABLES -I "
						+ chainName
						+ "-3g -m owner --uid-owner 9999 -j RETURN || exit 9999\n");
				if (any_wifi && ipv6enabled) {
					if (blacklist) {
						// block any application on this interface
						script.append(
								"$IP6TABLES -A " + chainName + "-wifi -j ")
								.append(targetRule).append(" || exit 85\n");
					} else {
						// release/block individual applications on this
						// interface
						for (final Integer uid : uidsWifi) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -A " + chainName
												+ "-wifi -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 86\n");
						}
					}
				} else {
					// release/block individual applications on this interface
					for (final Integer uid : uidsWifi) {
						if (uid >= 0)
							script.append(
									"$IP6TABLES -A " + chainName
											+ "-wifi -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 86\n");
					}
				}
				if (whitelist && ipv6enabled) {
					if (!any_3g) {
						if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IP6TABLES -A "
									+ chainName
									+ "-3g -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-reject || exit 87\n");
							if (logacceptenabled) {
								script.append("$IP6TABLES -A "
										+ chainName
										+ " -m owner --uid-owner 0:999999999 -j "
										+ chainName + "-accept || exit 54\n");
							}
						} else {
							script.append("$IP6TABLES -A " + chainName
									+ "-3g -j " + chainName
									+ "-reject || exit 88\n");
							if (logacceptenabled) {
								script.append("$IP6TABLES -A " + chainName
										+ " -j " + chainName
										+ "-accept || exit 54\n");
							}
						}
					}
					if (!any_wifi && ipv6enabled) {
						if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IP6TABLES -A "
									+ chainName
									+ "-wifi -m owner --uid-owner 0:999999999 -j "
									+ chainName + "-reject || exit 89\n");
							if (logacceptenabled) {
								script.append("$IP6TABLES -A "
										+ chainName
										+ " -m owner --uid-owner 0:999999999 -j "
										+ chainName + "-accept || exit 54\n");
							}
						} else {
							script.append("$IP6TABLES -A " + chainName
									+ "-wifi -j " + chainName
									+ "-reject || exit 90\n");
							if (logacceptenabled) {
								script.append("$IP6TABLES -A " + chainName
										+ " -j " + chainName
										+ "-accept || exit 54\n");
							}
						}
					}
				} else {
					if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IP6TABLES -A "
								+ chainName
								+ "-3g -m owner --uid-owner 0:999999999 -j RETURN || exit 91\n");
						script.append("$IP6TABLES -A " + chainName + "-3g -j "
								+ chainName + "-reject || exit 92\n");
						if (logacceptenabled) {
							script.append("$IP6TABLES -A " + chainName + " -j "
									+ chainName + "-accept || exit 54\n");
						}
					}
					if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IP6TABLES -A "
								+ chainName
								+ "-wifi -m owner --uid-owner 0:999999999 -j RETURN || exit 93\n");
						script.append("$IP6TABLES -A " + chainName
								+ "-wifi -j " + chainName
								+ "-reject || exit 94\n");
						if (logacceptenabled) {
							script.append("$IP6TABLES -A " + chainName + " -j "
									+ chainName + "-accept || exit 54\n");
						}
					}
				}
				if (vpnenabled && ipv6enabled) {
					if (any_vpn && ipv6enabled) {
						if (blacklist) {
							// block any application on this interface
							script.append(
									"$IP6TABLES -A " + chainName + "-vpn -j ")
									.append(targetRule).append(" || exit 82\n");
						} else {
							/*
							 * release/block individual applications on this
							 * interface
							 */
							for (final Integer uid : uidsvpn) {
								if (uid >= 0)
									script.append(
											"$IP6TABLES -I "
													+ chainName
													+ "-vpn -m owner --uid-owner ")
											.append(uid).append(" -j ")
											.append(targetRule)
											.append(" || exit 84\n");
							}
						}
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidsvpn) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I " + chainName
												+ "-vpn -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 84\n");
						}
					}
					if (whitelist && ipv6enabled && vpnenabled) {
						if (!any_vpn) {
							if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
								script.append("# hack to allow kernel packets on white-list\n");
								script.append("$IP6TABLES -A "
										+ chainName
										+ "-vpn -m owner --uid-owner 0:999999999 -j "
										+ chainName + "-reject || exit 87\n");
							} else {
								script.append("$IP6TABLES -A " + chainName
										+ "-vpn -j " + chainName
										+ "-reject || exit 88\n");
							}
						}
					} else {
						if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to BLOCK kernel packets on black-list\n");
							script.append("$IP6TABLES -A "
									+ chainName
									+ "-vpn -m owner --uid-owner 0:999999999 -j RETURN || exit 91\n");
							script.append("$IP6TABLES -A " + chainName
									+ "-vpn -j " + chainName
									+ "-reject || exit 92\n");
						}
					}
				}
				if (lanenabled && ipv6enabled) {
					if (any_lan && ipv6enabled) {
						if (blacklist) {
							// block any application on this interface
							script.append(
									"$IP6TABLES -A " + chainName + "-lan -j ")
									.append(targetRule).append(" || exit 82\n");
						} else {
							/*
							 * release/block individual applications on this
							 * interface
							 */
							for (final Integer uid : uidslan) {
								if (uid >= 0)
									script.append(
											"$IP6TABLES -I "
													+ chainName
													+ "-lan -m owner --uid-owner ")
											.append(uid).append(" -j ")
											.append(targetRule)
											.append(" || exit 84\n");
							}
						}
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidslan) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I " + chainName
												+ "-lan -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 84\n");
						}
					}
					if (whitelist && ipv6enabled && lanenabled) {
						if (!any_lan) {
							if (uidslan.indexOf(SPECIAL_UID_KERNEL) >= 0) {
								script.append("# hack to allow kernel packets on white-list\n");
								script.append("$IP6TABLES -A "
										+ chainName
										+ "-lan -m owner --uid-owner 0:999999999 -j "
										+ chainName + "-reject || exit 87\n");
							} else {
								script.append("$IP6TABLES -A " + chainName
										+ "-lan -j " + chainName
										+ "-reject || exit 88\n");
							}
						}
					} else {
						if (uidslan.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to BLOCK kernel packets on black-list\n");
							script.append("$IP6TABLES -A "
									+ chainName
									+ "-lan -m owner --uid-owner 0:999999999 -j RETURN || exit 91\n");
							script.append("$IP6TABLES -A " + chainName
									+ "-lan -j " + chainName
									+ "-reject || exit 92\n");
						}
					}
				}
				if (inputenabled && ipv6enabled) {
					if (any_inputwifi && inputenabled) {
						if (blacklist) {
							/* block any application on this interface */
							script.append(
									"$IP6TABLES -A " + chainName + "-input -j ")
									.append(targetRule).append(" || exit 40\n");
						} else {
							/*
							 * release/block individual applications on this
							 * interface
							 */
							for (final Integer uid : uidsinputwifi) {
								if (uid >= 0)
									script.append(
											"$IP6TABLES -I "
													+ chainName
													+ "-input -m owner --uid-owner ")
											.append(uid).append(" -j ")
											.append(targetRule)
											.append(" || exit 42\n");
							}
						}
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidsinputwifi) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I "
												+ chainName
												+ "-input -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 42\n");
						}
					}
					if (whitelist && inputenabled) {
						if (!any_inputwifi) {
							if (uidsinputwifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
								script.append("# hack to allow kernel packets on white-list\n");
								script.append("$IP6TABLES -A "
										+ chainName
										+ "-input -m owner --uid-owner 0:999999999 -j "
										+ chainName
										+ "-input-drop || exit 48\n");
								if (logacceptenabled) {
									script.append("$IP6TABLES -A "
											+ chainName
											+ "-input -m owner --uid-owner 0:999999999 -j "
											+ chainName
											+ "-input-accept || exit 54\n");
								}
							} else {
								script.append("$IP6TABLES -A " + chainName
										+ "-input -j " + chainName
										+ "-input-drop || exit 50\n");
								if (logacceptenabled) {
									script.append("$IP6TABLES -A " + chainName
											+ "-input -j " + chainName
											+ "-input-accept || exit 54\n");
								}
							}
						} else {
							script.append("$IP6TABLES -A " + chainName
									+ "-input-wifi -j " + chainName
									+ "-input-drop || exit 54\n");
							if (logacceptenabled) {
								script.append("$IP6TABLES -A " + chainName
										+ "-input -j " + chainName
										+ "-input-accept || exit 54\n");
							}
						}
					} else {
						if (uidsinputwifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to BLOCK kernel packets on black-list\n");
							script.append("$IP6TABLES -A "
									+ chainName
									+ "-input -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
							script.append("$IP6TABLES -A " + chainName
									+ "-input -j " + chainName
									+ "-input-drop || exit 57\n");
							if (logacceptenabled) {
								script.append("$IP6TABLES -A " + chainName
										+ "-input -j " + chainName
										+ "-input-accept || exit 54\n");
							}
						}
					}
					script.append("$IP6TABLES -I "
							+ chainName
							+ "-input -m owner --uid-owner 0 -j RETURN || exit 4444\n");
					script.append("$IP6TABLES -I "
							+ chainName
							+ "-input -m state --state ESTABLISHED,RELATED -j ACCEPT || exit 4455\n");
				}
			}
			final StringBuilder res = new StringBuilder();
			code = runScriptAsRoot(ctx, script.toString(), res);
			if (showErrors && code != 0) {
				String msg = res.toString();
				Log.e(TAG, msg);
				// Remove unnecessary help message from output
				if (msg.indexOf("\nTry `iptables -h' or 'iptables --help' for more information.") != -1) {
					msg = msg
							.replace(
									"\nTry `iptables -h' or 'iptables --help' for more information.",
									"");
				}
				if (enabled && ipv6enabled) {
					alert(ctx, "Error applying iptables rules. Exit code: "
							+ code + "\n\n" + msg.trim());
					setIPv6Enabled(ctx, false);
					setEnabled(ctx, false);
				} else if (enabled && !ipv6enabled) {
					alert(ctx, "Error applying iptables rules. Exit code: "
							+ code + "\n\n" + msg.trim());
					setIPv6Enabled(ctx, false);
					setEnabled(ctx, false);
				} else if (!enabled && ipv6enabled) {
					alert(ctx, "Error applying iptables rules. Exit code: "
							+ code + "\n\n" + msg.trim());
					setIPv6Enabled(ctx, false);
					setEnabled(ctx, false);
				} else if (!enabled && !ipv6enabled) {
					alert(ctx, "Error applying iptables rules. Exit code: "
							+ code + "\n\n" + msg.trim());
					setIPv6Enabled(ctx, false);
					setEnabled(ctx, false);
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			if (showErrors)
				Log.d("{AF} - error applying rules", e.getMessage());
			alert(ctx, "error refreshing iptables: " + e);
		}
		return false;
	}

	/**
	 * Purge and re-add all saved rules (not in-memory ones). This is much
	 * faster than just calling "applyIptablesRules", since it don't need to
	 * read installed applications.
	 * 
	 * @param ctx
	 *            application context (mandatory)
	 * @param showErrors
	 *            indicates if errors should be alerted
	 */
	public static boolean applySavedIptablesRules(Context ctx,
			boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
		final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
		final String savedUids_roaming = prefs.getString(PREF_ROAMING_UIDS, "");
		final String savedUids_vpn = prefs.getString(PREF_VPN_UIDS, "");
		final String savedUids_lan = prefs.getString(PREF_LAN_UIDS, "");
		final String savedUids_input_wifi = prefs.getString(
				PREF_INPUT_WIFI_UIDS, "");
		final List<Integer> uids_wifi = new LinkedList<Integer>();
		if (savedUids_wifi.length() > 0) {
			// Check which applications are allowed on wifi
			final StringTokenizer tok = new StringTokenizer(savedUids_wifi, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_wifi.add(Integer.parseInt(uid));
					} catch (Exception ex) {
						Log.d("{AF} - error with WiFi UIDs", ex.getMessage());
					}
				}
			}
		}
		final List<Integer> uids_3g = new LinkedList<Integer>();
		if (savedUids_3g.length() > 0) {
			// Check which applications are allowed on 2G/3G
			final StringTokenizer tok = new StringTokenizer(savedUids_3g, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_3g.add(Integer.parseInt(uid));
					} catch (Exception ex) {
						Log.d("{AF} - error with Data UIDs", ex.getMessage());
					}
				}
			}
		}
		final List<Integer> uids_roaming = new LinkedList<Integer>();
		if (savedUids_roaming.length() > 0) {
			// Check which applications are allowed on 2G/3G
			final StringTokenizer tok = new StringTokenizer(savedUids_roaming,
					"|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_roaming.add(Integer.parseInt(uid));
					} catch (Exception ex) {
						Log.d("{AF} - error with Roaming UIDs", ex.getMessage());
					}
				}
			}
		}
		final List<Integer> uids_vpn = new LinkedList<Integer>();
		if (savedUids_vpn.length() > 0) {
			// Check which applications are allowed on 2G/3G
			final StringTokenizer tok = new StringTokenizer(savedUids_vpn, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_vpn.add(Integer.parseInt(uid));
					} catch (Exception ex) {
						Log.d("{AF} - error with VPN UIDs", ex.getMessage());
					}
				}
			}
		}
		final List<Integer> uids_lan = new LinkedList<Integer>();
		if (savedUids_lan.length() > 0) {
			// Check which applications are allowed on 2G/3G
			final StringTokenizer tok = new StringTokenizer(savedUids_lan, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_lan.add(Integer.parseInt(uid));
					} catch (Exception ex) {
						Log.d("{AF} - error with LAN UIDs", ex.getMessage());
					}
				}
			}
		}
		final List<Integer> uids_input_wifi = new LinkedList<Integer>();
		if (savedUids_input_wifi.length() > 0) {
			// Check which applications are allowed on 2G/3G
			final StringTokenizer tok = new StringTokenizer(
					savedUids_input_wifi, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_input_wifi.add(Integer.parseInt(uid));
					} catch (Exception ex) {
						Log.d("{AF} - error with Input Wifi UIDs",
								ex.getMessage());
					}
				}
			}
		}
		return applyIptablesRulesImpl(ctx, uids_wifi, uids_3g, uids_roaming,
				uids_vpn, uids_lan, uids_input_wifi, showErrors);
	}

	/**
	 * Purge and re-add all rules.
	 * 
	 * @param ctx
	 *            application context (mandatory)
	 * @param showErrors
	 *            indicates if errors should be alerted
	 */
	public static boolean applyIptablesRules(Context ctx, boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		saveRules(ctx);
		return applySavedIptablesRules(ctx, showErrors);
	}

	/**
	 * Save current rules using the preferences storage.
	 * 
	 * @param ctx
	 *            application context (mandatory)
	 */
	public static void saveRules(Context ctx) {
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		final List<DroidApp> apps = getApps(ctx);
		// Builds a pipe-separated list of names
		final StringBuilder newuids_wifi = new StringBuilder();
		final StringBuilder newuids_3g = new StringBuilder();
		final StringBuilder newuids_roaming = new StringBuilder();
		final StringBuilder newuids_vpn = new StringBuilder();
		final StringBuilder newuids_lan = new StringBuilder();
		final StringBuilder newuids_input_wifi = new StringBuilder();
		for (int i = 0; i < apps.size(); i++) {
			if (apps.get(i).selected_wifi) {
				if (newuids_wifi.length() != 0)
					newuids_wifi.append('|');
				newuids_wifi.append(apps.get(i).uid);
			}
			if (apps.get(i).selected_3g) {
				if (newuids_3g.length() != 0)
					newuids_3g.append('|');
				newuids_3g.append(apps.get(i).uid);
			}
			if (apps.get(i).selected_roaming) {
				if (newuids_roaming.length() != 0)
					newuids_roaming.append('|');
				newuids_roaming.append(apps.get(i).uid);
			}
			if (apps.get(i).selected_vpn) {
				if (newuids_vpn.length() != 0)
					newuids_vpn.append('|');
				newuids_vpn.append(apps.get(i).uid);
			}
			if (apps.get(i).selected_lan) {
				if (newuids_lan.length() != 0)
					newuids_lan.append('|');
				newuids_lan.append(apps.get(i).uid);
			}
			if (apps.get(i).selected_input_wifi) {
				if (newuids_input_wifi.length() != 0)
					newuids_input_wifi.append('|');
				newuids_input_wifi.append(apps.get(i).uid);
			}
		}
		// save the new list of UIDs
		final Editor edit = prefs.edit();
		edit.putString(PREF_WIFI_UIDS, newuids_wifi.toString());
		edit.putString(PREF_3G_UIDS, newuids_3g.toString());
		edit.putString(PREF_ROAMING_UIDS, newuids_roaming.toString());
		edit.putString(PREF_VPN_UIDS, newuids_vpn.toString());
		edit.putString(PREF_LAN_UIDS, newuids_lan.toString());
		edit.putString(PREF_INPUT_WIFI_UIDS, newuids_input_wifi.toString());
		edit.commit();
	}

	/**
	 * This exports rule data
	 */

	@SuppressLint("SimpleDateFormat")
	public static boolean exportRulesToFile(Context ctx, String exportedName) {
		boolean rules = false;
		String filename = exportedName + "_af.rules";
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/androidfirewall/");
		dir.mkdirs();
		File file = new File(dir, filename);

		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(file));
			saveRules(ctx);
			SharedPreferences pref = ctx.getSharedPreferences(PREFS_NAME,
					Context.MODE_PRIVATE);
			output.writeObject(pref.getAll());
			rules = true;
		} catch (IOException error) {
			error.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException errors) {
				errors.printStackTrace();
			}
		}
		return rules;
	}

	/**
	 * Purge all iptables rules.
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param showErrors
	 *            indicates if errors should be alerted
	 * @return true if the rules were purged
	 */
	public static boolean purgeIptables(Context ctx, boolean showErrors) {
		final boolean ipv6enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_IP6TABLES, true);
		final StringBuilder res = new StringBuilder();
		try {
			assertBinaries(ctx, showErrors);
			// Custom "shutdown" script
			final String customScript = ctx.getSharedPreferences(
					Api.PREFS_NAME, 0).getString(Api.PREF_CUSTOMSCRIPT2, "");
			final StringBuilder script = new StringBuilder();
			SharedPreferences prefs2 = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			final String chainName = prefs2.getString("chainName", "");
			script.append(scriptHeader(ctx));
			script.append("" + "$IPTABLES -F " + chainName + "\n"
					+ "$IPTABLES -F " + chainName + "-reject\n"
					+ "$IPTABLES -F " + chainName + "-3g\n" + "$IPTABLES -F "
					+ chainName + "-vpn\n" + "$IPTABLES -F " + chainName
					+ "-lan\n" + "$IPTABLES -F " + chainName + "-wifi\n"
					+ "$IPTABLES -F " + chainName + "-input\n"
					+ "$IPTABLES -F " + chainName + "-input-wifi\n"
					+ "$IPTABLES -F " + chainName + "-input-3g\n"
					+ "$IPTABLES -F " + chainName + "-input-drop\n" + "");
			if (ipv6enabled) {
				script.append(scriptHeader(ctx));
				script.append("" + "$IP6TABLES --flush " + chainName + "\n"
						+ "$IP6TABLES --flush " + chainName + "-reject\n"
						+ "$IP6TABLES --flush " + chainName + "-3g\n"
						+ "$IP6TABLES --flush " + chainName + "-vpn\n"
						+ "$IP6TABLES --flush " + chainName + "-lan\n"
						+ "$IP6TABLES --flush " + chainName + "-wifi\n"
						+ "$IP6TABLES -F " + chainName + "-input\n"
						+ "$IP6TABLES -F " + chainName + "-input-wifi\n"
						+ "$IP6TABLES -F " + chainName + "-input-3g\n"
						+ "$IP6TABLES -F " + chainName + "-input-drop\n" + "");
			}
			if (customScript.length() > 0) {
				script.append("\n# BEGIN OF CUSTOM SCRIPT (user-defined)\n");
				script.append(customScript);
				script.append("\n# END OF CUSTOM SCRIPT (user-defined)\n\n");
			}
			int code = runScriptAsRoot(ctx, script.toString(), res);
			if (code == -1) {
				if (showErrors)
					alert(ctx, R.string.error_purging_code + " " + code + "\n"
							+ res);
				return false;
			}
			return true;
		} catch (Exception e) {
			if (showErrors)
				alert(ctx, R.string.error_purging + " " + e);
			return false;
		}
	}

	public static boolean purgeIp6tables(Context ctx, boolean showErrors) {
		final StringBuilder res = new StringBuilder();
		try {
			assertBinaries(ctx, showErrors);
			// Custom "shutdown" script
			final String customScript = ctx.getSharedPreferences(
					Api.PREFS_NAME, 0).getString(Api.PREF_CUSTOMSCRIPT2, "");
			final StringBuilder script = new StringBuilder();
			SharedPreferences prefs2 = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			final String chainName = prefs2.getString("chainName", "");
			script.append(scriptHeader(ctx));
			script.append("" + "$IP6TABLES --flush " + chainName + "\n"
					+ "$IP6TABLES --flush " + chainName + "-reject\n"
					+ "$IP6TABLES --flush " + chainName + "-3g\n"
					+ "$IP6TABLES --flush " + chainName + "-vpn\n"
					+ "$IP6TABLES --flush " + chainName + "-lan\n"
					+ "$IP6TABLES --flush " + chainName + "-wifi\n"
					+ "$IP6TABLES -F " + chainName + "-input\n"
					+ "$IP6TABLES -F " + chainName + "-input-wifi\n"
					+ "$IP6TABLES -F " + chainName + "-input-3g\n"
					+ "$IP6TABLES -F " + chainName + "-input-drop\n" + "");
			if (customScript.length() > 0) {
				script.append("\n# BEGIN OF CUSTOM SCRIPT (user-defined)\n");
				script.append(customScript);
				script.append("\n# END OF CUSTOM SCRIPT (user-defined)\n\n");
			}
			int code = runScriptAsRoot(ctx, script.toString(), res);
			if (code == -1) {
				if (showErrors)
					alert(ctx, R.string.error_purgingipv6_code + " " + code
							+ "\n" + res);
				return false;
			}
			return true;
		} catch (Exception e) {
			if (showErrors)
				alert(ctx, R.string.error_purgingipv6 + " " + e);
			return false;
		}
	}

	/**
	 * Display iptables rules output
	 * 
	 * @param ctx
	 *            application context
	 */
	public static String showIptablesRules(Context ctx) {
		final boolean ipv6enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_IP6TABLES, false);
		final boolean enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_ENABLED, false);
		try {
			if (enabled && ipv6enabled) {
				final StringBuilder res = new StringBuilder();
				runScriptAsRoot(ctx, scriptHeader(ctx) + "$ECHO $IPTABLES\n"
						+ "$IPTABLES -L -v -n\n"
						+ "***Start of IPv6 rules***\n"
						+ "$IP6TABLES -L -v -n\n", res);
				return res.toString();
			}
			if (enabled) {
				final StringBuilder res = new StringBuilder();
				runScriptAsRoot(ctx, scriptHeader(ctx) + "$ECHO $IPTABLES\n"
						+ "$IPTABLES -L -v -n\n", res);
				return res.toString();
			}
			if (!enabled) {
				final StringBuilder res = new StringBuilder();
				runScriptAsRoot(ctx, scriptHeader(ctx) + "$ECHO $IPTABLES\n"
						+ "$IPTABLES -L -v -n\n", res);
				return res.toString();
			}
		} catch (Exception e) {
			Log.d("{AF} - error showing rules", e.getMessage());
			alert(ctx, R.string.error_showing_rules + " " + e);
		}
		return "";
	}

	/**
	 * Display logs
	 * 
	 * @param ctx
	 *            application context
	 * @return true if the clogs were cleared
	 */
	public static boolean clearLog(Context ctx) {
		final String logtarget = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getString(PREF_LOGTARGET, "");
		if (logtarget.equals("NFLOG")) {
			NflogService.clearLog();
			return true;
		} else {
			try {
				final StringBuilder res = new StringBuilder();
				int code = runScriptAsRoot(ctx,
						"dmesg -c >/dev/null || exit\n", res);
				if (code != 0) {
					alert(ctx, res);
					return false;
				}
				return true;
			} catch (Exception e) {
				Log.d("{AF} - error clearing the logs", e.getMessage());
				alert(ctx, R.string.error_clearing_rules + " " + e);
			}
			return false;
		}
	}

	/**
	 * Display logs
	 * 
	 * @param ctx
	 *            application context
	 */

	static String getBusyBoxPath(Context ctx) {
		final String dir = ctx.getDir("bin", 0).getAbsolutePath();
		String arch = System.getProperty("os.arch");
		String busybox;
		if (arch.equals("i686")) {
			busybox = dir + "/busybox_x86v2 ";
			Log.d(TAG, "Using x86 Busybox. " + arch);
		} else {
			busybox = dir + "/busybox_g1 ";
			Log.d(TAG, "Using G1 Busybox. " + arch);
		}
		return busybox;
	}

	static String getNflogPath(Context ctx) {
		final String dir = ctx.getDir("bin", 0).getAbsolutePath();
		String arch = System.getProperty("os.arch");
		String nflog;
		if (arch.equals("i686")) {
			nflog = dir + "/nflog_x86 ";
			Log.d(TAG, "Using x86 nflog. " + arch);
		} else {
			nflog = dir + "/nflogv2 ";
			Log.d(TAG, "Using ARM nflog. " + arch);
		}
		return nflog;
	}

	public static String showLog(Context ctx) {

		StringBuilder res = new StringBuilder();
		StringBuilder output = new StringBuilder();
		int code = 0;
		try {
			code = runScriptAsRoot(ctx, scriptHeader(ctx) + dmesgCommand, res);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (code != 0) {
			if (res.length() == 0) {
				output.append(ctx.getString(R.string.log_empty));
			}
			return output.toString();
		}
		final BufferedReader r = new BufferedReader(new StringReader(
				res.toString()));
		final Integer unknownUID = -99;
		res = new StringBuilder();
		String line;
		int start, end;
		Integer appid;
		final SparseArray<LogInfo> map = new SparseArray<LogInfo>();
		LogInfo loginfo = null;

		try {
			while ((line = r.readLine()) != null) {
				if (line.indexOf(logstring) == -1)
					continue;
				appid = unknownUID;
				if (((start = line.indexOf("UID=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					appid = Integer.parseInt(line.substring(start + 4, end));
				}
				loginfo = map.get(appid);
				if (loginfo == null) {
					loginfo = new LogInfo();
					map.put(appid, loginfo);
				}
				loginfo.totalBlocked += 1;
				if (((start = line.indexOf("DST=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String dst = line.substring(start + 4, end);
					if (loginfo.dstBlocked.containsKey(dst)) {
						loginfo.dstBlocked.put(dst,
								loginfo.dstBlocked.get(dst) + 1);
					} else {
						loginfo.dstBlocked.put(dst, 1);
					}
				}
				if (((start = line.indexOf("PROTO=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String proto = line.substring(start + 6, end);
					if (loginfo.protoBlocked.containsKey(proto)) {
						loginfo.protoBlocked.put(proto,
								loginfo.protoBlocked.get(proto) + 1);
					} else {
						loginfo.protoBlocked.put(proto, 1);
					}
				}
				if (((start = line.indexOf("SRC=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String src = line.substring(start + 4, end);
					if (loginfo.srcBlocked.containsKey(src)) {
						loginfo.srcBlocked.put(src,
								loginfo.srcBlocked.get(src) + 1);
					} else {
						loginfo.srcBlocked.put(src, 1);
					}
				}
				if (((start = line.indexOf("DPT=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String dpt = line.substring(start + 4, end);
					if (loginfo.dptBlocked.containsKey(dpt)) {
						loginfo.dptBlocked.put(dpt,
								loginfo.dptBlocked.get(dpt) + 1);
					} else {
						loginfo.dptBlocked.put(dpt, 1);
					}
				}
			}
			final List<DroidApp> apps = getApps(ctx);
			Integer id;
			String appName = "";
			int appId = -1;
			int totalBlocked;
			for (int i = 0; i < map.size(); i++) {
				StringBuilder address = new StringBuilder();
				id = map.keyAt(i);
				if (id != unknownUID) {
					for (DroidApp app : apps) {
						if (app.uid == id) {
							appId = id;
							appName = app.names.get(0);
							break;
						}
					}
				} else {
					appName = "Kernel";
				}
				loginfo = map.valueAt(i);
				totalBlocked = loginfo.totalBlocked;
				if (loginfo.dstBlocked.size() > 0) {
					for (String dst : loginfo.dstBlocked.keySet()) {
						address.append("Destination IP: " + dst + "("
								+ loginfo.dstBlocked.get(dst) + ")");
						address.append("\n");
					}
				}
				if (loginfo.protoBlocked.size() > 0) {
					for (String proto : loginfo.protoBlocked.keySet()) {
						address.append("Protocol used: " + proto + "("
								+ loginfo.protoBlocked.get(proto) + ")");
						address.append("\n");
					}
				}
				if (loginfo.srcBlocked.size() > 0) {
					for (String src : loginfo.srcBlocked.keySet()) {
						address.append("Source IP: " + src + "("
								+ loginfo.srcBlocked.get(src) + ")");
						address.append("\n");
					}
				}
				if (loginfo.dptBlocked.size() > 0) {
					for (String dpt : loginfo.dptBlocked.keySet()) {
						address.append("Destination Port: " + dpt + "("
								+ loginfo.dptBlocked.get(dpt) + ")");
						address.append("\n");
					}
				}
				if (rejectlog == true) {
					res.append("AppID :\t" + appId + "\n"
							+ ctx.getString(R.string.LogAppName) + ":\t"
							+ appName + "\n"
							+ ctx.getString(R.string.LogPackBlock) + ":\t"
							+ totalBlocked + "\n");
				}
				if (rejectlog == false) {
					res.append("AppID :\t" + appId + "\n"
							+ ctx.getString(R.string.LogAppName) + ":\t"
							+ appName + "\n"
							+ ctx.getString(R.string.LogPackAllowed) + ":\t"
							+ totalBlocked + "\n");
				}
				res.append(address.toString());
				res.append("\n\t---------\n");
			}
		} catch (Exception e) {
			Log.d("{AF} - error showing the logs", e.getMessage());
			alert(ctx, "error: " + e);
		}
		if (res.length() == 0) {
			res.append(ctx.getString(R.string.log_empty));
		}
		return res.toString();
	}

	/**
	 * get NFLOG information
	 */
	public static String parseLog(Context ctx, String dmesg) {
		final BufferedReader r = new BufferedReader(new StringReader(
				dmesg.toString()));
		final Integer unknownUID = -99;
		StringBuilder res = new StringBuilder();
		String line;
		int start, end;
		Integer appid;
		final SparseArray<LogInfo> map = new SparseArray<LogInfo>();
		LogInfo loginfo = null;

		try {
			while ((line = r.readLine()) != null) {
				if (line.indexOf(nflogCommand) == -1)
					continue;
				appid = unknownUID;
				if (((start = line.indexOf("UID=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					appid = Integer.parseInt(line.substring(start + 4, end));
				}
				loginfo = map.get(appid);
				if (loginfo == null) {
					loginfo = new LogInfo();
					map.put(appid, loginfo);
				}
				loginfo.totalBlocked += 1;
				if (((start = line.indexOf("DST=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String dst = line.substring(start + 4, end);
					if (loginfo.dstBlocked.containsKey(dst)) {
						loginfo.dstBlocked.put(dst,
								loginfo.dstBlocked.get(dst) + 1);
					} else {
						loginfo.dstBlocked.put(dst, 1);
					}
				}
				if (((start = line.indexOf("PROTO=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String proto = line.substring(start + 6, end);
					if (loginfo.protoBlocked.containsKey(proto)) {
						loginfo.protoBlocked.put(proto,
								loginfo.protoBlocked.get(proto) + 1);
					} else {
						loginfo.protoBlocked.put(proto, 1);
					}
				}
				if (((start = line.indexOf("SRC=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String src = line.substring(start + 4, end);
					if (loginfo.srcBlocked.containsKey(src)) {
						loginfo.srcBlocked.put(src,
								loginfo.srcBlocked.get(src) + 1);
					} else {
						loginfo.srcBlocked.put(src, 1);
					}
				}
				if (((start = line.indexOf("DPT=")) != -1)
						&& ((end = line.indexOf(" ", start)) != -1)) {
					String dpt = line.substring(start + 4, end);
					if (loginfo.dptBlocked.containsKey(dpt)) {
						loginfo.dptBlocked.put(dpt,
								loginfo.dptBlocked.get(dpt) + 1);
					} else {
						loginfo.dptBlocked.put(dpt, 1);
					}
				}
			}
			final List<DroidApp> apps = getApps(ctx);
			Integer id;
			String appName = "";
			int appId = -1;
			int totalBlocked;
			for (int i = 0; i < map.size(); i++) {
				StringBuilder address = new StringBuilder();
				id = map.keyAt(i);
				if (id != unknownUID) {
					for (DroidApp app : apps) {
						if (app.uid == id) {
							appId = id;
							appName = app.names.get(0);
							break;
						}
					}
				} else {
					appName = "Kernel";
				}
				loginfo = map.valueAt(i);
				totalBlocked = loginfo.totalBlocked;
				if (loginfo.dstBlocked.size() > 0) {
					for (String dst : loginfo.dstBlocked.keySet()) {
						address.append("Destination IP: " + dst + "("
								+ loginfo.dstBlocked.get(dst) + ")");
						address.append("\n");
					}
				}
				if (loginfo.protoBlocked.size() > 0) {
					for (String proto : loginfo.protoBlocked.keySet()) {
						address.append("Protocol used: " + proto + "("
								+ loginfo.protoBlocked.get(proto) + ")");
						address.append("\n");
					}
				}
				if (loginfo.srcBlocked.size() > 0) {
					for (String src : loginfo.srcBlocked.keySet()) {
						address.append("Source IP: " + src + "("
								+ loginfo.srcBlocked.get(src) + ")");
						address.append("\n");
					}
				}
				if (loginfo.dptBlocked.size() > 0) {
					for (String dpt : loginfo.dptBlocked.keySet()) {
						address.append("Destination Port: " + dpt + "("
								+ loginfo.dptBlocked.get(dpt) + ")");
						address.append("\n");
					}
				}
				if (rejectlog == true) {
					res.append("AppID :\t" + appId + "\n"
							+ ctx.getString(R.string.LogAppName) + ":\t"
							+ appName + "\n"
							+ ctx.getString(R.string.LogPackBlock) + ":\t"
							+ totalBlocked + "\n");
				}
				if (rejectlog == false) {
					res.append("AppID :\t" + appId + "\n"
							+ ctx.getString(R.string.LogAppName) + ":\t"
							+ appName + "\n"
							+ ctx.getString(R.string.LogPackAllowed) + ":\t"
							+ totalBlocked + "\n");
				}
				res.append(address.toString());
				res.append("\n\t---------\n");
			}
		} catch (Exception e) {
			Log.d(TAG, "NFLOG is null");
			return null;

		}
		if (res.length() == 0) {
			res.append(ctx.getString(R.string.log_empty));
		}
		return res.toString();
	}

	/**
	 * Change user language
	 */
	public static void changeLanguage(Context context, String language) {
		Locale locale;

		if (language.equals("")) {
			/* use system language settings */
			locale = Locale.getDefault();
		} else if (language.contains("-")) {
			/* handle special language code, in language-country format */
			String array[] = language.split("-");
			locale = new Locale(array[0], array[1]);
		} else {
			locale = new Locale(language);
		}
		Configuration config = new Configuration();
		config.locale = locale;
		context.getResources().updateConfiguration(config, null);
	}

	/**
	 * get uids for interfaces
	 */
	private static List<Integer> getUidList(Context ctx, final String packages) {
		final PackageManager pm = ctx.getPackageManager();
		final List<Integer> uids = new ArrayList<Integer>();
		final StringTokenizer tok = new StringTokenizer(packages, "|");
		while (tok.hasMoreTokens()) {
			final String pkg = tok.nextToken();
			if (pkg != null && pkg.length() > 0) {
				try {
					uids.add(pm.getApplicationInfo(pkg, 0).uid);
				} catch (Exception ex) {
				}
			}

		}
		Collections.sort(uids);
		return uids;
	}

	private static List<Integer> getListFromPref(String savedPkg_uid) {

		final StringTokenizer tok = new StringTokenizer(savedPkg_uid, "|");
		List<Integer> listUids = new ArrayList<Integer>();
		while (tok.hasMoreTokens()) {
			final String uid = tok.nextToken();
			if (!uid.equals("")) {
				try {
					listUids.add(Integer.parseInt(uid));
				} catch (Exception ex) {

				}
			}
		}
		// Sort the array to allow using "Arrays.binarySearch" later
		Collections.sort(listUids);
		return listUids;
	}

	/**
	 * @param ctx
	 *            application context (mandatory)
	 * @return a list of applications
	 */
	public static List<DroidApp> getApps(Context ctx) {
		if (applications != null) {
			// return cached instance
			return applications;
		}
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		final boolean vpnenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_VPNENABLED, false);
		final boolean lanenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_LANENABLED, false);
		final boolean roamenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_ROAMENABLED, false);
		final boolean inputwifienabled = ctx
				.getSharedPreferences(PREFS_NAME, 0).getBoolean(
						PREF_INPUTENABLED, false);

		// allowed application names separated by pipe '|' (persisted)
		final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
		final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
		final String savedUids_Roaming = prefs.getString(PREF_ROAMING_UIDS, "");
		final String savedUids_Vpn = prefs.getString(PREF_VPN_UIDS, "");
		final String savedUids_Lan = prefs.getString(PREF_LAN_UIDS, "");
		final String savedUids_InputWifi = prefs.getString(
				PREF_INPUT_WIFI_UIDS, "");

		List<Integer> selected_wifi = new ArrayList<Integer>();
		List<Integer> selected_3g = new ArrayList<Integer>();
		List<Integer> selected_roaming = new ArrayList<Integer>();
		List<Integer> selected_vpn = new ArrayList<Integer>();
		List<Integer> selected_lan = new ArrayList<Integer>();
		List<Integer> selected_input_wifi = new ArrayList<Integer>();

		if (savedUids_wifi.equals("")) {
			selected_wifi = getUidList(ctx, savedUids_wifi);
		} else {
			selected_wifi = getListFromPref(savedUids_wifi);
		}

		if (savedUids_3g.equals("")) {
			selected_3g = getUidList(ctx, savedUids_3g);
		} else {
			selected_3g = getListFromPref(savedUids_3g);
		}
		if (roamenabled) {
			if (savedUids_Roaming.equals("")) {
				selected_roaming = getUidList(ctx, savedUids_Roaming);
			} else {
				selected_roaming = getListFromPref(savedUids_Roaming);
			}
		}
		if (vpnenabled) {
			if (savedUids_Vpn.equals("")) {
				selected_vpn = getUidList(ctx, savedUids_Vpn);
			} else {
				selected_vpn = getListFromPref(savedUids_Vpn);
			}
		}
		if (lanenabled) {
			if (savedUids_Lan.equals("")) {
				selected_lan = getUidList(ctx, savedUids_Lan);
			} else {
				selected_lan = getListFromPref(savedUids_Lan);
			}
		}
		if (inputwifienabled) {
			if (savedUids_InputWifi.equals("")) {
				selected_input_wifi = getUidList(ctx, savedUids_InputWifi);
			} else {
				selected_input_wifi = getListFromPref(savedUids_InputWifi);
			}
		}

		try {
			final PackageManager pkgmanager = ctx.getPackageManager();
			final List<ApplicationInfo> installed = pkgmanager
					.getInstalledApplications(PackageManager.GET_META_DATA);
			SparseArray<DroidApp> syncMap = new SparseArray<DroidApp>();
			final Editor edit = prefs.edit();
			boolean changed = false;
			String name = null;
			String cachekey = null;
			DroidApp app = null;
			ApplicationInfo appStatus;
			boolean as;

			for (final ApplicationInfo apinfo : installed) {

				appStatus = pkgmanager
						.getApplicationInfo(apinfo.packageName, 0);
				as = appStatus.enabled;
				boolean firstseen = false;
				app = syncMap.get(apinfo.uid);
				// filter applications which are not allowed to access the
				// Internet
				if (app == null
						&& PackageManager.PERMISSION_GRANTED != pkgmanager
								.checkPermission(Manifest.permission.INTERNET,
										apinfo.packageName)) {
					continue;
				}
				if (app == null && as == false) {
					continue;
				}
				// try to get the application label from our cache -
				// getApplicationLabel() is horribly slow!!!!
				cachekey = "cache.label." + apinfo.packageName;
				name = prefs.getString(cachekey, "");
				if (name.length() == 0) {
					// get label and put on cache
					name = pkgmanager.getApplicationLabel(apinfo).toString();
					edit.putString(cachekey, name);
					changed = true;
					firstseen = true;
				}
				if (app == null) {
					app = new DroidApp();
					app.uid = apinfo.uid;
					app.names = new ArrayList<String>();
					app.names.add(name);
					app.appinfo = apinfo;
					syncMap.put(apinfo.uid, app);
				} else {
					app.names.add(name);
				}
				app.firstseen = firstseen;
				// check if this application is selected
				if (!app.selected_wifi
						&& Collections.binarySearch(selected_wifi, app.uid) >= 0) {
					app.selected_wifi = true;
				}
				if (!app.selected_3g
						&& Collections.binarySearch(selected_3g, app.uid) >= 0) {
					app.selected_3g = true;
				}
				if (!app.selected_roaming
						&& Collections.binarySearch(selected_roaming, app.uid) >= 0) {
					app.selected_roaming = true;
				}
				if (!app.selected_vpn
						&& Collections.binarySearch(selected_vpn, app.uid) >= 0) {
					app.selected_vpn = true;
				}
				if (!app.selected_lan
						&& Collections.binarySearch(selected_lan, app.uid) >= 0) {
					app.selected_lan = true;
				}
				if (!app.selected_input_wifi
						&& Collections.binarySearch(selected_input_wifi,
								app.uid) >= 0) {
					app.selected_input_wifi = true;
				}
			}
			if (changed) {
				edit.commit();
			}
			/* add special applications to the list */
			List<DroidApp> special = new ArrayList<DroidApp>();
			special.add(new DroidApp(SPECIAL_UID_ANY,
					"(Any application) - Same as selecting all applications",
					false, false, false, false, false, false));
			special.add(new DroidApp(SPECIAL_UID_KERNEL,
					"(Kernel) - Linux kernel", false, false, false, false,
					false, false));
			special.add(new DroidApp(android.os.Process.getUidForName("root"),
					"(root) - Applications running as root", false, false,
					false, false, false, false));
			special.add(new DroidApp(android.os.Process.getUidForName("media"),
					"Media server", false, false, false, false, false, false));
			special.add(new DroidApp(android.os.Process.getUidForName("vpn"),
					"VPN networking", false, false, false, false, false, false));
			special.add(new DroidApp(android.os.Process.getUidForName("shell"),
					"Linux shell", false, false, false, false, false, false));
			special.add(new DroidApp(android.os.Process.getUidForName("gps"),
					"GPS", false, false, false, false, false, false));
			for (int i = 0; i < special.size(); i++) {
				app = special.get(i);
				if (app.uid != -1 && syncMap.get(app.uid) == null) {
					// check if this application is allowed
					if (Collections.binarySearch(selected_wifi, app.uid) >= 0) {
						app.selected_wifi = true;
					}
					if (Collections.binarySearch(selected_3g, app.uid) >= 0) {
						app.selected_3g = true;
					}
					if (Collections.binarySearch(selected_roaming, app.uid) >= 0) {
						app.selected_roaming = true;
					}
					if (Collections.binarySearch(selected_vpn, app.uid) >= 0) {
						app.selected_vpn = true;
					}
					if (Collections.binarySearch(selected_lan, app.uid) >= 0) {
						app.selected_lan = true;
					}
					if (Collections.binarySearch(selected_input_wifi, app.uid) >= 0) {
						app.selected_input_wifi = true;
					}
					syncMap.put(app.uid, app);
				}
			}
			applications = new ArrayList<DroidApp>();
			for (int i = 0; i < syncMap.size(); i++) {
				applications.add(syncMap.valueAt(i));
			}
			return applications;
		} catch (Exception e) {
			Log.d("{AF} - error generating list of apps", e.getMessage());
			alert(ctx, "error: " + e);
		}
		return null;
	}

	/**
	 * Check if we have root access
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param showErrors
	 *            indicates if errors should be alerted
	 * @return boolean true if we have root
	 */

	public static boolean hasRootAccess(Context ctx, boolean showErrors) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		boolean rootaccess = prefs.getBoolean("superuser", false);

		if (!rootaccess) {
			try {
				// Run an empty script just to check root access
				int returnCode = new checkForRoot().execute(null, null).get();
				if (returnCode == 0) {
					rootaccess = true;
					Editor edit = prefs.edit();
					edit.putBoolean("superuser", true);
					edit.commit();
				} else {
					if (showErrors) {
						alert(ctx, ctx.getString(R.string.error_no_root));
					}
				}
			} catch (Exception e) {
				alert(ctx, R.string.error_accessing_root + " " + e);
			}
		}
		return rootaccess;
	}

	private static class checkForRoot extends
			AsyncTask<Object, Object, Integer> {
		private int exitCode = -1;
		private boolean suAvailable = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Object... params) {
			try {
				suAvailable = Shell.SU.available();
				if (suAvailable)
					exitCode = 0;
			} catch (Exception ex) {
			}
			return exitCode;
		}

	}

	/**
	 * Runs a script as root (multiple commands separated by "\n") with a
	 * default timeout of 20 seconds.
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param script
	 *            the script to be executed
	 * @param res
	 *            the script output response (stdout + stderr)
	 * @param timeout
	 *            timeout in milliseconds (-1 for none)
	 * @return the script exit code
	 * @throws IOException
	 *             on any error executing the script, or writing it to disk
	 */

	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res, long timeout) {
		return runScript(ctx, script, res, timeout, true);
	}

	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res) throws IOException {
		return runScriptAsRoot(ctx, script, res, 40000);
	}

	public static int runScript(Context ctx, String script, StringBuilder res)
			throws IOException {
		return runScript(ctx, script, res, 40000, false);
	}

	public static int runScript(Context ctx, String script, StringBuilder res,
			long timeout, boolean asroot) {
		int returncode = -1;
		try {
			returncode = new applyIptableRules().execute(script, res).get();
		} catch (Exception e) {
			Log.d("{AF} - error applying iptables in runScript", e.getMessage());
			Toast.makeText(ctx, R.string.toast_error_enabling,
					Toast.LENGTH_LONG).show();
		}
		return returncode;
	}

	/**
	 * Asserts that the binary files are installed in the cache directory.
	 * 
	 * @param ctx
	 *            context
	 * @param showErrors
	 *            indicates if errors should be alerted
	 * @return false if the binary files could not be installed
	 */
	public static boolean assertBinaries(Context ctx, boolean showErrors) {
		boolean changed = false;
		String arch = System.getProperty("os.arch");
		try {
			// Check iptables_armv5
			File file = new File(ctx.getDir("bin", 0), "iptables_armv5");
			if (!file.exists() || file.length() != 198652) {
				copyRawFile(ctx, R.raw.iptables_armv5, file, "755");
				changed = true;
			}
			if (arch.equals("i686")) {
				// Check busybox for x86
				file = new File(ctx.getDir("bin", 0), "busybox_x86v2");
				if (!file.exists()) {
					copyRawFile(ctx, R.raw.busybox_x86v2, file, "755");
					changed = true;
				}
			} else {
				// Check busybox for ARM
				file = new File(ctx.getDir("bin", 0), "busybox_g1");
				if (!file.exists()) {
					copyRawFile(ctx, R.raw.busybox_g1, file, "755");
					changed = true;
				}
			}
			// check nflog
			if (arch.equals("i686")) {
				file = new File(ctx.getDir("bin", 0), "nflog_x86");
				if (!file.exists()) {
					copyRawFile(ctx, R.raw.nflog_x86, file, "755");
					changed = true;
				}
			} else {
				file = new File(ctx.getDir("bin", 0), "nflogv2");
				if (!file.exists()) {
					copyRawFile(ctx, R.raw.nflogv2, file, "755");
					changed = true;
				}
			}
			if (changed) {
				Toast.makeText(ctx, R.string.toast_bin_installed,
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			if (showErrors)
				alert(ctx, R.string.error_installing_binaries + " " + e);
			return false;
		}
		return true;
	}

	/**
	 * Check if the firewall is enabled
	 * 
	 * @param ctx
	 *            mandatory context
	 * @return boolean
	 */
	public static boolean isEnabled(Context ctx) {
		if (ctx == null)
			return false;
		return ctx.getSharedPreferences(PREFS_NAME, 0).getBoolean(PREF_ENABLED,
				false);
	}

	/**
	 * determines if data connection is roaming
	 */
	public static boolean isRoaming(Context context) {
		TelephonyManager localTelephonyManager = (TelephonyManager) context
				.getSystemService("phone");
		try {
			return localTelephonyManager.isNetworkRoaming();
		} catch (Exception i) {
			while (true) {
			}
		}
	}

	/**
	 * Defines if the firewall is enabled and broadcasts the new status
	 * 
	 * @param ctx
	 *            mandatory context
	 * @param enabled
	 *            enabled flag
	 */
	public static void setEnabled(Context ctx, boolean enabled) {
		if (ctx == null)
			return;
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		if (prefs.getBoolean(PREF_ENABLED, false) == enabled) {
			return;
		}
		final Editor edit = prefs.edit();
		edit.putBoolean(PREF_ENABLED, enabled);
		if (!edit.commit()) {
			alert(ctx, "Error writing to preferences");
			return;
		}
		/* notify */
		final Intent message = new Intent(Api.STATUS_CHANGED_MSG);
		message.putExtra(Api.STATUS_EXTRA, enabled);
		ctx.sendBroadcast(message);
	}

	public static void setIPv6Enabled(Context ctx, boolean ipv6enabled) {
		if (ctx == null)
			return;
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		if (prefs.getBoolean("ipv6enabled", false) == ipv6enabled) {
			return;
		}
		final Editor edit = prefs.edit();
		edit.putBoolean("ipv6enabled", ipv6enabled);
		if (!edit.commit()) {
			alert(ctx, "Error writing to preferences!");
			return;
		}
	}

	/**
	 * Called when an application in removed (un-installed) from the system.
	 * This will look for that application in the selected list and update the
	 * persisted values if necessary
	 * 
	 * @param ctx
	 *            mandatory app context
	 * @param uid
	 *            UID of the application that has been removed
	 */
	public static void applicationRemoved(Context ctx, int uid) {
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		final Editor editor = prefs.edit();
		// allowed application names separated by pipe '|' (persisted)
		final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
		final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
		final String savedUids_roaming = prefs.getString(PREF_ROAMING_UIDS, "");
		final String savedUids_vpn = prefs.getString(PREF_VPN_UIDS, "");
		final String savedUids_lan = prefs.getString(PREF_LAN_UIDS, "");
		final String savedUids_input_wifi = prefs.getString(
				PREF_INPUT_WIFI_UIDS, "");
		final String uid_str = uid + "";
		boolean changed = false;
		// look for the removed application in the "wi-fi" list
		if (savedUids_wifi.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(savedUids_wifi, "|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d(TAG, "Removing UID " + token
							+ " from the wi-fi list (package removed)!");
					changed = true;
				} else {
					if (newuids.length() > 0)
						newuids.append('|');
					newuids.append(token);
				}
			}
			if (changed) {
				editor.putString(PREF_WIFI_UIDS, newuids.toString());
			}
		}
		// look for the removed application in the "3g" list
		if (savedUids_3g.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(savedUids_3g, "|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d(TAG, "Removing UID " + token
							+ " from the 3G list (package removed)!");
					changed = true;
				} else {
					if (newuids.length() > 0)
						newuids.append('|');
					newuids.append(token);
				}
			}
			if (changed) {
				editor.putString(PREF_3G_UIDS, newuids.toString());
			}
		}
		// look for the removed application in the roaming list
		if (savedUids_roaming.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(savedUids_roaming,
					"|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d(TAG, "Removing UID " + token
							+ " from the Roaming list (package removed)!");
					changed = true;
				} else {
					if (newuids.length() > 0)
						newuids.append('|');
					newuids.append(token);
				}
			}
			if (changed) {
				editor.putString(PREF_ROAMING_UIDS, newuids.toString());
			}
		}
		// look for the removed application in the vpn list
		if (savedUids_vpn.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(savedUids_vpn, "|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d(TAG, "Removing UID " + token
							+ " from the VPN list (package removed)!");
					changed = true;
				} else {
					if (newuids.length() > 0)
						newuids.append('|');
					newuids.append(token);
				}
			}
			if (changed) {
				editor.putString(PREF_VPN_UIDS, newuids.toString());
			}
		}
		// look for the removed application in the lan list
		if (savedUids_lan.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(savedUids_lan, "|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d(TAG, "Removing UID " + token
							+ " from the LAN list (package removed)!");
					changed = true;
				} else {
					if (newuids.length() > 0)
						newuids.append('|');
					newuids.append(token);
				}
			}
			if (changed) {
				editor.putString(PREF_LAN_UIDS, newuids.toString());
			}
		}
		// look for the removed application in the input wifi list
		if (savedUids_input_wifi.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(
					savedUids_input_wifi, "|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d(TAG, "Removing UID " + token
							+ " from the Input Wifi list (package removed)!");
					changed = true;
				} else {
					if (newuids.length() > 0)
						newuids.append('|');
					newuids.append(token);
				}
			}
			if (changed) {
				editor.putString(PREF_INPUT_WIFI_UIDS, newuids.toString());
			}
		}
		// if anything has changed, save the new prefs...
		if (changed) {
			editor.commit();
			if (isEnabled(ctx)) {
				// .. and also re-apply the rules if the firewall is enabled
				applySavedIptablesRules(ctx, false);
			}
		}
	}

	/**
	 * Small structure to hold an application info
	 */
	public static final class DroidApp {
		/** linux user id */
		int uid;
		/** application names belonging to this user id */
		List<String> names;
		/** indicates if this application is selected for wifi */
		boolean selected_wifi;
		/** indicates if this application is selected for 3g */
		boolean selected_3g;
		// indicated if this application is selected for roaming
		boolean selected_roaming;
		// indicates if this application is selected for vpn
		boolean selected_vpn;
		// indicates if this application is selected for lan
		boolean selected_lan;
		// indicates if this application is selected for input chain on wifi
		boolean selected_input_wifi;

		/** toString cache */
		String tostr;
		/** application info */
		ApplicationInfo appinfo;
		/** cached application icon */
		Drawable cached_icon;
		/** indicates if the icon has been loaded already */
		boolean icon_loaded;
		/** first time seen? */
		boolean firstseen;

		public DroidApp() {
		}

		public DroidApp(int uid, String name, boolean selected_wifi,
				boolean selected_3g, boolean selected_roaming,
				boolean selected_vpn, boolean selected_lan,
				boolean selected_input_wifi) {
			this.uid = uid;
			this.names = new ArrayList<String>();
			this.names.add(name);
			this.selected_wifi = selected_wifi;
			this.selected_3g = selected_3g;
			this.selected_roaming = selected_roaming;
			this.selected_vpn = selected_vpn;
			this.selected_lan = selected_lan;
			this.selected_input_wifi = selected_input_wifi;
		}

		/**
		 * Screen representation of this application
		 */
		@Override
		public String toString() {
			if (tostr == null) {
				final StringBuilder s = new StringBuilder();
				if (uid > 0)
					s.append(uid + ": ");
				for (int i = 0; i < names.size(); i++) {
					if (i != 0)
						s.append(", ");
					s.append(names.get(i));
				}
				s.append("\n");
				tostr = s.toString();
			}
			return tostr;
		}
	}

	/**
	 * Small internal structure used to hold log information
	 */
	private static final class LogInfo {
		private int totalBlocked; // Total number of packets blocked
		private HashMap<String, Integer> dstBlocked; // Number of packets
														// blocked per
														// destination IP
														// address
		private HashMap<String, Integer> srcBlocked;
		private HashMap<String, Integer> dptBlocked;
		private HashMap<String, Integer> protoBlocked;

		private LogInfo() {
			this.dstBlocked = new HashMap<String, Integer>();
			this.srcBlocked = new HashMap<String, Integer>();
			this.dptBlocked = new HashMap<String, Integer>();
			this.protoBlocked = new HashMap<String, Integer>();
		}
	}

	/**
	 * Internal thread used to execute scripts (as root or not).
	 */
	private static class applyIptableRules extends
			AsyncTask<Object, String, Integer> {

		private int exitcode = -1;

		@Override
		protected Integer doInBackground(Object... parameters) {
			final String script = (String) parameters[0];
			final StringBuilder resources = (StringBuilder) parameters[1];
			final String[] commands = script.split("\n");
			try {
				// check for SU
				if (!Shell.SU.available())
					return exitcode;
				if (script != null && script.length() > 0) {
					// apply the rules
					List<String> rules = Shell.SU.run(commands);
					if (rules != null && rules.size() > 0) {
						for (String script2 : rules) {
							resources.append(script2);
							resources.append("\n");
						}
					}
					exitcode = 0;
				}
			} catch (Exception e) {
				if (resources != null)
					resources.append("\n" + e);
			}
			return exitcode;
		}
	}

	/**
	 * Remove cache file when file uninstalled so apps appear at top of list if
	 * they are reinstalled.
	 */
	public static void updateCacheLabel(String pkgname, Context ctx) {
		String cachelabel = "cache.label." + pkgname;
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		String appname = prefs.getString(cachelabel, "");
		if (appname.length() > 0) {
			prefs.edit().remove(cachelabel).commit();
		}
	}

}
