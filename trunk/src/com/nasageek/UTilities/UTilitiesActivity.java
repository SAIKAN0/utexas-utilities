package com.nasageek.UTilities;

import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UTilitiesActivity extends SherlockActivity {
    
	ProgressDialog pd; 
	SharedPreferences settings;
	Intent about_intent;
	private Menu menu;
	ActionBar actionbar;
	Toast message;
	 AnimationDrawable frameAnimation;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  //    Window win = getWindow();
        settings = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
   //   win.setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.main);
        setSupportProgressBarIndeterminateVisibility(false);
  //    final Intent exams = new Intent(getBaseContext(), ExamScheduleActivity.class);
        final Intent schedule = new Intent(getBaseContext(), ScheduleActivity.class);
    	final Intent balance = new Intent(getBaseContext(), BalanceActivity.class);
    	final Intent map = new Intent(getBaseContext(), CampusMapActivity.class);
    	final Intent data = new Intent(getBaseContext(), DataUsageActivity.class);
    	about_intent = new Intent(this, AboutMeActivity.class);
    	
    	actionbar = getSupportActionBar();
    	
    	message = Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT);
    	
    	
    	
    	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)	
    		actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg));
   // 	BitmapDrawable bmd = (BitmapDrawable) getResources().getDrawable(R.drawable.main_background);
   // 	bmd.setDither(true);
       
  /*    TableLayout tl = (TableLayout) findViewById(R.id.button_table);
        tl.setBackgroundDrawable(bmd);
        */
        if(!ConnectionHelper.cookieHasBeenSet() && (!settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
        {
        	AlertDialog.Builder nologin_builder = new AlertDialog.Builder(this);
        	nologin_builder.setMessage("It would seem that you are not logged into UTDirect yet, why don't we take care of that?")
        			.setCancelable(false)
        			.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    loadSettings();
                }
            })
            .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                }
            });
        	AlertDialog nologin = nologin_builder.create();
        	nologin.show();
        }
        if(settings.getBoolean("autologin", false))
        {
        	login(); 
        }
        
        final ImageButton schedulebutton = (ImageButton) findViewById(R.id.schedule_button);
        schedulebutton.setBackgroundResource(R.drawable.schedule_button_anim);

        // Get the background, which has been compiled to an AnimationDrawable object.
        frameAnimation = (AnimationDrawable) schedulebutton.getBackground();
        
        // Start the animation (looped playback by default).
        
        schedulebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	frameAnimation.start();
            	if(!ConnectionHelper.cookieHasBeenSet() && new ClassDatabase(UTilitiesActivity.this).size()==0)// && (!settings.getBoolean("loginpref", true)||!settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		message.setText(R.string.login_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	
            	else
            	{
            		startActivity(schedule);
            	}
            }
            
    });
        final ImageButton balancebutton = (ImageButton) findViewById(R.id.balance_button);
        balancebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(!ConnectionHelper.cookieHasBeenSet()) /*&& 
            			(!settings.getBoolean("loginpref", true)||
            					!settings.contains("eid") || 
            					!settings.contains("password")||
            					settings.getString("eid", "error").equals("")||
            					settings.getString("password", "error").equals("")))*/
            	{
            		message.setText(R.string.login_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	else{
            
            	startActivity(balance);
            		}
            }
            
    });
        final ImageButton mapbutton = (ImageButton) findViewById(R.id.map_button);
        mapbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
         		pd = ProgressDialog.show(UTilitiesActivity.this, "", "Loading. Please wait...");
            
            	startActivity(map);
            		
            }
            
    });
        final ImageButton databutton = (ImageButton) findViewById(R.id.data_button);
        databutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	if(!ConnectionHelper.PNACookieHasBeenSet())// && (!settings.getBoolean("loginpref", true)|| !settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		message.setText(R.string.login_first);
                	message.setDuration(Toast.LENGTH_SHORT);
            		message.show();
            	}
            	else{
            
            	startActivity(data);
            		}
            		
            }
            
    });
 /*       final ImageButton examsbutton = (ImageButton) findViewById(R.id.exams_button);
        examsbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	if(!ConnectionHelper.cookieHasBeenSet() )//&& (!settings.getBoolean("loginpref", true)|| !settings.contains("eid") || !settings.contains("password")||settings.getString("eid", "error").equals("")||settings.getString("password", "error").equals("")))
            	{
            		Toast.makeText(UTilitiesActivity.this, "Please log in before using this feature",Toast.LENGTH_SHORT).show();
            	}
            	else
            	{
            		startActivity(exams);	
            	}
            		
            }
            
    });*/
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.layout.main_menu, menu);
        
  /*      if(settings.getBoolean("loginpref", false))
    	{
    		menu.removeItem(R.id.login);
    		menu.removeItem(11);
    	}*/
    	if(ConnectionHelper.cookieHasBeenSet())
    	{
    		menu.removeItem(R.id.login);
    		menu.removeItem(11);
    		menu.add(Menu.NONE, 11, Menu.NONE, "Log out");
    		MenuItem item = menu.findItem(11);
    		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	}
    	else if(!ConnectionHelper.cookieHasBeenSet())
    	{
    		menu.removeItem(R.id.login);
    		menu.removeItem(11);
    		menu.add(Menu.NONE, R.id.login, Menu.NONE, "Log in");
    		MenuItem item = menu.findItem(R.id.login);
    		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	}
    	
        
        
        return true;
    }
