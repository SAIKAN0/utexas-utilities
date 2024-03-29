package com.nasageek.utexasutilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nasageek.utexasutilities.model.BBClass;
import com.nasageek.utexasutilities.model.FeedItem;

import android.util.TimingLogger;
import android.util.Xml;

public class BlackboardDashboardXmlParser {

	private HashMap<String, BBClass> courses;
	private TimingLogger tl;
	private boolean feedParsed = false;
	//need this so we don't instantiate it every time we make a new FeedItem
	private SimpleDateFormat feedItemDateFormat;
	
	public List<ParcelablePair<String, List<FeedItem>>> parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return actuallyParse(parser);
		} finally {
			in.close();
		}
	}
	public List<ParcelablePair<String, List<FeedItem>>> parse(Reader in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in);
			parser.nextTag();
			return actuallyParse(parser);
		} finally {
			in.close();
		}
	}
	
	/*
	 * Weird name? yeah.  This probably isn't best practice, but I think it's okay in order to remain 
	 * easy to understand to calling classes with minimal code duplication.  This is where the actual 
	 * parsing occurs.
	 */
	private List<ParcelablePair<String, List<FeedItem>>> actuallyParse(XmlPullParser parser) throws XmlPullParserException, IOException {
		tl = new TimingLogger("Dashboard", "Parser");
		
		List<FeedItem> feeditems = readDashboard(parser); 
		
		tl.addSplit("readDashboard()");
		
		//want to show most recent date at top and ListView's stackFromBottom is broken by AmazingListView
		Collections.reverse(feeditems);
		tl.addSplit("Collections.reverse()");
		List<ParcelablePair<String, List<FeedItem>>> categorizedList = new ArrayList<ParcelablePair<String, List<FeedItem>>>();
		String currentCategory="";
		ArrayList<FeedItem> sectionList=null;
		DateFormat df = SimpleDateFormat.getDateInstance();
		for(int i = 0; i<feeditems.size(); i++) {
			//first FeedItem is always in a new category (the first category)
			if(i==0) {	
				currentCategory = df.format(feeditems.get(i).getDate());
				sectionList = new ArrayList<FeedItem>();
				sectionList.add(feeditems.get(i));
			}
			//if the current course is not part of the current category or we're on the last course
			//weird stuff going on here depending on if we're at the end of the list
			else if(!df.format(feeditems.get(i).getDate()).equals(currentCategory) || i == feeditems.size()-1) {
				if(i == feeditems.size()-1)
					sectionList.add(feeditems.get(i));
					
				categorizedList.add(new ParcelablePair<String, List<FeedItem>>(currentCategory,sectionList));
				
				currentCategory = df.format(feeditems.get(i).getDate());
				sectionList=new ArrayList<FeedItem>();
				
				if(i != feeditems.size()-1)
					sectionList.add(feeditems.get(i));
			}
			//otherwise just add to the current category
			else {
				sectionList.add(feeditems.get(i));
			}
			
		}
		tl.addSplit("CategorizedList created");
	//	tl.dumpToLog();
		feedParsed = true;
		return categorizedList;
	}
	
	private List<FeedItem> readDashboard(XmlPullParser parser) throws XmlPullParserException, IOException {		
		feedItemDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
		
		courses = new HashMap<String, BBClass>();
		List<FeedItem> feed = new ArrayList<FeedItem>();
		parser.require(XmlPullParser.START_TAG, null, "mobileresponse");
		parser.nextTag();
		
		parser.require(XmlPullParser.START_TAG, null, "courses");
		while(parser.next() != XmlPullParser.END_DOCUMENT) {
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			if(name.equals("course")) {	
				BBClass clz = readCourse(parser);
				if(clz != null)
					courses.put(clz.getBbid(), clz);
			} else if(name.equals("feeditem")) {
				FeedItem item = readFeedItem(parser);
				if(item != null)
					feed.add(item);
			}		
		}
		return feed;
	}
	
	private BBClass readCourse(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "course");
		
		String name = parser.getAttributeValue(null, "name");
		String bbid = parser.getAttributeValue(null, "bbid");
		String fullcourseid = parser.getAttributeValue(null, "courseid");
		
		//case for system announcement course
		if(fullcourseid == null)
			return null;
		while(parser.next() != XmlPullParser.END_TAG){};
		return new BBClass(name, bbid, fullcourseid);
	}
	private FeedItem readFeedItem(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "feeditem");
		//if sourcetype="AN" then it is a notification
		
//		String itemid = parser.getAttributeValue(null, "itemid");
		String type = parser.getAttributeValue(null, "type");
		
		//Don't care about system announcements
		if(type.equals("SYSTEM_ANNOUNCEMENT"))
			return null;
		String courseid = parser.getAttributeValue(null, "courseid");
		String message = parser.getAttributeValue(null, "message");
		String date = parser.getAttributeValue(null, "startdate");
		
		//could be null
		String contentid = parser.getAttributeValue(null, "contentid");
		String sourcetype = parser.getAttributeValue(null, "sourcetype");
//		String id = parser.getAttributeValue(null, "contentid");
		//TODO sourceid?
		while(parser.next() != XmlPullParser.END_TAG){};
		return new FeedItem(type, message, contentid, courseid, sourcetype, date, feedItemDateFormat);
	}
	
	public HashMap<String,BBClass> getCourses() {
		if(feedParsed)
			return courses;
		else
			throw new IllegalStateException("You must parse the feed before you can call getCourses()");
	}
	
	//unused, will remove later
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if(parser.getEventType() != XmlPullParser.START_TAG)
			throw new IllegalStateException();
		int depth = 1;
		while (depth != 0)
		{
			switch(parser.next())
			{
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
