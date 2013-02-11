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

import android.content.Context;
import android.os.Bundle;

/**
 * Many thanks to the Tasker and Locale Development teams for great products and
 * good examples on how to create these plugins.
 * 
 * @author jason
 */

public final class PluginBundleManager {
	/**
	 * Type: {@code String}.
	 * <p>
	 * String message to display in a Toast message.
	 */
	public static final String BUNDLE_EXTRA_STRING_MESSAGE = "com.jtschohl.androidfirewall.extra.CHANGE_PROFILE"; //$NON-NLS-1$
	
	/**
	 * Method to verify the content of the bundle are correct.
	 * <p>
	 * This method will not mutate {@code bundle}.
	 * 
	 * @param bundle
	 *            bundle to verify. May be null, which will always return false.
	 * @return true if the Bundle is valid, false if the bundle is invalid.
	 */
	public static boolean isBundleValid(Bundle bundle) {
		if (null == bundle) {
			return false;
		}
		if (!bundle.containsKey(BUNDLE_EXTRA_STRING_MESSAGE)) {
			return false;
		}
		return true;
	}

	/**
	 * @param context
	 *            Application context.
	 * @param message
	 *            The toast message to be displayed by the plug-in. Cannot be
	 *            null.
	 * @return A plug-in bundle.
	 */
	public static Bundle generateBundle(Context context, String message) {
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_EXTRA_STRING_MESSAGE, message);
		return bundle;
	}

	/**
	 * Private constructor prevents instantiation
	 * 
	 * @throws UnsupportedOperationException
	 *             because this class cannot be instantiated.
	 */
	private PluginBundleManager() {
		throw new UnsupportedOperationException(
				"This class is non-instantiable"); //$NON-NLS-1$
	}
}