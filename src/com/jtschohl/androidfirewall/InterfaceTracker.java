/**
 * Keep track of wifi/3G/tethering status and LAN IP ranges.
 *
 * Copyright (C) 2013 Kevin Cernekee
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
 * @author Kevin Cernekee - Original Author
 * @author Jason Tschohl
 * 
 * @version 1.0
 */

/**
 * Many thanks to Kevin Cernekee for the open source code for the LAN support functionality.
 */

package com.jtschohl.androidfirewall;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public final class InterfaceTracker {

	final static String TAG = "{AF}";
	private static InterfaceInfo currentConfig = null;

	public static final String ITFS_WIFI[] = { "tiwlan+", "wlan+", "eth+",
			"ra+" };
	public static final String ITFS_3G[] = { "rmnet+", "pdp+", "ppp+", "uwbr+",
			"wimax+", "vsnet+", "ccmni+", "usb+", "rmnet_sdio+", "qmi+",
			"wwan+", "svnet+", "cdma_rmnet+", "rmnet_usb+", "bond+", "clat+",
			"cc2mni+" };
	public static final String ITFS_VPN[] = { "tun+", "tun0+" };
	public static final String ITFS_TETHER[] = { "bnep0+", "bt-pan+",
			"rndis0+", "ap0+" };

	private static class OldInterfaceScanner {

		private static String buildIP(int ip) {
			return String.format(Locale.US, "%d.%d.%d.%d", (ip >>> 0) & 0xff,
					(ip >>> 8) & 0xff, (ip >>> 16) & 0xff, (ip >>> 24) & 0xff);
		}

		public static void populateLanInfo(Context context, String[] names,
				InterfaceInfo ret) {
			WifiManager wifi = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcp = wifi.getDhcpInfo();

			if (dhcp != null) {
				ret.lanipv4 = buildIP(dhcp.ipAddress) + "/"
						+ buildIP(dhcp.netmask);
				ret.wifiName = "UNKNOWN";
			}
		}
	}

	private static class NewInterfaceScanner {

		private static String truncAfter(String in, String regexp) {
			return in.split(regexp)[0];
		}

		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public static void populateLanMasks(Context context, String[] names,
				InterfaceInfo ret) {
			try {
				Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces();

				while (en.hasMoreElements()) {
					NetworkInterface intf = en.nextElement();
					boolean match = false;

					if (!intf.isUp() || intf.isLoopback()) {
						continue;
					}

					for (String pattern : ITFS_WIFI) {
						if (intf.getName().startsWith(
								truncAfter(pattern, "\\+"))) {
							match = true;
							break;
						}
					}
					if (!match)
						continue;
					ret.wifiName = intf.getName();

					Iterator<InterfaceAddress> addrList = intf
							.getInterfaceAddresses().iterator();
					while (addrList.hasNext()) {
						InterfaceAddress addr = addrList.next();
						InetAddress ip = addr.getAddress();
						String mask = truncAfter(ip.getHostAddress(), "%")
								+ "/" + addr.getNetworkPrefixLength();

						if (ip instanceof Inet4Address) {
							ret.lanipv4 = mask;
							ret.allowWifi = true;
						} else if (ip instanceof Inet6Address) {
							ret.lanipv6 = mask;
							ret.allowWifi = true;
						}
					}
				}
			} catch (SocketException e) {
				Log.e(TAG, "Error fetching network interface list");
			}
		}
	}

	private static InterfaceInfo getInterfaceDetails(Context context) {
		InterfaceInfo msg = new InterfaceInfo();

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();

		if (info == null || info.isConnected() == false) {
			return msg;
		}

		switch (info.getType()) {
		case ConnectivityManager.TYPE_MOBILE:
		case ConnectivityManager.TYPE_MOBILE_DUN:
		case ConnectivityManager.TYPE_MOBILE_HIPRI:
		case ConnectivityManager.TYPE_MOBILE_MMS:
		case ConnectivityManager.TYPE_MOBILE_SUPL:
		case ConnectivityManager.TYPE_WIMAX:
			msg.netType = ConnectivityManager.TYPE_MOBILE;
			msg.netEnabled = true;
			break;
		case ConnectivityManager.TYPE_WIFI:
		case ConnectivityManager.TYPE_BLUETOOTH:
		case ConnectivityManager.TYPE_ETHERNET:
			msg.netType = ConnectivityManager.TYPE_WIFI;
			msg.netEnabled = true;
			break;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			OldInterfaceScanner.populateLanInfo(context, ITFS_WIFI, msg);
		} else {
			NewInterfaceScanner.populateLanMasks(context, ITFS_WIFI, msg);
		}

		return msg;
	}

	public static boolean checkForNewCfg(Context context) {
		InterfaceInfo newCfg = getInterfaceDetails(context);

		if (currentConfig != null && currentConfig.equals(newCfg)) {
			return false;
		}
		currentConfig = newCfg;

		if (!newCfg.netEnabled) {
			Log.i(TAG, "Now assuming NO connection (all interfaces down)");
		} else {
			if (newCfg.netType == ConnectivityManager.TYPE_WIFI) {
				Log.i(TAG, "Now assuming wifi connection");
			} else if (newCfg.netType == ConnectivityManager.TYPE_MOBILE) {
				Log.i(TAG, "Now assuming 3G connection");
			}

			if (!newCfg.lanipv4.equals("")) {
				Log.i(TAG, "IPv4 LAN netmask on " + newCfg.wifiName + ": "
						+ newCfg.lanipv4);
			}
			if (!newCfg.lanipv6.equals("")) {
				Log.i(TAG, "IPv6 LAN netmask on " + newCfg.wifiName + ": "
						+ newCfg.lanipv6);
			}
		}
		return true;
	}

	public static InterfaceInfo getCurrentCfg(Context context) {
		if (currentConfig == null) {
			currentConfig = getInterfaceDetails(context);
		}
		return currentConfig;
	}
}