/*    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	
    	
    	 return super.onPrepareOptionsMenu(menu);
    }*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	int id = item.getItemId();
    	switch(id)
    	{
    		case R.id.login:login();invalidateOptionsMenu();break;
    		case R.id.settings:loadSettings();break;
    		case R.id.about:aboutMe();break;
    		case 11:logout();invalidateOptionsMenu();break;
    	}
    	return true;
    }
    public void aboutMe()
    {
    	
    	startActivity(about_intent);
    }
    public void loadSettings()
    {
    	Intent pref_intent = new Intent(this, Preferences.class);
    	startActivity(pref_intent);
    }
    
    public void login()
    {
    	if(settings.getBoolean("loginpref", false))
    	{
    		if( !settings.contains("eid") || 
      				 !settings.contains("password") || 
      				 settings.getString("eid", "error").equals("") ||
      				 settings.getString("password", "error").equals("") )
    				 {	
      				 message.setText("Please enter your credentials to log in");
          			 message.setDuration(Toast.LENGTH_LONG);
          			 message.show();
    				 }
           	else
           	{
           		message.setText("Logging in...");
         		message.setDuration(Toast.LENGTH_SHORT);
         		message.show();
         			 
           		setSupportProgressBarIndeterminateVisibility(true);
           		ConnectionHelper ch = new ConnectionHelper(this);
      			DefaultHttpClient httpclient = ConnectionHelper.getThreadSafeClient();
      			DefaultHttpClient pnahttpclient = ConnectionHelper.getThreadSafeClient();

      			ConnectionHelper.resetPNACookie();

      			ch.new loginTask(this,httpclient,pnahttpclient).execute(ch);
      			ch.new PNALoginTask(this,httpclient,pnahttpclient).execute(ch);
           	}
    	}
    	else
    	{
	    	Intent login_intent = new Intent(this, LoginActivity.class);
	    	startActivity(login_intent);
    	}
    }
    public void logout()
    {
    	ConnectionHelper.logout(this);
    	message.setText("You have been successfully logged out");
    	message.show();
    }
    public void onResume()
    {
    	super.onResume();
    	invalidateOptionsMenu();
    	if(pd!=null)
    		pd.dismiss();
    }
    public void onStart()
    {
    	super.onStart();
    	if(pd!=null)
    		pd.dismiss();
    }

}