package com.nasageek.utexasutilities.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.util.TimingLogger;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.foound.widget.AmazingAdapter;
import com.foound.widget.AmazingListView;
import com.nasageek.utexasutilities.BlackboardDashboardXmlParser;
import com.nasageek.utexasutilities.ConnectionHelper;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.activities.BlackboardAnnouncementsActivity;
import com.nasageek.utexasutilities.activities.BlackboardDownloadableItemActivity;
import com.nasageek.utexasutilities.activities.BlackboardGradesActivity;
import com.nasageek.utexasutilities.activities.CourseMapActivity;
import com.nasageek.utexasutilities.model.FeedItem;

public class BlackboardDashboardFragment extends SherlockFragment {

		private DefaultHttpClient httpclient;
		private LinearLayout d_pb_ll;
		private AmazingListView dlv;
		private TextView etv;
		private TimingLogger tl;
		private fetchDashboardTask fetch;
				
		
		public BlackboardDashboardFragment() { }
		
		public static BlackboardDashboardFragment newInstance(String title)
		{
			BlackboardDashboardFragment f = new BlackboardDashboardFragment();

	        Bundle args = new Bundle();
	        args.putString("title", title);
	        f.setArguments(args);

	        return f;
		}
		
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			tl = new TimingLogger("Dashboard", "loadTime");
			setRetainInstance(true);
			
			httpclient = ConnectionHelper.getThreadSafeClient();
			httpclient.getCookieStore().clear();
			BasicClientCookie cookie = new BasicClientCookie("s_session_id", ConnectionHelper.getBBAuthCookie(getSherlockActivity(),httpclient));
	    	cookie.setDomain("courses.utexas.edu");
	    	httpclient.getCookieStore().addCookie(cookie);
			
			fetch = new fetchDashboardTask(httpclient);
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				fetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else
				fetch.execute();
			
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View vg =  inflater.inflate(R.layout.blackboard_dashboard_fragment, container, false);
			
