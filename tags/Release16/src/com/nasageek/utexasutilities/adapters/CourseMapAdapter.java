package com.nasageek.utexasutilities.adapters;

import java.util.ArrayList;

import com.nasageek.utexasutilities.Pair;
import com.nasageek.utexasutilities.R;
import com.nasageek.utexasutilities.R.id;
import com.nasageek.utexasutilities.R.layout;
import com.nasageek.utexasutilities.CourseMapItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CourseMapAdapter extends ArrayAdapter<Pair<CourseMapItem,ArrayList>> {

	private Context con;
	private ArrayList<Pair<CourseMapItem,ArrayList>> items;
	private LayoutInflater li;
	
	public CourseMapAdapter(Context c, ArrayList<Pair<CourseMapItem,ArrayList>> items)
	{
		super(c,0,items);
		con = c;
		this.items=items;
		li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	}
	public int getCount() {
		
		return items.size();
	}

	public Pair getItem(int position) {
		
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
		Boolean isFolder = true;
		Pair<CourseMapItem, ArrayList> item = items.get(position);
		
		String title = item.first.getName();
		if(item.second.size()==0)
			isFolder = false;
		
		ViewGroup lin = (ViewGroup) convertView;
	
		if (lin == null)
			lin = (ViewGroup) li.inflate(R.layout.coursemap_item_view,null,false);
		
		TextView itemName = (TextView) lin.findViewById(R.id.coursemap_item_name);
		ImageView folder = (ImageView) lin.findViewById(R.id.coursemap_folder);
	
		folder.setVisibility(isFolder ? View.VISIBLE
									  : View.INVISIBLE);
		itemName.setText(title);

		return (View)lin;
	}
}
