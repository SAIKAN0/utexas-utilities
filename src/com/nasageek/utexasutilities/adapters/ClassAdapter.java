package com.nasageek.utexasutilities.adapters;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nasageek.utexasutilities.WrappingSlidingDrawer;
import com.nasageek.utexasutilities.model.Classtime;
import com.nasageek.utexasutilities.model.UTClass;


public class ClassAdapter extends BaseAdapter {

	private ArrayList<Classtime> cllist;
	private Context currentContext;
	private ArrayList<Boolean> firstlist;
	private SharedPreferences sp;
	private int currentTimePos=-1;
	private String time;
	private int day;
	private Calendar cal;
	
	private int currMinutes;
	
	private String empty_cell_pref;
	
	public ClassAdapter(Context c, WrappingSlidingDrawer wsd, LinearLayout llsd, ImageView ci_iv, TextView ci_tv, String semId, ArrayList<UTClass> classList) {
		sp = PreferenceManager.getDefaultSharedPreferences(c);
		empty_cell_pref = sp.getString("schedule_background_style", "checkhour");
		currentContext = c;
		
		updateTime();
		
		ArrayList<Classtime> cl = new ArrayList<Classtime>(50);

		for(UTClass clz : classList) {
			for(Classtime clzt : clz.getClassTimes())
				cl.add(clzt);
		}

		firstlist = new ArrayList<Boolean>();
		
		cllist = new ArrayList<Classtime>();
		cllist.ensureCapacity(180);
		firstlist.ensureCapacity(180);
		for(int x = 0; x<180; x++){	cllist.add(null);firstlist.add(false);}
		
		for(int i = 0; i < cl.size(); i++) {
			Classtime ct = cl.get(i);
		
			int startpos = timeToPos(ct.getStartTime());
			int endpos = timeToPos(ct.getEndTime());
			switch(ct.getDay()) {
			case 'M':
				for(int a = 0; a<(endpos-startpos); a++) {
					cllist.set(0+5*startpos+a*5, ct);
					if(a==0)
						firstlist.set(0+5*startpos+a*5, true);
				}break;
			case 'T':
				for(int a = 0; a<(endpos-startpos); a++) {
					cllist.set(1+5*startpos+a*5, ct);
					if(a==0)firstlist.set(1+5*startpos+a*5, true);
				}break;
			//TODO: 2+5*startpos+a*5 == -9 ? getting an exception from someone
			case 'W':
				for(int a = 0; a<(endpos-startpos); a++) {
					cllist.set(2+5*startpos+a*5, ct);
					if(a==0)firstlist.set(2+5*startpos+a*5, true);
				}break;
			case 'H':
				for(int a = 0; a<(endpos-startpos); a++) {
					cllist.set(3+5*startpos+a*5, ct);
					if(a==0)firstlist.set(3+5*startpos+a*5, true);
				}break;
			case 'F':
				for(int a = 0; a<(endpos-startpos); a++) {
					cllist.set(4+5*startpos+a*5, ct);
					if(a==0)firstlist.set(4+5*startpos+a*5, true);
				}break;
			}	
		}	
	}
	public void updateTime() {
		cal = Calendar.getInstance();
		day = cal.get(Calendar.DAY_OF_WEEK)-2;
		time = cal.get(Calendar.HOUR)+(cal.get(Calendar.MINUTE)>=30?":30":":00")+ (cal.get(Calendar.AM_PM)==Calendar.PM?"pm":"");
		
		if(day<5 && day>=0 && cal.get(Calendar.HOUR_OF_DAY)<=22 && cal.get(Calendar.HOUR_OF_DAY)>=6) {
			currentTimePos = day+5*timeToPos(time);
			currMinutes = cal.get(Calendar.MINUTE) % 30;
		}
		//currentTimePos = day+5*timeToPos(time);
	}
	//6am is at position 0
	private int timeToPos(String time)
	{
		String[] temp = time.split(":");
		int pos = Integer.parseInt(temp[0])*2 - 12;
		if(temp[1].contains("pm") && pos != 12)
			pos+=24;
		if(temp[1].charAt(0) == '3')
			pos++;
		return pos;
	}
	public int getEarliestClassPos() {
		for(int i = 0; i < firstlist.size(); i++) {
			if(firstlist.get(i))
				return i;
		}
		return 0;
	}
	@Override
	public int getCount() {
		return cllist.size();
	}
	@Override
	public Object getItem(int position) {
		return cllist.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		TextView iv;
		
		if(convertView==null)   
			iv = new TextView(currentContext);
		else	
			iv =(TextView) convertView;
		
        iv.setTextColor(Color.BLACK); 
        iv.setTextSize(13f); //11.75 for full
        if(cllist.get(position)==null) {	
        	if(position == currentTimePos) {	
        		Drawable currentMinutesLine = new ShapeDrawable(new Shape() {
					
					@Override
					public void draw(Canvas canvas, Paint paint) {
						paint.setStrokeWidth(3f);
						paint.setColor(Color.BLACK);
						paint.setStyle(Paint.Style.STROKE);
						canvas.drawColor(getEmptyCellColor(position));

						Paint blur = new Paint(paint);
						blur.setStrokeWidth(3f);
						blur.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.SOLID));
						canvas.drawLine(0, (int)((currMinutes/30.0)*getHeight() + .5), 
								getWidth(), (int)((currMinutes/30.0)*getHeight() + .5), paint);
	
					}
				});
        		iv.setBackgroundDrawable(currentMinutesLine);
        	}
        	else {	
        		iv.setBackgroundColor(getEmptyCellColor(position));
        		iv.setText("");
        	}
        }
        else {	
        	final Classtime cl = cllist.get(position);
        	final String color = "#"+cl.getColor();

        	if(position == currentTimePos) {	
        		Drawable currentMinutesLine = new ShapeDrawable(new Shape() {
					
					@Override
					public void draw(Canvas canvas, Paint paint) {
						paint.setStrokeWidth(3f);
						paint.setColor(Color.BLACK);
						paint.setStyle(Paint.Style.STROKE);
						canvas.drawColor(Color.parseColor(color));
						Paint blur = new Paint(paint);
						blur.setStrokeWidth(3f);
						blur.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.SOLID));
						canvas.drawLine(0, (int)((currMinutes/30.0)*getHeight() + .5), 
								getWidth(), (int)((currMinutes/30.0)*getHeight() + .5), paint);

					}
				});
        		iv.setBackgroundDrawable(currentMinutesLine);
        	}
        	else
        		iv.setBackgroundColor(Color.parseColor(color));
        	
        	if(firstlist.get(position)) {	
        		iv.setText(cllist.get(position).getStartTime());
        		iv.setGravity(Gravity.CENTER_HORIZONTAL);
        //		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        //		llp.setMargins(0, 1, 0, 0);
       // 		iv.setLayoutParams(llp);
       // 		LinearLayout ll = new LinearLayout(currentContext);
        //		ll.addView(iv);
        //		ll.setPadding(0, 1, 0, 1);
        		
       // 		return ll;
        	}
        	else
        		iv.setText("");	
        }
		return iv;
	}
	private int getEmptyCellColor(int position) {
		int darkgray = 0xFFcecece;
		int lightgray = 0xFFdcdcdc;
		
		if(empty_cell_pref.equals("checkhour")) {
    		if((position/10)%2==0) {
    			if((position/5)%2==0)
    				return position%2==0?lightgray:darkgray;
    			else
    				return position%2==0?darkgray:lightgray;
    		}
    		else {
    			if((position/5)%2==0)
    				return position%2==0?darkgray:lightgray;
    			else
    				return position%2==0?lightgray:darkgray;
    		}
		}	
		else if(empty_cell_pref.equals("checkhalf"))
			return position%2==0 && (position%10)%2==0?lightgray:darkgray;
		else if(empty_cell_pref.equals("stripehour"))
			return position/10%2==0?lightgray:darkgray;
		else if(empty_cell_pref.equals("stripehalf"))
			return position/5%2==0?lightgray:darkgray;
		else
			return Color.BLACK;
	}
}
