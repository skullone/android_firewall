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

public class InterfaceInfo {
	// firewall policy
	boolean allowWifi = false;
	String lanipv4 = "";
	String lanipv6 = "";

	// supplementary info
	String wifiName = "";
	boolean netEnabled = false;
	int netType = -1;

	public boolean equals(InterfaceInfo that) {
		if (this.allowWifi != that.allowWifi
				|| !this.lanipv4.equals(that.lanipv4)
				|| !this.lanipv6.equals(that.lanipv6)
				|| !this.wifiName.equals(that.wifiName)
				|| this.netEnabled != that.netEnabled
				|| this.netType != that.netType)
			return false;
		return true;
	}
}