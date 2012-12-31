package com.nasageek.utexasutilities.activities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.R.menu;
import com.nasageek.utexasutilities.R.style;

public class BlackboardGradesActivity extends SherlockActivity 
{
	private ActionBar actionbar;
	private LinearLayout g_pb_ll;
	private ListView glv;
	private TextView getv;
	private DefaultHttpClient httpclient;
	private fetchGradesTask fetch;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.blackboard_grades_layout);
			actionbar = getSupportActionBar();
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle(getIntent().getStringExtra("coursename"));
			actionbar.setSubtitle("Grades");
			
			g_pb_ll = (LinearLayout)findViewById(R.id.grades_progressbar_ll);
			glv = (ListView) findViewById(R.id.gradesListView);
			getv = (TextView) findViewById(R.id.grades_error);
			
			glv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					bbGrade grade = (bbGrade) arg0.getAdapter().getItem(arg2);
					
					Dialog dlg = new Dialog(BlackboardGradesActivity.this,R.style.Theme_Sherlock_Light_Dialog);
					dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dlg.setContentView(R.layout.grade_info_dialog);
					dlg.setTitle("Grade Info");
					
					TextView name = (TextView) dlg.findViewById(R.id.grade_info_name);
					TextView value = (TextView) dlg.findViewById(R.id.grade_info_value);
					TextView comment = (TextView) dlg.findViewById(R.id.grade_info_comment);
					
					name.setText(grade.getName());
					
					String valueString = null;
					if(grade.getNumGrade().equals(-1))
						valueString = "-";
					else if(grade.getNumGrade().equals(-2))
						valueString = grade.getGrade();
					else
						valueString = grade.getNumGrade() +"/"+grade.getNumPointsPossible();
					value.setText(valueString);
					comment.setText(grade.getComment());
					
					dlg.setCanceledOnTouchOutside(true);
					dlg.show();
					//TODO: DialogFragment or showDialog
				}
				
			});
			
			
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
        inflater.inflate(R.menu.blackboard_dlable_item_menu, menu);
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
		           super.onBackPressed();
		           break;
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
	    		web.putExtra("itemName", "Grades");
	    		web.putExtra("coursename", getIntent().getStringExtra("coursename"));
	    		startActivity(web);
			}		
		});
		alertBuilder.setTitle("View on Blackboard");
		alertBuilder.show();
	}
	private class fetchGradesTask extends AsyncTask<Object,Void,ArrayList<bbGrade>>
	{
		private DefaultHttpClient client;
		private String errorMsg;
		
		public fetchGradesTask(DefaultHttpClient client)
		{
			this.client = client;
		}
		
		@Override
		protected ArrayList<bbGrade> doInBackground(Object... params)
		{
			HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/courseData?course_section=GRADES&course_id="+getIntent().getStringExtra("courseid"));
	    	String pagedata="";

	    	try
			{
				HttpResponse response = client.execute(hget);
		    	pagedata = EntityUtils.toString(response.getEntity());
			} catch (Exception e)
			{
				errorMsg = "UTilities could not fetch this course's grades";
				cancel(true);
				e.printStackTrace();
				return null;
			}
	    	ArrayList<bbGrade> data=new ArrayList<bbGrade>();
	    	
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
		    	Pattern commentPattern = Pattern.compile("comments=\"(.*?)\"",Pattern.DOTALL);
		    	Matcher commentMatcher = commentPattern.matcher(gradeData);
		    	
		    	if(nameMatcher.find() && pointsMatcher.find() && gradeMatcher.find())
		    	{	
		    		data.add(new bbGrade(nameMatcher.group(1).replace("&amp;", "&"),gradeMatcher.group(1),pointsMatcher.group(1), commentMatcher.find() ? Html.fromHtml(Html.fromHtml(commentMatcher.group(1)).toString()).toString() 
		    																													  : "No comments"));
		    	}
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
				getv.setVisibility(View.GONE);
	    		glv.setVisibility(View.VISIBLE);
	    	}
		}	
		@Override
		protected void onCancelled()
		{
			getv.setText(errorMsg);
			
			g_pb_ll.setVisibility(View.GONE);
    		glv.setVisibility(View.GONE);
    		getv.setVisibility(View.VISIBLE);
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

			return items.size();
		}
	
		public bbGrade getItem(int position) {

			return items.get(position);
		}
	
		public long getItemId(int position) {

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
				lin = (RelativeLayout) li.inflate(R.layout.grade_item_view,null,false);
			
			TextView gradeName = (TextView) lin.findViewById(R.id.grade_name);
			TextView gradeValue = (TextView) lin.findViewById(R.id.grade_value);
			ImageView commentImg = (ImageView) lin.findViewById(R.id.comment_available_img);
		
			if(grade.getComment().equals("No comments"))
				commentImg.setVisibility(View.INVISIBLE);
			else
				commentImg.setVisibility(View.VISIBLE);
			gradeName.setText(title);
			gradeValue.setText(value);
	
			return (View)lin;
		}
	}

	class bbGrade
	{
		String name, grade, pointsPossible, comment;
		
		public bbGrade(String name, String grade, String pointsPossible, String comment)
		{
			this.name = name;
			this.grade = grade;
			this.pointsPossible = pointsPossible;
			this.comment = comment;
		}
		public String getName()
		{
			return name;
		}
		public String getComment()
		{
			return comment;
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
