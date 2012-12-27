package com.nasageek.utexasutilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.foound.widget.AmazingAdapter;

public class TransactionAdapter extends AmazingAdapter
{
	private Context con;
	private ArrayList<Boolean> areHeaders;
	private ArrayList<String> transactions;
	private LayoutInflater li;
	private String currentDate;
	boolean isSectionHeader;
	private List<BasicNameValuePair> postdata;
	private SherlockFragment sf;
	
	public TransactionAdapter(Context c, SherlockFragment sf, ArrayList<String> objects)
	{
		con = c;
		transactions = objects;
		this.sf = sf;
		areHeaders = new ArrayList<Boolean>();
		li = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(int i = 0; i<transactions.size(); i++)
		{
			String dateandplace  = transactions.get(i).substring(0,transactions.get(i).indexOf("$"));
			String date = dateandplace.substring(0,dateandplace.indexOf(" "));
			if(i == 0)
			{
				currentDate = date;
				areHeaders.add(true);
			}
			else if(currentDate.equals(date))
			{
				areHeaders.add(false);
			}
			else 
			{
				areHeaders.add(true);
				currentDate=date;
			}
		}
		
	}
	public int getCount() {
		return transactions.size();
	}

	public Object getItem(int position) {
		return transactions.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}
	@Override
	public boolean isEnabled(int i)
	{
		return false;
	}
	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent)
	{
		
/*		if(position == transactions.size() - 1)
			return new View(con);*/
		String trans = transactions.get(position);
		
		String dateplace = trans.substring(0,trans.indexOf("$"));
		String date = dateplace.substring(0,dateplace.indexOf(" "));
		String place = "\t"+dateplace.substring(dateplace.indexOf(" "));
		String cost = trans.substring(trans.indexOf("$"));
		
		
		ViewGroup lin = (ViewGroup) convertView;
	
		if (areHeaders.size() == transactions.size() && areHeaders.get(position))
		{
			lin =(ViewGroup)li.inflate(R.layout.trans_item_header_view,null,false);
			TextView dateview = (TextView) lin.findViewById(R.id.list_item_section_text);
			dateview.setText(date);
		}
		else
		{
			lin = (ViewGroup)li.inflate(R.layout.trans_item_view,null,false);
		}
		
		TextView left= (TextView) lin.findViewById(R.id.itemview);
		left.setText(place);
		TextView right = (TextView) lin.findViewById(R.id.costview);
		right.setText(cost);

		return (View)lin;
	}
	public void updateHeaders()
	{
		areHeaders.clear();
		for(int i = 0; i<transactions.size(); i++)
		{
			String dateandplace  = transactions.get(i).substring(0,transactions.get(i).indexOf("$"));
			String date = dateandplace.substring(0,dateandplace.indexOf(" "));
			if(i == 0)
			{
				currentDate = date;
				areHeaders.add(true);
			}
			else if(currentDate.equals(date))
			{
				areHeaders.add(false);
			}
			else 
			{
				areHeaders.add(true);
				currentDate=date;
			}
		}	
	}
	protected View getLoadingView(ViewGroup parent)
	{
		if (con != null) {
		      LayoutInflater inflater=
		          (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		      return inflater.inflate(R.layout.loading_content_layout, parent, false);
		}
		else
			return new View(con);
	}
	@Override
	protected void onNextPageRequested(int page) {
		
		if(super.automaticNextPageLoading)
		{	
			nextPage();
			Log.d("TransactionAdapter","Page requested!");
			try {
				Method meth = sf.getClass().getMethod("parser",Boolean.TYPE);
				meth.invoke(sf, new Object[] {false});	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.automaticNextPageLoading = false;
	}
	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
		return;
	}
	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		return;
	}
	@Override
	public int getPositionForSection(int section) {
		return 0;
	}
	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}
	@Override
	public String[] getSections() {
		return null;
	}

}