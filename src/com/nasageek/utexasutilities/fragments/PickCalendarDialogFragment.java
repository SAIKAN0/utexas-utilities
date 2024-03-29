package com.nasageek.utexasutilities.fragments;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nasageek.utexasutilities.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class PickCalendarDialogFragment extends SherlockDialogFragment {

	private SherlockFragmentActivity parentAct;
	
	public PickCalendarDialogFragment() {}
	
	public static PickCalendarDialogFragment newInstance(ArrayList<Integer> indices, ArrayList<String> calendars, ArrayList<ContentValues> valuesList)
	{
		PickCalendarDialogFragment pcdf = new PickCalendarDialogFragment();
		Bundle args = new Bundle();
		args.putIntegerArrayList("indices", indices);
		args.putStringArrayList("calendars", calendars);
		args.putParcelableArrayList("valuesList", valuesList);
		pcdf.setArguments(args);
		return pcdf;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder build = new AlertDialog.Builder(getSherlockActivity());
		ArrayList<String> calendars = getArguments().getStringArrayList("calendars");
		parentAct = getSherlockActivity();
		build.setAdapter(new CalendarAdapter(getSherlockActivity(), calendars), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				CalendarInsertHandler cih = new CalendarInsertHandler(getSherlockActivity().getContentResolver());
				ArrayList<ContentValues> cvList = getArguments().getParcelableArrayList("valuesList");
				ArrayList<Integer> indices = getArguments().getIntegerArrayList("indices"); 
				int selection = indices.get(which);

				for(int i = 0; i < cvList.size(); i++)
				{
					cvList.get(i).put(CalendarContract.Events.CALENDAR_ID, selection);
					cih.startInsert(i, null, CalendarContract.Events.CONTENT_URI, cvList.get(i));
				}
				
			}
		})
		.setTitle("Select calendar");
		
		AlertDialog dlg = build.create();
		
		return dlg;
	}
	
	class CalendarInsertHandler extends AsyncQueryHandler
	{
		@SuppressLint("HandlerLeak")
		public CalendarInsertHandler(ContentResolver cr)
		{
			super(cr);
		}
		@Override
		public void onInsertComplete(int token, Object cookie, Uri uri)
		{
			if(token == getArguments().getParcelableArrayList("valuesList").size()-1)
			{
				Log.d("CalendarStuff","insert finished");
				Toast.makeText(parentAct, "This schedule has been exported successfully. It may take a minute or two " +
					"for it to show up on your calendar", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	class CalendarAdapter extends BaseAdapter
	{
		private ArrayList<String> elements;
		private LayoutInflater inflater;
		
		public CalendarAdapter(Context con, ArrayList<String> elements)
		{
			super();
			this.elements = elements;
			inflater = (LayoutInflater) con.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return elements.size();
		}

		@Override
		public Object getItem(int arg0) {
			return elements.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.calendar_item_view, null, false);
			}
			String calName = elements.get(position).split(" \\^\\^ ")[0];
			String accName = elements.get(position).split(" \\^\\^ ")[1];
			((TextView) convertView.findViewById(R.id.calendar_name)).setText(calName);
			((TextView) convertView.findViewById(R.id.calendar_account)).setText(accName);
			return convertView;
		}
		
	}
}
