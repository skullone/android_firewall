/**
 * Dialog displayed when the "Help" menu option is selected
 * 
 * Copyright (C) 2011-2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * The Help file code came from the wonderful AdAway application
 * https://github.com/dschuermann/ad-away/blob/master/AdAway/src/main/java/org/adaway/ui/HelpActivity.java
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

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * Dialog displayed when the "Help" menu option is selected
 */
public class HelpDialog extends SherlockFragmentActivity {
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);
		setContentView(mViewPager);
		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayShowTitleEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);

		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(bar.newTab()
				.setText(getString(R.string.usage_help)),
				HelpUsageFragment.class);
		mTabsAdapter.addTab(bar.newTab()
				.setText(getString(R.string.rules_help)),
				HelpRulesFragment.class);
		mTabsAdapter.addTab(
				bar.newTab().setText(getString(R.string.profiles_help)),
				HelpProfilesFragment.class);
		mTabsAdapter.addTab(
				bar.newTab().setText(getString(R.string.faq_help)),
				HelpFaqFragment.class);
		mTabsAdapter.addTab(
				bar.newTab().setText(getString(R.string.changelog_help)),
				HelpChangelogFragment.class);
		mTabsAdapter.addTab(bar.newTab()
				.setText(getString(R.string.about_help)),
				HelpAboutFragment.class);

	}

	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context ctx;
		private final ActionBar bar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;

			TabInfo(Class<?> _class) {
				clss = _class;
			}
		}

		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			ctx = activity;
			bar = activity.getSupportActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss) {
			TabInfo info = new TabInfo(clss);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			bar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(ctx, info.clss.getName());
		}

		public void onPageScrolled(int position, float positionOffset,
				int postionOffsetPixels) {

		}

		public void onPageSelected(int position) {
			bar.setSelectedNavigationItem(position);
		}

		public void onPageScrollStateChanged(int state) {

		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	}
}
