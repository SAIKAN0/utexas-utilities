
package com.nasageek.utexasutilities;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.viewpagerindicator.TitleProvider;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

/**
 * The <code>PagerAdapter</code> serves the fragments when paging.
 * @author mwho
 */
public class PagerAdapter extends FragmentPagerAdapter implements TitleProvider {

	
	int pos;
	private List<SherlockFragment> fragments;
	/**
	 * @param fm
	 * @param fragments
	 */
	public PagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return this.fragments.size();
	}
	@Override
	public String getTitle(int position) {
		// TODO Auto-generated method stub
		return this.fragments.get(position).getArguments().getString("title");
	}
	public void updateFragments(List<SherlockFragment> fragments)
	{
		this.fragments=fragments;
	}
}