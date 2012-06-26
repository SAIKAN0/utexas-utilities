package com.nasageek.utexasutilities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BlackboardGradesActivity extends SherlockActivity 
{
	private ActionBar actionbar;
	private LinearLayout g_pb_ll;
	private ListView glv;
	private ConnectionHelper ch;
	private DefaultHttpClient httpclient;
	private fetchGradesTask fetch;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.blackboard_grades_layout);
			actionbar = getSupportActionBar();
			actionbar.setHomeButtonEnabled(true);
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
				actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
			actionbar.setTitle(BlackboardActivity.currentBBCourseName+" - Grades");
			
			g_pb_ll = (LinearLayout)findViewById(R.id.grades_progressbar_ll);
			glv = (ListView) findViewById(R.id.gradesListView);
			
			ch = new ConnectionHelper(this);
			
			
			httpclient = ConnectionHelper.getThreadSafeClient();
			httpclient.getCookieStore().clear();
			BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(this,httpclient));
			cookie.setDomain("courses.utexas.edu");
			httpclient.getCookieStore().addCookie(cookie);
		
			fetch = new fetchGradesTask(httpclient);
			fetch.execute();
			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.layout.blackboard_dlable_item_menu, menu);
		return true;
		 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    	int id = item.getItemId();
	    	switch(id)
	    	{
		    	case android.R.id.home:
		            // app icon in action bar clicked; go home
		            Intent home = new Intent(this, UTilitiesActivity.class);
		            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(home);break;
		    	case R.id.viewInWeb:
		    		showAreYouSureDlg(BlackboardGradesActivity.this);
		    		break;
	    	}
	    	return false;
	}
	private void showAreYouSureDlg(Context con)
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(con);
		alertBuilder.setMessage("Would you like to view this item on the Blackboard website?");
		alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				
			}
		});
		
		alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
	
				Intent web = new Intent(null,Uri.parse(getIntent().getStringExtra("viewUri")),BlackboardGradesActivity.this,BlackboardExternalItemActivity.class);
	    		web.putExtra("itemName", actionbar.getTitle());
	    		startActivity(web);
			}		
		});
		alertBuilder.setTitle("View on Blackboard");
		alertBuilder.show();
	}
	
	private class fetchGradesTask extends AsyncTask<Object,Void,ArrayList<bbGrade>>
	{
		private DefaultHttpClient client;
		
		public fetchGradesTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected ArrayList<bbGrade> doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/courseData?course_section=GRADES&course_id="+BlackboardActivity.currentBBCourseId);
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				
				e.printStackTrace();
			}
	    	ArrayList<bbGrade> data=new ArrayList<bbGrade>();
	//    	pagedata = pagedata.replaceAll("comments=\".*?\"", ""); //might include later, need to strip for now for grade recognition
	    	
	    	Pattern gradeItemPattern = Pattern.compile("<grade-item.*?/>",Pattern.DOTALL);
	    	Matcher gradeItemMatcher = gradeItemPattern.matcher(pagedata);
	    	
	    	while(gradeItemMatcher.find())
	    	{
	    		String gradeData = gradeItemMatcher.group();
	    		Pattern namePattern = Pattern.compile("name=\"(.*?)\"");
		    	Matcher nameMatcher = namePattern.matcher(gradeData);
		    	Pattern pointsPattern = Pattern.compile("pointspossible=\"(.*?)\"");
		    	Matcher pointsMatcher = pointsPattern.matcher(gradeData);
		    	Pattern gradePattern = Pattern.compile("grade=\"(.*?)\"");
		    	Matcher gradeMatcher = gradePattern.matcher(gradeData);
		    	
		    	if(nameMatcher.find() && pointsMatcher.find() && gradeMatcher.find())
		    		data.add(new bbGrade(nameMatcher.group(1),gradeMatcher.group(1),pointsMatcher.group(1)));
	    	}
			return data;
		}
		@Override
		protected void onPostExecute(ArrayList<bbGrade> result)
		{
			if(!this.isCancelled())
	    	{
				
				glv.setAdapter(new GradesAdapter(BlackboardGradesActivity.this,result));
				
				g_pb_ll.setVisibility(View.GONE);
	    		glv.setVisibility(View.VISIBLE);
	    	}
		}	
	}

	class GradesAdapter extends ArrayAdapter<bbGrade> {
	
		private Context con;
		private ArrayList<bbGrade> items;
		private LayoutInflater li;
		
		public GradesAdapter(Context c, ArrayList<bbGrade> items)
		{
			super(c,0,items);
			con = c;
			this.items=items;
			li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		}
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}
	
		public bbGrade getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}
	
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public boolean areAllItemsEnabled()
		{
			return true;
		}
		@Override
		public boolean isEnabled(int i)
		{
			return true;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			
			bbGrade grade = items.get(position);
			
			String title = grade.getName();
			String value = null;
			if(grade.getNumGrade().equals(-1))
				value = "-";
			else if(grade.getNumGrade().equals(-2))
				value = grade.getGrade();
			else
				value = grade.getNumGrade() +"/"+grade.getNumPointsPossible();
			ViewGroup lin = (ViewGroup) convertView;

			if (lin == null)
				lin = (LinearLayout) li.inflate(R.layout.grade_item_view,null,false);
			
			TextView gradeName = (TextView) lin.findViewById(R.id.grade_name);
			TextView gradeValue = (TextView) lin.findViewById(R.id.grade_value);
		
			
			gradeName.setText(title);
			gradeValue.setText(value);
	
			return (View)lin;
		}
	}

	class bbGrade
	{
		String name, grade, pointsPossible;
		
		public bbGrade(String name, String grade, String pointsPossible)
		{
			this.name = name;
			this.grade = grade;
			this.pointsPossible = pointsPossible;
		}
		public String getName()
		{
			return name;
		}
		public Number getNumGrade()
		{
			if(!grade.equals("-"))
			{
				String temp = grade.replaceAll("[^\\d\\.]*", "");
				if(temp.equals(""))
				{
					return -2;
				}
				double d = Double.parseDouble(temp);
				if(d == Math.floor(d))
				{
					return (int)d;
				}
				else
					return d;
			}	
			else
				return -1;
		}
		public String getGrade()
		{
			return grade;
		}
		public String getPointsPossible()
		{
			return pointsPossible;
		}
		public Number getNumPointsPossible()
		{
			String temp = pointsPossible.replaceAll("[^\\d\\.]*", "");
			double d = Double.parseDouble(temp);
			if(d == Math.floor(d))
			{
				return (int)d;
			}
			else
				return d;
		}
	
	}
	
}