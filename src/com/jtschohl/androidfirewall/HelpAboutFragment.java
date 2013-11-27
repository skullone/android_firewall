/**
 * Dialog displayed when the "Help" menu option is selected
 * 
 * Copyright (C) 2011-2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * This Help file code came from the wonderful AdAway application
 * https://github.com/dschuermann/ad-away/blob/master/AdAway/src/main/java/org/adaway/ui/HelpFragmentHtml.java
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
 * @author Dominik Schürmann
 * @author Jason Tschohl
 * @version 1.0
 */
package com.jtschohl.androidfirewall;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class HelpAboutFragment extends SherlockFragment {

	final static String TAG = "{AF}";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the view from fragment1.xml
		View view = inflater.inflate(R.layout.help_about_fragment, container,
				false);

		TextView version = (TextView) view.findViewById(R.id.about_version);
		version.setText(getString(R.string.version_about) + " " + getVersion());
		
		return view;
	}
	
	/**
	 * Get app version
	 */
	
	private String getVersion(){
		String result = "";
		try{
			PackageManager manager = getActivity().getPackageManager();
			PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
			result = String.format("%s", info.versionName, info.versionCode);
		} catch (NameNotFoundException e){
			Log.w(TAG, "Unable to get application version: " + e.getMessage());
			result = "Unable to get application version.";
		}
		
		return result;
	}

}