package com.nasageek.utexasutilities;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

public class ScheduleActivity extends SherlockFragmentActivity implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener, ViewPager.OnPageChangeListener{
	
	private WrappingSlidingDrawer sd ;
	
	private ImageView ci_iv;
	private TextView ci_tv;
	
	private ActionBar actionbar;
	private classtime current_clt;
	private ActionMode mode;
	protected PagerAdapter mPagerAdapter;
	protected List<SherlockFragment> fragments;
	protected TitlePageIndicator titleIndicator;
	String semId ="";
	int selection;
	Spinner spinner;
	
		
	public enum Semester {
		No_Overlay(Calendar.getInstance().get(Calendar.YEAR)+"2","Spring"),
		JesterCityLimits(Calendar.getInstance().get(Calendar.YEAR)+"6","Summer"),
		JesterCityMarket(Calendar.getInstance().get(Calendar.YEAR)+"9","Fall");
		
	    private String code;
	    private String fullName; 

	    private Semester(String c, String fullName) {
	      code = c;
	      this.fullName = fullName;
	    }
	    public String getCode() {
	      return code;
	    }
	    public String fullName()
	    {
	    	return fullName;
	    }
	    public String toString()
	    {
	    	return fullName;
	    }
	}
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_layout);
		
/*		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		switch (month)
		{
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:semId=cal.get(Calendar.YEAR)+"2";selection=0;break;
		case 5:
		case 6:semId=cal.get(Calendar.YEAR)+"6";selection=1;break;
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:semId=cal.get(Calendar.YEAR)+"9";selection=2;break;
		}*/
		
		initialisePaging();
		
		actionbar = getSupportActionBar();
		actionbar.setTitle("Schedule");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	    actionbar.setHomeButtonEnabled(true);
	    actionbar.setDisplayHomeAsUpEnabled(true);
	    Crittercism.leaveBreadcrumb("Entered ScheduleActivity");
		
/*		spinner = new Spinner(this);
        spinner.setPromptId(R.string.semesterprompt);
        spinner.setSelection(selection);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionbar.getThemedContext(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        
		
        spinner.setAdapter(adapter);
        getSupportActionBar().setListNavigationCallbacks(adapter, new OnNavigationListener() 
        {
        	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        		String semStr = ((String) spinner.getAdapter().getItem(itemPosition));
        		
        		String semId = "20" +semStr.substring(semStr.length()-2);
        		if(semStr.contains("Summer"))
        		{
        			semId+="6";
        		}
        		else if(semStr.contains("Spring"))
        		{
        			semId+="2";
        		}
        		else if(semStr.contains("Fall"))
        		{
        			semId+="9";
        		}
        		 
         			
         		((CourseScheduleFragment)(fragments.get(0))).updateView(semId,fragments.get(0).getView());
         		((ExamScheduleFragment)(fragments.get(1))).updateView(semId,fragments.get(1).getView());

        		return true;
        	}
        });*/
        
		
		}
		private void initialisePaging() 
		{
			
			fragments = new Vector<SherlockFragment>();
			
			Bundle args = new Bundle(2);
		    args.putString("title", "Exam Schedule");
		    args.putString("semId", semId);
		    fragments.add((SherlockFragment)SherlockFragment.instantiate(this, ExamScheduleFragment.class.getName(), args));
		
			
			args = new Bundle(2);
	        args.putString("title", "Current Schedule");
	        args.putString("semId", semId);
	        fragments.add((SherlockFragment)SherlockFragment.instantiate(this, CourseScheduleFragment.class.getName(), args));
	      
	        this.mPagerAdapter  = new PagerAdapter(getSupportFragmentManager(), fragments);	
	        ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
	        pager.setPageMargin(2);
	        pager.setOffscreenPageLimit(2);
	        pager.setAdapter(this.mPagerAdapter);
	        
	        titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
			titleIndicator.setViewPager(pager);
			
			titleIndicator.setOnPageChangeListener(this);
			pager.setCurrentItem(1,false);
						
	   }
		@Override
		public void onPause()
		{
			super.onPause();
		}
		
		public boolean onOptionsItemSelected(MenuItem item)
	    {
	    	int id = item.getItemId();
	    	switch(id)
	    	{
	    	case android.R.id.home:
	            // app icon in action bar clicked; go home
	            super.onBackPressed();
	            break;
	    	
	    	default: return super.onOptionsItemSelected(item);
	    	}
	    	return true;
	    }
		public void onDrawerClosed()
		{
			((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_expand_half);
		}
		public void onDrawerOpened()
		{
			((ImageView)(sd.getHandle())).setImageResource(R.drawable.ic_collapse_half);
		}
		private final class ScheduleActionMode extends ViewPager.SimpleOnPageChangeListener implements ActionMode.Callback {
	        @Override
	        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	            mode.setTitle("Class Info");
	            MenuInflater inflater = getSupportMenuInflater();
	            inflater.inflate(R.menu.schedule_menu, menu);
	            return true;
	        }

	        @Override
	        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	            return false;
	        }

	        @Override
	        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	            switch(item.getItemId())
	            {
	            	case R.id.locate_class:
	            		Intent map = new Intent(getString(R.string.building_intent), null, ScheduleActivity.this, CampusMapActivity.class);
	    				map.setData(Uri.parse(current_clt.getBuilding().getId()));
	    				startActivity(map);break;
	            }
	            return true;
	        }

	        @Override
	        public void onDestroyActionMode(ActionMode mode) {
	        	if(sd!=null)
	        		sd.close();
	        }
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		@Override
		public void onPageSelected(int location) {

			for(SherlockFragment csf : fragments)
				if(((ActionModeFragment)csf).getActionMode()!=null)
					((ActionModeFragment)csf).getActionMode().finish();
		}
		
}