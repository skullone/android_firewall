/**
 * This contains parts of the Tasker/Locale Plugin
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

package com.jtschohl.androidfirewall.plugin;

import android.content.Intent;
import android.os.Bundle;

/**
 * Many thanks to the Tasker and Locale Development teams for great products and
 * good examples on how to create these plugins.
 * 
 * @author jason
 */

public final class BundleScrubber {
	public static boolean scrub(final Intent intent) {
		if (null == intent) {
			return false;
		}
		return scrub(intent.getExtras());
	}

	public static boolean scrub(final Bundle bundle) {
		if (null == bundle) {
			return false;
		}
		try {
			// if a private serializable exists, this will throw an exception
			bundle.containsKey(null);
		} catch (final Exception e) {
			bundle.clear();
			return true;
		}
		return false;
	}
}