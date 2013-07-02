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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.Shell.SU;

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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Contains shared programming interfaces. All iptables "communication" is
 * handled by this class.
 */
public final class Api {
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
	public static final String PREF_SDCARD = "SDCard";

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

	// Cached applications
	public static DroidApp applications[] = null;

	// Do we have root access?
	// private static boolean hasroot = false;

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
		final String myiptables = dir + "/iptables_armv5";
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
			List<Integer> uidsroaming, List<Integer> uidsvpn, boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		assertBinaries(ctx, showErrors);
		final String ITFS_WIFI[] = { "tiwlan+", "wlan+", "eth+", "ra+",
				"wlan0+", "eth0+" };
		final String ITFS_3G[] = { "rmnet+", "pdp+", "ppp+", "uwbr+", "wimax+",
				"vsnet+", "ccmni+", "usb+", "rmnet1+", "rmnet_sdio+",
				"rmnet_sdio0+", "rmnet_sdio1+", "qmi+", "wwan0+", "svnet0+",
				"rmnet0+", "cdma_rmnet+", "rmnet_usb0+", "rment_usb+" };
		final String ITFS_VPN[] = { "tun+", "tun0+", "ppp+", "ppp0+", "bnep0" };
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		final boolean whitelist = prefs.getString(PREF_MODE, MODE_WHITELIST)
				.equals(MODE_WHITELIST);
		final boolean blacklist = !whitelist;
		final boolean logenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_LOGENABLED, false);
		final boolean vpnenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_VPNENABLED, false);
		final boolean roamenabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_ROAMENABLED, false);
		final boolean ipv6enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_IP6TABLES, false);
		final boolean enabled = ctx.getSharedPreferences(PREFS_NAME, 0)
				.getBoolean(PREF_ENABLED, false);
		final String customScript = ctx.getSharedPreferences(Api.PREFS_NAME, 0)
				.getString(Api.PREF_CUSTOMSCRIPT, "");

		final StringBuilder script = new StringBuilder();
		try {
			int code;
			script.append(scriptHeader(ctx));
			script.append(""
					+ "dmesg -c >/dev/null || exit\n"
					+ "$IPTABLES --version || exit 1\n"
					+ "# Create the droidwall chains if necessary\n"
					+ "$IPTABLES -L droidwall >/dev/null 2>/dev/null || $IPTABLES --new droidwall || exit 3\n"
					+ "$IPTABLES -L droidwall-3g >/dev/null 2>/dev/null || $IPTABLES --new droidwall-3g || exit 4\n"
					+ "$IPTABLES -L droidwall-wifi >/dev/null 2>/dev/null || $IPTABLES --new droidwall-wifi || exit 5\n"
					+ "$IPTABLES -L droidwall-reject >/dev/null 2>/dev/null || $IPTABLES --new droidwall-reject || exit 6\n"
					+ "$IPTABLES -L droidwall-vpn >/dev/null 2>/dev/null || $IPTABLES --new droidwall-vpn || exit 7 \n"
					+ "# Add droidwall chain to OUTPUT chain if necessary\n"
					+ "$IPTABLES -L OUTPUT | $GREP -q droidwall || $IPTABLES -A OUTPUT -j droidwall || exit 11\n"
					+ "# Flush existing rules\n"
					+ "$IPTABLES -F droidwall || exit 17\n"
					+ "$IPTABLES -F droidwall-3g || exit 18\n"
					+ "$IPTABLES -F droidwall-wifi || exit 19\n"
					+ "$IPTABLES -F droidwall-reject || exit 20\n"
					+ "$IPTABLES -F droidwall-vpn || exit 20\n"
					+ "# Create reject rule and fix for WiFi slow DNS lookups"
					+ "$IPTABLES -A droidwall-reject -j REJECT || exit 21\n"
					+ "$IPTABLES -A droidwall -m owner --uid-owner 0 -p udp --dport 53 -j RETURN || exit 22\n"
					+ "$IPTABLES -D OUTPUT -j droidwall || exit 11\n"
					+ "$IPTABLES -I OUTPUT 1 -j droidwall || exit 12\n" + "");
			// Check if logging is enabled
			if (logenabled) {
				script.append(""
						+ "# Create the log and reject rules (ignore errors on the LOG target just in case it is not available)\n"
						+ "$IPTABLES -A droidwall-reject -j LOG --log-prefix \"[AndroidFirewall] \" --log-level 4 --log-uid\n"
						+ "$IPTABLES -A droidwall-reject -j REJECT || exit 29\n"
						+ "");
			} else {
				script.append(""
						+ "# Create the reject rule (log disabled)\n"
						+ "$IPTABLES -A droidwall-reject -j REJECT || exit 30\n"
						+ "");
			}
			if (customScript.length() > 0) {
				script.append("\n# BEGIN OF CUSTOM SCRIPT (user-defined)\n");
				script.append(customScript);
				script.append("\n# END OF CUSTOM SCRIPT (user-defined)\n\n");
			}
			script.append("# Main rules (per interface)\n");
			for (final String itf : ITFS_3G) {
				script.append("$IPTABLES -A droidwall -o ").append(itf)
						.append(" -j droidwall-3g || exit 32\n");
			}
			for (final String itf : ITFS_WIFI) {
				script.append("$IPTABLES -A droidwall -o ").append(itf)
						.append(" -j droidwall-wifi || exit 34\n");
			}
			for (final String itf : ITFS_VPN) {
				script.append("$IPTABLES -A droidwall -o ").append(itf)
						.append(" -j droidwall-vpn || exit 34\n");
			}
			script.append("# Filtering rules\n");
			final String targetRule = (whitelist ? "RETURN"
					: "droidwall-reject");
			final boolean any_3g = uids3g.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_wifi = uidsWifi.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_vpn = uidsvpn.indexOf(SPECIAL_UID_ANY) >= 0;
			if (whitelist && !any_wifi) {
				// When "white listing" wifi, we need to ensure that the dhcp
				// and wifi users are allowed
				int uid = android.os.Process.getUidForName("dhcp");
				if (uid != -1) {
					script.append("# dhcp user\n");
					script.append(
							"$IPTABLES -A droidwall-wifi -m owner --uid-owner ")
							.append(uid).append(" -j RETURN || exit 36\n");
				}
				uid = android.os.Process.getUidForName("wifi");
				if (uid != -1) {
					script.append("# wifi user\n");
					script.append(
							"$IPTABLES -A droidwall-wifi -m owner --uid-owner ")
							.append(uid).append(" -j RETURN || exit 38\n");
				}
			}
			if (any_3g) {
				if (blacklist) {
					/* block any application on this interface */
					script.append("$IPTABLES -A droidwall-3g -j ")
							.append(targetRule).append(" || exit 40\n");
				}
			} else {
				/* release/block individual applications on this interface */
				if (isRoaming(ctx) && roamenabled) {
					for (final Integer uid : uidsroaming) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I droidwall-3g -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 50\n");
					}
				} else {
					for (final Integer uid : uids3g) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I droidwall-3g -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
			}
			script.append("$IPTABLES -I droidwall-3g -m owner --uid-owner 9999 -j RETURN || exit 9999\n");
			if (any_wifi) {
				if (blacklist) {
					/* block any application on this interface */
					script.append("$IPTABLES -A droidwall-wifi -j ")
							.append(targetRule).append(" || exit 44\n");
					// script.append("$IP6TABLES -A droidwall-wifi -j ").append(targetRule).append(" || exit 45\n");
				}
			} else {
				/* release/block individual applications on this interface */
				for (final Integer uid : uidsWifi) {
					if (uid >= 0)
						script.append(
								"$IPTABLES -A droidwall-wifi -m owner --uid-owner ")
								.append(uid).append(" -j ").append(targetRule)
								.append(" || exit 46\n");
				}
			}
			if (whitelist) {
				if (!any_3g) {
					if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to allow kernel packets on white-list\n");
						script.append("$IPTABLES -A droidwall-3g -m owner --uid-owner 0:999999999 -j droidwall-reject || exit 48\n");
					} else {
						script.append("$IPTABLES -A droidwall-3g -j droidwall-reject || exit 50\n");

					}
				}
				if (!any_wifi) {
					if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to allow kernel packets on white-list\n");
						script.append("$IPTABLES -A droidwall-wifi -m owner --uid-owner 0:999999999 -j droidwall-reject || exit 52\n");

					} else {
						script.append("$IPTABLES -A droidwall-wifi -j droidwall-reject || exit 54\n");
					}
				}
			} else {
				if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
					script.append("# hack to BLOCK kernel packets on black-list\n");
					script.append("$IPTABLES -A droidwall-3g -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
					script.append("$IPTABLES -A droidwall-3g -j droidwall-reject || exit 57\n");
				}
				if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
					script.append("# hack to BLOCK kernel packets on black-list\n");
					script.append("$IPTABLES -A droidwall-wifi -m owner --uid-owner 0:999999999 -j RETURN || exit 60\n");
					script.append("$IPTABLES -A droidwall-wifi -j droidwall-reject || exit 61\n");
				}
			}
			if (vpnenabled) {
				if (any_vpn && vpnenabled) {
					if (blacklist) {
						/* block any application on this interface */
						script.append("$IPTABLES -A droidwall-vpn -j ")
								.append(targetRule).append(" || exit 40\n");
					}
				} else {
					/* release/block individual applications on this interface */
					for (final Integer uid : uidsvpn) {
						if (uid >= 0)
							script.append(
									"$IPTABLES -I droidwall-vpn -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 42\n");
					}
				}
				if (whitelist && vpnenabled) {
					if (!any_vpn) {
						if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IPTABLES -A droidwall-vpn -m owner --uid-owner 0:999999999 -j droidwall-reject || exit 48\n");
						} else {
							script.append("$IPTABLES -A droidwall-vpn -j droidwall-reject || exit 50\n");

						}
					} else {
						script.append("$IPTABLES -A droidwall-vpn -j droidwall-reject || exit 54\n");
					}
				} else {
					if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IPTABLES -A droidwall-vpn -m owner --uid-owner 0:999999999 -j RETURN || exit 56\n");
						script.append("$IPTABLES -A droidwall-vpn -j droidwall-reject || exit 57\n");
					}
				}
			}
			if (ipv6enabled) {
				{
					script.append(scriptHeader(ctx));
					script.append(""
							+ "$IP6TABLES --version || exit 60\n"
							+ "# Create the droidwall chains if necessary\n"
							+ "$IP6TABLES -L droidwall >/dev/null 2>/dev/null || $IP6TABLES --new droidwall || exit 61\n"
							+ "$IP6TABLES -L droidwall-3g >/dev/null 2>/dev/null || $IP6TABLES --new droidwall-3g || exit 64\n"
							+ "$IP6TABLES -L droidwall-wifi >/dev/null 2>/dev/null || $IP6TABLES --new droidwall-wifi || exit 65\n"
							+ "$IP6TABLES -L droidwall-reject >/dev/null 2>/dev/null || $IP6TABLES --new droidwall-reject || exit 66\n"
							+ "$IP6TABLES -L droidwall-vpn >/dev/null 2>/dev/null || $IP6TABLES --new droidwall-vpn || exit 66\n"
							+ "# Add droidwall chain to OUTPUT chain if necessary\n"
							+ "$IP6TABLES -L OUTPUT | $GREP -q droidwall || $IP6TABLES -A OUTPUT -j droidwall || exit 67\n"
							+ "# Flush existing rules\n"
							+ "$IP6TABLES -F droidwall || exit 70\n"
							+ "$IP6TABLES -F droidwall-3g || exit 71\n"
							+ "$IP6TABLES -F droidwall-wifi || exit 72\n"
							+ "$IP6TABLES -F droidwall-reject || exit 73\n"
							+ "$IP6TABLES -F droidwall-vpn || exit 73\n"
							+ "# Create reject rule and fix for WiFi slow DNS lookups"
							+ "$IP6TABLES -A droidwall-reject -j REJECT || exit 74\n"
							+ "$IP6TABLES -A droidwall -m owner --uid-owner 0 -p udp --dport 53 -j RETURN || exit 75\n"
							+ "$IP6TABLES -D OUTPUT -j droidwall || exit 68\n"
							+ "$IP6TABLES -I OUTPUT 1 -j droidwall || exit 69\n"
							+ "");
					// Check if logging is enabled
					if (logenabled && ipv6enabled) {
						script.append(""
								+ "# Create the log and reject rules (ignore errors on the LOG target just in case it is not available)\n"
								+ "$IP6TABLES -A droidwall-reject -j LOG --log-prefix \"[AndroidFirewall] \" --log-level 4 --log-uid\n"
								+ "$IP6TABLES -A droidwall-reject -j REJECT || exit 76\n"
								+ "");
					} else {
						script.append(""
								+ "# Create the reject rule (log disabled)\n"
								+ "$IP6TABLES -A droidwall-reject -j REJECT || exit 77\n"
								+ "");
					}

					script.append("# Main rules (per interface)\n");
					for (final String itf : ITFS_3G) {
						script.append("$IP6TABLES -A droidwall -o ")
								.append(itf)
								.append(" -j droidwall-3g || exit 78\n");

					}
					for (final String itf : ITFS_WIFI) {
						script.append("$IP6TABLES -A droidwall -o ")
								.append(itf)
								.append(" -j droidwall-wifi || exit 79\n");

					}
					for (final String itf : ITFS_VPN) {
						script.append("$IP6TABLES -A droidwall -o ")
								.append(itf)
								.append(" -j droidwall-vpn || exit 79\n");

					}
					int uid = android.os.Process.getUidForName("dhcp");
					if (uid != -1) {
						script.append("# dhcp user\n");
						script.append(
								"$IP6TABLES -A droidwall-wifi -m owner --uid-owner ")
								.append(uid).append(" -j RETURN || exit 80\n");

					}
					uid = android.os.Process.getUidForName("wifi");
					if (uid != -1) {
						script.append("# wifi user\n");
						script.append(
								"$IP6TABLES -A droidwall-wifi -m owner --uid-owner ")
								.append(uid).append(" -j RETURN || exit 81\n");

					}
				}
				if (any_3g && ipv6enabled) {
					if (blacklist) {
						// block any application on this interface
						script.append("$IP6TABLES -A droidwall-3g -j ")
								.append(targetRule).append(" || exit 82\n");
					}
				} else {
					/* release/block individual applications on this interface */
					if (isRoaming(ctx) && ipv6enabled && roamenabled) {
						for (final Integer uid : uidsroaming) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I droidwall-3g -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 83\n");
						}
					} else {
						for (final Integer uid : uids3g) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I droidwall-3g -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 84\n");
						}
					}
				}
				script.append("$IP6TABLES -I droidwall-3g -m owner --uid-owner 9999 -j RETURN || exit 9999\n");
				if (any_wifi && ipv6enabled) {
					if (blacklist) {
						// block any application on this interface
						script.append("$IP6TABLES -A droidwall-wifi -j ")
								.append(targetRule).append(" || exit 85\n");
					}
				} else {
					// release/block individual applications on this interface
					for (final Integer uid : uidsWifi) {
						if (uid >= 0)
							script.append(
									"$IP6TABLES -A droidwall-wifi -m owner --uid-owner ")
									.append(uid).append(" -j ")
									.append(targetRule).append(" || exit 86\n");
					}
				}
				if (whitelist && ipv6enabled) {
					if (!any_3g) {
						if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IP6TABLES -A droidwall-3g -m owner --uid-owner 0:999999999 -j droidwall-reject || exit 87\n");
						} else {
							script.append("$IP6TABLES -A droidwall-3g -j droidwall-reject || exit 88\n");
						}
					}
					if (!any_wifi && ipv6enabled) {
						if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to allow kernel packets on white-list\n");
							script.append("$IP6TABLES -A droidwall-wifi -m owner --uid-owner 0:999999999 -j droidwall-reject || exit 89\n");
						} else {
							script.append("$IP6TABLES -A droidwall-wifi -j droidwall-reject || exit 90\n");
						}
					}
				} else {
					if (uids3g.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IP6TABLES -A droidwall-3g -m owner --uid-owner 0:999999999 -j RETURN || exit 91\n");
						script.append("$IP6TABLES -A droidwall-3g -j droidwall-reject || exit 92\n");
					}
					if (uidsWifi.indexOf(SPECIAL_UID_KERNEL) >= 0) {
						script.append("# hack to BLOCK kernel packets on black-list\n");
						script.append("$IP6TABLES -A droidwall-wifi -m owner --uid-owner 0:999999999 -j RETURN || exit 93\n");
						script.append("$IP6TABLES -A droidwall-wifi -j droidwall-reject || exit 94\n");
					}
				}
				if (vpnenabled && ipv6enabled) {
					if (any_vpn && ipv6enabled) {
						if (blacklist) {
							// block any application on this interface
							script.append("$IP6TABLES -A droidwall-vpn -j ")
									.append(targetRule).append(" || exit 82\n");
						}
					} else {
						/*
						 * release/block individual applications on this
						 * interface
						 */
						for (final Integer uid : uidsvpn) {
							if (uid >= 0)
								script.append(
										"$IP6TABLES -I droidwall-vpn -m owner --uid-owner ")
										.append(uid).append(" -j ")
										.append(targetRule)
										.append(" || exit 84\n");
						}
					}
					if (whitelist && ipv6enabled && vpnenabled) {
						if (!any_vpn) {
							if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
								script.append("# hack to allow kernel packets on white-list\n");
								script.append("$IP6TABLES -A droidwall-vpn -m owner --uid-owner 0:999999999 -j droidwall-reject || exit 87\n");
							} else {
								script.append("$IP6TABLES -A droidwall-vpn -j droidwall-reject || exit 88\n");
							}
						}
					} else {
						if (uidsvpn.indexOf(SPECIAL_UID_KERNEL) >= 0) {
							script.append("# hack to BLOCK kernel packets on black-list\n");
							script.append("$IP6TABLES -A droidwall-vpn -m owner --uid-owner 0:999999999 -j RETURN || exit 91\n");
							script.append("$IP6TABLES -A droidwall-vpn -j droidwall-reject || exit 92\n");
						}
					}
				}
			}
			final StringBuilder res = new StringBuilder();
			code = runScriptAsRoot(ctx, script.toString(), res);
			if (showErrors && code != 0) {
				String msg = res.toString();
				Log.e("AndroidFirewall", msg);
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
				Log.d("Android Firewall - error applying rules", e.getMessage());
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
						Log.d("Android Firewall - error with WiFi UIDs",
								ex.getMessage());
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
						Log.d("Android Firewall - error with Data UIDs",
								ex.getMessage());
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
						Log.d("Android Firewall - error with Roaming UIDs",
								ex.getMessage());
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
						Log.d("Android Firewall - error with Data UIDs",
								ex.getMessage());
					}
				}
			}
		}
		return applyIptablesRulesImpl(ctx, uids_wifi, uids_3g, uids_roaming,
				uids_vpn, showErrors);
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
		final DroidApp[] apps = getApps(ctx);
		// Builds a pipe-separated list of names
		final StringBuilder newuids_wifi = new StringBuilder();
		final StringBuilder newuids_3g = new StringBuilder();
		final StringBuilder newuids_roaming = new StringBuilder();
		final StringBuilder newuids_vpn = new StringBuilder();
		for (int i = 0; i < apps.length; i++) {
			if (apps[i].selected_wifi) {
				if (newuids_wifi.length() != 0)
					newuids_wifi.append('|');
				newuids_wifi.append(apps[i].uid);
			}
			if (apps[i].selected_3g) {
				if (newuids_3g.length() != 0)
					newuids_3g.append('|');
				newuids_3g.append(apps[i].uid);
			}
			if (apps[i].selected_roaming) {
				if (newuids_roaming.length() != 0)
					newuids_roaming.append('|');
				newuids_roaming.append(apps[i].uid);
			}
			if (apps[i].selected_vpn) {
				if (newuids_vpn.length() != 0)
					newuids_vpn.append('|');
				newuids_vpn.append(apps[i].uid);
			}
		}
		// save the new list of UIDs
		final Editor edit = prefs.edit();
		edit.putString(PREF_WIFI_UIDS, newuids_wifi.toString());
		edit.putString(PREF_3G_UIDS, newuids_3g.toString());
		edit.putString(PREF_ROAMING_UIDS, newuids_roaming.toString());
		edit.putString(PREF_VPN_UIDS, newuids_vpn.toString());
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
			script.append(scriptHeader(ctx));
			script.append("" + "$IPTABLES -F droidwall\n"
					+ "$IPTABLES -F droidwall-reject\n"
					+ "$IPTABLES -F droidwall-3g\n"
					+ "$IPTABLES -F droidwall-vpn\n"
					+ "$IPTABLES -F droidwall-wifi\n" + "");
			if (ipv6enabled) {
				script.append(scriptHeader(ctx));
				script.append("" + "$IP6TABLES --flush droidwall\n"
						+ "$IP6TABLES --flush droidwall-reject\n"
						+ "$IP6TABLES --flush droidwall-3g\n"
						+ "$IP6TABLES --flush droidwall-vpn\n"
						+ "$IP6TABLES --flush droidwall-wifi\n" + "");
			}
			if (customScript.length() > 0) {
				script.append("\n# BEGIN OF CUSTOM SCRIPT (user-defined)\n");
				script.append(customScript);
				script.append("\n# END OF CUSTOM SCRIPT (user-defined)\n\n");
			}
			int code = runScriptAsRoot(ctx, script.toString(), res);
			if (code == -1) {
				if (showErrors)
					alert(ctx, "Error purging iptables. exit code: " + code
							+ "\n" + res);
				return false;
			}
			return true;
		} catch (Exception e) {
			if (showErrors)
				alert(ctx, "Error purging iptables: " + e);
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
			script.append(scriptHeader(ctx));
			script.append("" + "$IP6TABLES --flush droidwall\n"
					+ "$IP6TABLES --flush droidwall-reject\n"
					+ "$IP6TABLES --flush droidwall-3g\n"
					+ "$IP6TABLES --flush droidwall-vpn\n"
					+ "$IP6TABLES --flush droidwall-wifi\n" + "");
			if (customScript.length() > 0) {
				script.append("\n# BEGIN OF CUSTOM SCRIPT (user-defined)\n");
				script.append(customScript);
				script.append("\n# END OF CUSTOM SCRIPT (user-defined)\n\n");
			}
			int code = runScriptAsRoot(ctx, script.toString(), res);
			if (code == -1) {
				if (showErrors)
					alert(ctx, "Error purging ip6tables. exit code: " + code
							+ "\n" + res);
				return false;
			}
			return true;
		} catch (Exception e) {
			if (showErrors)
				alert(ctx, "Error purging ip6tables: " + e);
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
			Log.d("Android Firewall - error showing rules", e.getMessage());
			alert(ctx, "error: " + e);
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
		try {
			final StringBuilder res = new StringBuilder();
			int code = runScriptAsRoot(ctx, "dmesg -c >/dev/null || exit\n",
					res);
			if (code != 0) {
				alert(ctx, res);
				return false;
			}
			return true;
		} catch (Exception e) {
			Log.d("Android Firewall - error clearing the logs", e.getMessage());
			alert(ctx, "error: " + e);
		}
		return false;
	}

	/**
	 * Display logs
	 * 
	 * @param ctx
	 *            application context
	 */
	public static String showLog(Context ctx) {
		try {
			StringBuilder res = new StringBuilder();
			StringBuilder output = new StringBuilder();
			int code = runScriptAsRoot(ctx, scriptHeader(ctx)
					+ "dmesg | $GREP [AndroidFirewall]\n", res);
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
			final HashMap<Integer, LogInfo> map = new HashMap<Integer, LogInfo>();
			LogInfo loginfo = null;
			while ((line = r.readLine()) != null) {
				if (line.indexOf("[AndroidFirewall]") == -1)
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
			}
			final DroidApp[] apps = getApps(ctx);
			for (Integer id : map.keySet()) {
				res.append("App ID ");
				if (id != unknownUID) {
					res.append(id);
					for (DroidApp app : apps) {
						if (app.uid == id) {
							res.append(" (").append(app.names[0]);
							if (app.names.length > 1) {
								res.append(", ...)");
							} else {
								res.append(")");
							}
							break;
						}
					}
				} else {
					res.append("(kernel)");
				}
				loginfo = map.get(id);
				res.append(" - Blocked ").append(loginfo.totalBlocked)
						.append(" packets");
				if (loginfo.dstBlocked.size() > 0) {
					res.append(" (");
					boolean first = true;
					for (String dst : loginfo.dstBlocked.keySet()) {
						if (!first) {
							res.append(", ");
						}
						res.append(loginfo.dstBlocked.get(dst))
								.append(" packets for ").append(dst);
						first = false;
					}
					res.append(")");
				}
				res.append("\n\n");
			}
			if (res.length() == 0) {
				res.append(ctx.getString(R.string.log_empty));
			}
			return res.toString();
		} catch (Exception e) {
			Log.d("Android Firewall - error showing the logs", e.getMessage());
			alert(ctx, "error: " + e);
		}
		return "";
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
	 * @param ctx
	 *            application context (mandatory)
	 * @return a list of applications
	 */
	public static DroidApp[] getApps(Context ctx) {
		if (applications != null) {
			// return cached instance
			return applications;
		}
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		// allowed application names separated by pipe '|' (persisted)
		final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
		final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
		final String savedUids_Roaming = prefs.getString(PREF_ROAMING_UIDS, "");
		final String savedUids_Vpn = prefs.getString(PREF_VPN_UIDS, "");
		int selected_wifi[] = new int[0];
		int selected_3g[] = new int[0];
		int selected_roaming[] = new int[0];
		int selected_vpn[] = new int[0];
		if (savedUids_wifi.length() > 0) {
			// Check which applications are allowed
			final StringTokenizer tok = new StringTokenizer(savedUids_wifi, "|");
			selected_wifi = new int[tok.countTokens()];
			for (int i = 0; i < selected_wifi.length; i++) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						selected_wifi[i] = Integer.parseInt(uid);
					} catch (Exception ex) {
						selected_wifi[i] = -1;
					}
				}
			}
			// Sort the array to allow using "Arrays.binarySearch" later
			Arrays.sort(selected_wifi);
		}
		if (savedUids_3g.length() > 0) {
			// Check which applications are allowed
			final StringTokenizer tok = new StringTokenizer(savedUids_3g, "|");
			selected_3g = new int[tok.countTokens()];
			for (int i = 0; i < selected_3g.length; i++) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						selected_3g[i] = Integer.parseInt(uid);
					} catch (Exception ex) {
						selected_3g[i] = -1;
					}
				}
			}
			// Sort the array to allow using "Arrays.binarySearch" later
			Arrays.sort(selected_3g);
		}
		if (savedUids_Roaming.length() > 0) {
			// Check which applications are allowed
			final StringTokenizer tok = new StringTokenizer(savedUids_Roaming,
					"|");
			selected_roaming = new int[tok.countTokens()];
			for (int i = 0; i < selected_roaming.length; i++) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						selected_roaming[i] = Integer.parseInt(uid);
					} catch (Exception ex) {
						selected_roaming[i] = -1;
					}
				}
			}
			// Sort the array to allow using "Arrays.binarySearch" later
			Arrays.sort(selected_roaming);
		}
		if (savedUids_Vpn.length() > 0) {
			// Check which applications are allowed
			final StringTokenizer tok = new StringTokenizer(savedUids_Vpn, "|");
			selected_vpn = new int[tok.countTokens()];
			for (int i = 0; i < selected_vpn.length; i++) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						selected_vpn[i] = Integer.parseInt(uid);
					} catch (Exception ex) {
						selected_vpn[i] = -1;
					}
				}
			}
			// Sort the array to allow using "Arrays.binarySearch" later
			Arrays.sort(selected_vpn);
		}
		try {
			final PackageManager pkgmanager = ctx.getPackageManager();
			final List<ApplicationInfo> installed = pkgmanager
					.getInstalledApplications(0);
			final HashMap<Integer, DroidApp> map = new HashMap<Integer, DroidApp>();
			final Editor edit = prefs.edit();
			boolean changed = false;
			String name = null;
			String cachekey = null;
			DroidApp app = null;
			for (final ApplicationInfo apinfo : installed) {
				boolean firstseen = false;
				app = map.get(apinfo.uid);
				// filter applications which are not allowed to access the
				// Internet
				if (app == null
						&& PackageManager.PERMISSION_GRANTED != pkgmanager
								.checkPermission(Manifest.permission.INTERNET,
										apinfo.packageName)) {
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
					app.names = new String[] { name };
					app.appinfo = apinfo;
					map.put(apinfo.uid, app);
				} else {
					final String newnames[] = new String[app.names.length + 1];
					System.arraycopy(app.names, 0, newnames, 0,
							app.names.length);
					newnames[app.names.length] = name;
					app.names = newnames;
				}
				app.firstseen = firstseen;
				// check if this application is selected
				if (!app.selected_wifi
						&& Arrays.binarySearch(selected_wifi, app.uid) >= 0) {
					app.selected_wifi = true;
				}
				if (!app.selected_3g
						&& Arrays.binarySearch(selected_3g, app.uid) >= 0) {
					app.selected_3g = true;
				}
				if (!app.selected_roaming
						&& Arrays.binarySearch(selected_roaming, app.uid) >= 0) {
					app.selected_roaming = true;
				}
				if (!app.selected_vpn
						&& Arrays.binarySearch(selected_vpn, app.uid) >= 0) {
					app.selected_vpn = true;
				}
			}
			if (changed) {
				edit.commit();
			}
			/* add special applications to the list */
			final DroidApp special[] = {
					new DroidApp(
							SPECIAL_UID_ANY,
							"(Any application) - Same as selecting all applications",
							false, false, false, false),
					new DroidApp(SPECIAL_UID_KERNEL, "(Kernel) - Linux kernel",
							false, false, false, false),
					new DroidApp(android.os.Process.getUidForName("root"),
							"(root) - Applications running as root", false,
							false, false, false),
					new DroidApp(android.os.Process.getUidForName("media"),
							"Media server", false, false, false, false),
					new DroidApp(android.os.Process.getUidForName("vpn"),
							"VPN networking", false, false, false, false),
					new DroidApp(android.os.Process.getUidForName("shell"),
							"Linux shell", false, false, false, false),
					new DroidApp(android.os.Process.getUidForName("gps"),
							"GPS", false, false, false, false), };
			for (int i = 0; i < special.length; i++) {
				app = special[i];
				if (app.uid != -1 && !map.containsKey(app.uid)) {
					// check if this application is allowed
					if (Arrays.binarySearch(selected_wifi, app.uid) >= 0) {
						app.selected_wifi = true;
					}
					if (Arrays.binarySearch(selected_3g, app.uid) >= 0) {
						app.selected_3g = true;
					}
					if (Arrays.binarySearch(selected_roaming, app.uid) >= 0) {
						app.selected_roaming = true;
					}
					if (Arrays.binarySearch(selected_vpn, app.uid) >= 0) {
						app.selected_vpn = true;
					}
					map.put(app.uid, app);
				}
			}
			/* convert the map into an array */
			applications = map.values().toArray(new DroidApp[map.size()]);
			;
			return applications;
		} catch (Exception e) {
			Log.d("Android Firewall - error generating list of apps",
					e.getMessage());
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
			}
		}
		return rootaccess;
	}

	private static class checkForRoot extends
			AsyncTask<Object, Object, Integer> {
		private int exitCode = -1;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Object... params) {
			try {
				if (SU.available())
					exitCode = 0;
			} catch (Exception ex) {
			}
			return exitCode;
		}

	}

	/**
	 * Runs a script as root (multiple commands separated by "\n").
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
	 */
	public static int runScriptAsRoot(Context ctx, String script,
			StringBuilder res, long timeout) {
		return runScript(ctx, script, res, timeout, true);
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
			StringBuilder res) throws IOException {
		return runScriptAsRoot(ctx, script, res, 40000);
	}

	/**
	 * Runs a script as a regular user (multiple commands separated by "\n")
	 * with a default timeout of 20 seconds.
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
	public static int runScript(Context ctx, String script, StringBuilder res)
			throws IOException {
		return runScript(ctx, script, res, 40000, false);
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
		try {
			// Check iptables_armv5
			File file = new File(ctx.getDir("bin", 0), "iptables_armv5");
			if (!file.exists() || file.length() != 198652) {
				copyRawFile(ctx, R.raw.iptables_armv5, file, "755");
				changed = true;
			}
			// Check busybox
			file = new File(ctx.getDir("bin", 0), "busybox_g1");
			if (!file.exists()) {
				copyRawFile(ctx, R.raw.busybox_g1, file, "755");
				changed = true;
			}
			if (changed) {
				Toast.makeText(ctx, R.string.toast_bin_installed,
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			if (showErrors)
				alert(ctx, "Error installing binary files: " + e);
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
		final String uid_str = uid + "";
		boolean changed = false;
		// look for the removed application in the "wi-fi" list
		if (savedUids_wifi.length() > 0) {
			final StringBuilder newuids = new StringBuilder();
			final StringTokenizer tok = new StringTokenizer(savedUids_wifi, "|");
			while (tok.hasMoreTokens()) {
				final String token = tok.nextToken();
				if (uid_str.equals(token)) {
					Log.d("Android Firewall", "Removing UID " + token
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
					Log.d("Android Firewall", "Removing UID " + token
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
					Log.d("Android Firewall", "Removing UID " + token
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
					Log.d("Android Firewall", "Removing UID " + token
							+ " from the Roaming list (package removed)!");
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
		String names[];
		/** indicates if this application is selected for wifi */
		boolean selected_wifi;
		/** indicates if this application is selected for 3g */
		boolean selected_3g;
		// indicated if this application is selected for roaming
		boolean selected_roaming;
		// indicates if this application is selected for vpn
		boolean selected_vpn;
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
				boolean selected_vpn) {
			this.uid = uid;
			this.names = new String[] { name };
			this.selected_wifi = selected_wifi;
			this.selected_3g = selected_3g;
			this.selected_roaming = selected_roaming;
			this.selected_vpn = selected_vpn;
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
				for (int i = 0; i < names.length; i++) {
					if (i != 0)
						s.append(", ");
					s.append(names[i]);
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

		private LogInfo() {
			this.dstBlocked = new HashMap<String, Integer>();
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
	 * Runs a script, wither as root or as a regular user (multiple commands
	 * separated by "\n").
	 */
	public static int runScript(Context ctx, String script, StringBuilder res,
			long timeout, boolean asroot) {
		int returncode = -1;
		try {
			returncode = new applyIptableRules().execute(script, res).get();
		} catch (Exception e) {
			Log.d("Android Firewall - error applying iptables in runScript",
					e.getMessage());
			Toast.makeText(ctx, "There was an error applying the iptables.",
					Toast.LENGTH_LONG).show();
		}
		return returncode;
	}
}