			dlv = (AmazingListView) vg.findViewById(R.id.dash_listview);
			d_pb_ll = (LinearLayout) vg.findViewById(R.id.dash_progressbar_ll);
			etv = (TextView) vg.findViewById(R.id.dash_error);
			return vg;
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if(fetch!=null)
				fetch.cancel(true);
		}
		public void refresh()
		{
		//	tlv.setVisibility(View.GONE);
		//	etv.setVisibility(View.GONE);
		//	t_pb_ll.setVisibility(View.VISIBLE);
			if(fetch!=null)
			{	fetch.cancel(true);
				fetch = null;
			}
	//		transactionlist.clear();
			
	//		parser(true);
	//		ta.resetPage();
	//		tlv.setSelectionFromTop(0, 0);
		}
		private class fetchDashboardTask extends AsyncTask<Object,Void,List<Pair<String, List<FeedItem>>>>
		{
			private DefaultHttpClient client;
			private String errorMsg = "";
			private List<Pair<String, List<FeedItem>>> feedList;
			
			public fetchDashboardTask(DefaultHttpClient client)
			{
				this.client = client;
			}
			
			@Override
			protected List<Pair<String, List<FeedItem>>> doInBackground(Object... params)
			{
		    	String pagedata="";
		    	feedList = new ArrayList<Pair<String, List<FeedItem>>>();
		    	
		    	if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) 
		    	{
			    	URL location;
			    	HttpsURLConnection conn = null;
					try {
						location = new URL("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/dashboard?course_type=COURSE&with_notifications=true");
						conn = (HttpsURLConnection) location.openConnection();
						conn.setRequestProperty("Cookie", "s_session_id="+ConnectionHelper.getBBAuthCookie(getSherlockActivity(),httpclient));
						conn.setRequestMethod("GET");
						conn.setDoInput(true);
						conn.connect();
						InputStream in = conn.getInputStream();
						feedList = new BlackboardDashboardXmlParser().parse(in);
						in.close();
						tl.addSplit("XML downloaded");
						
					} catch (IOException e) {
						errorMsg = "UTilities could not fetch your Blackboard Dashboard";
						e.printStackTrace();
						cancel(true);
						return null;
					} catch (XmlPullParserException e) {
						errorMsg = "UTilities could not parse the downloaded Dashboard data.";
			        	e.printStackTrace();
			        	cancel(true);
			        	return null;	
						
					} finally {
						if(conn != null)
							conn.disconnect();
					}
		    	}
		    	else 
		    	{
			    	try {
			    		HttpGet hget = new HttpGet("https://courses.utexas.edu/webapps/Bb-mobile-BBLEARN/dashboard?course_type=COURSE&with_notifications=true");
			    		HttpResponse response = client.execute(hget);
				    	pagedata = EntityUtils.toString(response.getEntity());
				    	feedList = new BlackboardDashboardXmlParser().parse(new StringReader(pagedata));
				    	
					} catch (IOException e) {
						errorMsg = "UTilities could not fetch your Blackboard Dashboard";
						e.printStackTrace();
						cancel(true);
						return null;
					} catch(XmlPullParserException e) {
			    		errorMsg = "UTilities could not parse the downloaded Dashboard data.";
			    		e.printStackTrace();
			    		cancel(true);
			    		return null;	        	 
			    	}	
		    	}
		    	tl.addSplit("XML parsed");
		   // 	tl.dumpToLog();
				return feedList;
			}
			@Override
			protected void onPostExecute(List<Pair<String, List<FeedItem>>> result)
			{
				dlv.setAdapter(new BlackboardDashboardAdapter(result));
				dlv.setPinnedHeaderView(getSherlockActivity().getLayoutInflater().inflate(R.layout.menu_header_item_view, dlv, false));
				dlv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						FeedItem fi = (FeedItem) parent.getAdapter().getItem(position);
						String courseid = fi.getBbId();
						String contentid = fi.getContentId();
						String coursename = fi.getCourseId();
						String message = fi.getMessage();
						
						if("Grades".equals(fi.getType()))
						{		
							final Intent gradesLaunch = new Intent(null, null, getSherlockActivity(), BlackboardGradesActivity.class);
				//			gradesLaunch.putExtra("viewUri", url);
						//TODO: fetch coursemap for viewurl
							gradesLaunch.putExtra("courseid", courseid);
							gradesLaunch.putExtra("coursename", coursename);
							gradesLaunch.putExtra("showViewInWeb", false);
							startActivity(gradesLaunch);
						}
						else if("Content".equals(fi.getType()))
						{
							final Intent bbItemLaunch = new Intent(null, null, getSherlockActivity(), BlackboardDownloadableItemActivity.class);
							bbItemLaunch.putExtra("contentid", contentid);
							
							bbItemLaunch.putExtra("itemName", message); //TODO: not sure if I want to keep this
							
						//	bbItemLaunch.putExtra("viewUri", url); TODO
							bbItemLaunch.putExtra("courseid", courseid);
							bbItemLaunch.putExtra("coursename", coursename);
							bbItemLaunch.putExtra("showViewInWeb", false);
							startActivity(bbItemLaunch);
						}
						else if("Announcement".equals(fi.getType()))
						{
							//TODO: figure out how to seek to a specific announcement
							final Intent announcementsLaunch = new Intent(null, null, getSherlockActivity(), BlackboardAnnouncementsActivity.class);
					//		announcementsLaunch.putExtra("viewUri", url); TODO
							announcementsLaunch.putExtra("courseid", courseid);
							announcementsLaunch.putExtra("coursename", coursename);
							announcementsLaunch.putExtra("showViewInWeb", false);
							startActivity(announcementsLaunch);
						}
						else if("Courses".equals(fi.getType()))
						{
							final Intent classLaunch = new Intent(getString(R.string.coursemap_intent), null, getSherlockActivity(), CourseMapActivity.class);
							classLaunch.putExtra("courseid", fi.getBbId());
							classLaunch.setData(Uri.parse(fi.getBbId()));
							classLaunch.putExtra("folderName", "Course Map");
							classLaunch.putExtra("coursename", fi.getCourseId());
				//			classLaunch.putExtra("showViewInWeb", false);
							startActivity(classLaunch);
						}
					}
				});
				tl.addSplit("Adapter created");
				d_pb_ll.setVisibility(View.GONE);
	    		dlv.setVisibility(View.VISIBLE);
	    		etv.setVisibility(View.GONE);
			}
			@Override
			protected void onCancelled()
			{
				etv.setText(errorMsg);
				d_pb_ll.setVisibility(View.GONE);
	    		dlv.setVisibility(View.GONE);
				etv.setVisibility(View.VISIBLE); 
			}
		}
		
		//TODO: figure out fast scroll, maybe the min-sdk is just too low...
		class BlackboardDashboardAdapter extends AmazingAdapter {
			
			private List<Pair<String, List<FeedItem>>> items;
			
			public BlackboardDashboardAdapter(List<Pair<String, List<FeedItem>>> items)
			{
				this.items = items;
			}
			
			@Override
			public int getCount() {
				int res = 0;
				for (int i = 0; i < items.size(); i++) {
					res += items.get(i).second.size();
				}
				return res;
			}
			
			@Override
			public boolean isEnabled(int position) {
				return !("Unknown".equals(getItem(position).getType()) || "Notification".equals(getItem(position).getType())); 
			}

			@Override
			public FeedItem getItem(int position) {
				int c = 0;
				for (int i = 0; i < items.size(); i++) {
					if (position >= c && position < c + items.get(i).second.size()) {
						return items.get(i).second.get(position - c);
					}
					c += items.get(i).second.size();
				}
				return null;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			protected void onNextPageRequested(int page) {}
			
			@Override
			protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
				if (displaySectionHeader) {
					view.findViewById(R.id.header).setVisibility(View.VISIBLE);
					TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
					lSectionTitle.setText(getSections()[getSectionForPosition(position)]);
				} else {
					view.findViewById(R.id.header).setVisibility(View.GONE);
				}
			}
			
			//TODO
			@Override
			public View getAmazingView(int position, View convertView, ViewGroup parent) {
				View res = convertView;
				if (res == null) res = getSherlockActivity().getLayoutInflater().inflate(R.layout.dashboard_item_view, null);
				FeedItem fi = getItem(position);
				
				TextView courseName = (TextView) res.findViewById(R.id.d_course_name);
				TextView contentType = (TextView) res.findViewById(R.id.d_content_type);
				TextView message = (TextView) res.findViewById(R.id.d_message);

				//TODO: make this look nice for malformed classes
				if(!"".equals(fi.getCourseId()))
					courseName.setText(fi.getCourseId()+ " - " + fi.getName()+ " ("+fi.getBbClass().getUnique()+")");
				else
					courseName.setText(fi.getName()+ " ("+fi.getBbClass().getUnique()+")");
				contentType.setText(fi.getType());
				message.setText(fi.getMessage());
				
				if(!isEnabled(position))
				{	TypedValue tv = new TypedValue();
					if(getSherlockActivity().getTheme().resolveAttribute(android.R.attr.textColorTertiary, tv, true))	
					{	
						TypedArray arr = getSherlockActivity().obtainStyledAttributes(tv.resourceId, new int[] {android.R.attr.textColorTertiary});
						message.setTextColor(arr.getColor(0, Color.BLACK));
						courseName.setTextColor(arr.getColor(0, Color.BLACK));
					}
				}
				else
				{	
					message.setTextColor(Color.BLACK);
					courseName.setTextColor(Color.BLACK);
				}
				return res;
			}
			
			@Override
			public void configurePinnedHeader(View header, int position, int alpha) {
				TextView lSectionHeader = (TextView)header;
				lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
				//	lSectionHeader.getBackground().setAlpha(alpha);
				//	lSectionHeader.setBackgroundColor(alpha << 24 | (0xEAEAEA));
				//	lSectionHeader.setTextColor(alpha << 24 | (0x343434));
			}
			
			@Override
			public int getPositionForSection(int section) {
				if (section < 0) section = 0;
				if (section >= items.size()) section = items.size() - 1;
				int c = 0;
				for (int i = 0; i < items.size(); i++) {
					if (section == i) { 
						return c;
					}
					c += items.get(i).second.size();
				}
				return 0;
			}
			
			@Override
			public int getSectionForPosition(int position) {
				int c = 0;
				for (int i = 0; i < items.size(); i++) {
					if (position >= c && position < c + items.get(i).second.size()) {
						return i;
					}
					c += items.get(i).second.size();
				}
				return 0;
			}
			
			@Override
			public String[] getSections() {
				String[] res = new String[items.size()];
				for (int i = 0; i < items.size(); i++) {
					res[i] = items.get(i).first;
				}
				return res;
			}
			
			@Override
			protected View getLoadingView(ViewGroup parent) {
				return null;
			}
		}
	}
