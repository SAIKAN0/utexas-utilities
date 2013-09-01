package com.nasageek.utexasutilities.activities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.BBClass;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.Pair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.string;
import com.nasageek.utexasutilities.adapters.BBClassAdapter;

public class BlackboardActivity extends SherlockActivity {
	
	private ActionBar actionbar;
	private DefaultHttpClient httpclient;
	private SharedPreferences settings;
	private LinearLayout bb_pb_ll;
	private LinearLayout blackboardlinlay;
	private TextView bbtv;
	private AmazingListView bblv;
	private ArrayList<BBClass> classList;
	private ArrayList<Pair<String,ArrayList<BBClass>>> classSectionList;
	private fetchClassesTask fetch;
//	public static String currentBBCourseId;
//	public static String currentBBCourseName;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.blackboard_layout);

    	bb_pb_ll = (LinearLayout) findViewById(R.id.blackboard_progressbar_ll);
    	bblv = (AmazingListView) findViewById(R.id.blackboard_class_listview);
    	blackboardlinlay = (LinearLayout) findViewById(R.id.blackboard_courselist);
    	bbtv = (TextView) findViewById (R.id.blackboard_error);
    	
    	classList = new ArrayList<BBClass>();
    	classSectionList = new ArrayList<Pair<String,ArrayList<BBClass>>>();
    	
    	actionbar = getSupportActionBar();
		actionbar.setTitle("Blackboard");
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		
	
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		httpclient = ConnectionHelper.getThreadSafeClient();
		httpclient.getCookieStore().clear();
		BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,httpclient));
    	cookie.setDomain("courses.utexas.edu");
    	httpclient.getCookieStore().addCookie(cookie);
    	
    	fetch = new fetchClassesTask(httpclient);
    	fetch.execute();
		
    	 Crittercism.leaveBreadcrumb("Loaded BlackboardActivity");
	}

	
	private class fetchClassesTask extends AsyncTask<Object,Void,String>
	{
		private DefaultHttpClient client;
		private String errorMsg;
		
		public fetchClassesTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected String doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/enrollments?course_type=COURSE");
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				errorMsg = "UTilities could not fetch the Blackboard course list";
				e.printStackTrace();
				cancel(true);
				return null;
			}

	    	Pattern class_pattern = Pattern.compile("bbid=\"(.*?)\" name=\"(.*?)\" courseid=\"(.*?)\"");
	    	Matcher class_matcher = class_pattern.matcher(pagedata);
	    	
	    	while(class_matcher.find())
	    	{
	    		classList.add(new BBClass(class_matcher.group(2).replace("&amp;","&"),class_matcher.group(1).replace("&amp;","&"),class_matcher.group(3)));	
	    	}
	    	
			return pagedata;
		}
		@Override
		protected void onPostExecute(String result)
		{
			//build the list of courses here
			if(!this.isCancelled()) // not necessary
	    	{
	    		String currentCategory="";
	    		ArrayList sectionList=null;
				for(int i = 0; i<classList.size(); i++)
	    		{
	    			//first course is always in a new category (the first category)
					if(i==0)
	    			{	
	    				currentCategory = classList.get(i).getSemester();
	    				sectionList = new ArrayList();
	    				sectionList.add(classList.get(i));
	    			}
					//if the current course is not part of the current category or we're on the last course
					//weird stuff going on here depending on if we're at the end of the course list
	    			else if(!classList.get(i).getSemester().equals(currentCategory) || i == classList.size()-1)
	    			{
	    				
	    				if(i == classList.size()-1)
	    					sectionList.add(classList.get(i));
	    					
	    				classSectionList.add(new Pair(currentCategory,sectionList));
	    				
	    				currentCategory = classList.get(i).getSemester();
	    				sectionList=new ArrayList();
	    				
	    				if(i != classList.size()-1)
	    					sectionList.add(classList.get(i));
	    			}
					//otherwise just add to the current category
	    			else
	    			{
	    				sectionList.add(classList.get(i));
	    			}
	    			
	    		}
				
				bblv.setAdapter(new BBClassAdapter(BlackboardActivity.this,classSectionList));
				bblv.setPinnedHeaderView(BlackboardActivity.this.getLayoutInflater().inflate(R.layout.menu_header_item_view, bblv, false));
				
				bblv.setOnItemClickListener(new OnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
						//TODO: figure out course id stuff here
						Intent classLaunch = new Intent(getString(R.string.coursemap_intent), null, BlackboardActivity.this, CourseMapActivity.class);
						BBClass bbclass = (BBClass)(parent.getItemAtPosition(position));
						classLaunch.putExtra("courseid", bbclass.getBbid());
					//	currentBBCourseId = bbclass.getBbid();
						classLaunch.setData(Uri.parse((bbclass).getBbid()));
						String unique = "", cid = "";
						try{
							unique = bbclass.getUnique();
							cid = bbclass.getFullCourseid().substring(bbclass.getFullCourseid().indexOf(unique)+6).replaceAll("_"," ");
						}
						catch(Exception e)
						{
							e.printStackTrace();
							Crittercism.leaveBreadcrumb("BBUnique issue "+bbclass.getFullCourseid());
							Toast.makeText(BlackboardActivity.this, "An error occurred, things might look a bit odd.", Toast.LENGTH_SHORT).show();
						}
						classLaunch.putExtra("folderName", "Course Map");
						classLaunch.putExtra("coursename", cid);
					//	currentBBCourseName = cid;
						startActivity(classLaunch);

					}
				});
				
				
				bb_pb_ll.setVisibility(View.GONE);
				bbtv.setVisibility(View.GONE);
	    		bblv.setVisibility(View.VISIBLE);
	    	}	
		}
		@Override
		protected void onCancelled()
		{
			bbtv.setText(errorMsg);
			bb_pb_ll.setVisibility(View.GONE);
			bbtv.setVisibility(View.VISIBLE);
    		bblv.setVisibility(View.GONE);
		}
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(fetch!=null)
			fetch.cancel(true);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		    	case android.R.id.home:
		            // app icon in action bar clicked; go home
		            super.onBackPressed();
		            break;
	    	}
	    	return false;
	}

}