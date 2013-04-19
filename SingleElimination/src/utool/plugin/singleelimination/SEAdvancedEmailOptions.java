package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.singleelimination.R;
import utool.plugin.singleelimination.email.AutomaticEmailHandler;

/**
 * Activity for handling the setting up of subscriber emails.
 * @author waltzm
 * @version 1/14/2013
 */
public class SEAdvancedEmailOptions extends AbstractPluginCommonActivity
{
	
	/**
	 * Log tag to be used in this class
	 */
	private static String logtag = "SE Advanced Email Options";
	
	/**
	 * Holds the arrayAdapter
	 */
	private AdvancedOptionsAdapter ad;

	/**
	 * Shared preferences key for getting if the screen has been visited before
	 */
	String firstTimeKey = "utool.plugin.singleelimination.SEAdvancedOptionsActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.se_options_email_advanced);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");

		//hide help menus
		//setup dialogs
		findViewById(R.id.hint1).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint2).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint3).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint4).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint5).setVisibility(FrameLayout.INVISIBLE);

		//setup adapter
		AutomaticEmailHandler a = TournamentLogic.getInstance(getTournamentId()).getAutomaticEmailHandler();
		ArrayList<String> emails = a.getSubscribers();
		int size = emails.size();
		emails.addAll(a.getPossibleSubscribers());

		ListView l = (ListView)findViewById(R.id.email_subscribers);
		ad=new AdvancedOptionsAdapter(this, R.id.email_subscribers, emails);
		l.setAdapter(ad);

		//load email addresses from preferences and add to list if unique
		SharedPreferences prefs = getSharedPreferences("utool.plugin.singleelimination", Context.MODE_PRIVATE);
		String em= prefs.getString("email_addresses", ""); 
		StringTokenizer e = new StringTokenizer(em, ",");
		while(e.hasMoreTokens())
		{
			addPossibleSubscriber(emails, e.nextToken());
		}

		ArrayList<Boolean> ton = new ArrayList<Boolean>();
		for(int i=0;i<emails.size();i++)
		{
			if(i<size)
			{
				ton.add(true);
			}
			else
			{
				ton.add(false);
			}
		}
		ad.turnedOn= ton ;
		ad.notifyDataSetChanged();

		//setup add button
		ImageButton plus = (ImageButton)findViewById(R.id.email_plus);
		plus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) 
			{
				EditText ea = (EditText)findViewById(R.id.email_address);

				//add typed in email to list
				ad.add(ea.getText().toString());
				ad.notifyDataSetChanged();
				reloadUI();

			}

		});

		//setup save button
		Button save = (Button)findViewById(R.id.adv_save);
		save.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) 
			{
				//save settings to tournament's email object and exit
				AutomaticEmailHandler a = TournamentLogic.getInstance(getTournamentId()).getAutomaticEmailHandler();
				ArrayList<String> subs = new ArrayList<String>();
				ArrayList<String> psubs = new ArrayList<String>();
				ArrayList<String> emails = ad.addresses;
				ArrayList<Boolean> on = ad.turnedOn;
				for(int i=0;i<emails.size();i++)
				{
					if(on.get(i))
					{
						//add to subscriber since checked
						subs.add(emails.get(i));
					}
					else
					{
						//add to possible subscriber since unchecked
						psubs.add(emails.get(i));
					}
				}

				//Log.e(logtag, subs);
				a.setSubscribers(subs);
				a.setPossibleSubscribers(psubs);

				String ems = "";
				for(int i=0;i<subs.size();i++)
				{
					ems+=subs.get(i)+",";
				}
				for(int i=0;i<psubs.size();i++)
				{
					ems+=psubs.get(i)+",";
				}

				//save list to preferences
				SharedPreferences prefs = getSharedPreferences("utool.plugin.singleelimination", Context.MODE_PRIVATE);
				prefs.edit().putString("email_addresses", ems).commit();
				finish();
			}

		});

		// use a default value to true (is first time)
		Boolean firstTime= prefs.getBoolean(firstTimeKey, true); 
		if(firstTime)
		{
			//setup preferences to remember help has been played
			prefs.edit().putBoolean(firstTimeKey, false).commit();
		}


		reloadUI();

	}

	/**
	 * Re-registers listview for the context menu
	 */
	private void reloadUI()
	{
		ListView l = (ListView)findViewById(R.id.email_subscribers);
		l.setOnCreateContextMenuListener(this);
		registerForContextMenu(l);
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_email_options_context_menu, menu);
		Log.d(logtag,"Inflating Menu");
	}


	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.context_delete:
			ad.addresses.remove(info.position);
			ad.turnedOn.remove(info.position);
			ad.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Adds nextToken to list if not already in emails
	 * @param emails list of addresses
	 * @param nextToken email to add if unique
	 */
	public void addPossibleSubscriber(ArrayList<String> emails, String nextToken) 
	{
		for(int i=0;i<emails.size();i++)
		{
			if(emails.get(i).equals(nextToken))
			{
				return;
			}
		}

		//not in list
		emails.add(nextToken);
	}



	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 12/11/2012
	 */
	private class AdvancedOptionsAdapter extends ArrayAdapter<String>{

		/**
		 * Holds the list of players
		 */
		private ArrayList<String> addresses;

		/**
		 * Holds whether addresses are subscribed
		 */
		private ArrayList<Boolean> turnedOn;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param addresses list of addresses
		 */
		public AdvancedOptionsAdapter(Context context, int textViewResourceId, ArrayList<String> addresses)
		{
			super(context, textViewResourceId, addresses);
			this.addresses = addresses;
			turnedOn = new ArrayList<Boolean>();
			for(int i=0;i<addresses.size();i++)
			{
				turnedOn.add(false);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.se_options_email_row, parent, false);

			//add address
			TextView adr = (TextView)row.findViewById(R.id.adv_address);
			adr.setText(addresses.get(position));

			CheckBox box = (CheckBox)row.findViewById(R.id.checkBox1);
			box.setOnCheckedChangeListener(new OnCheckChangedListener_AdvancedOptions(position));
			if(turnedOn.get(position))
			{

				box.setChecked(true);
			}
			else
			{
				box.setChecked(false);
			}



			row.invalidate();
			return row;
		}

		@Override
		public void add(String item)
		{
			addresses.add(item);
			turnedOn.add(true);
		}

		/**
		 * Turns the email address at the position on or off
		 * @param position the position of the address
		 * @param state on or off
		 */
		public void setTurnedOn(int position, boolean state)
		{
			turnedOn.set(position,state);
		}

	}


	/**
	 * Custom listener to update the player based on if the check box is checked
	 * If checked the player is set to Moderator, otherwise it is set to Participant
	 * @author waltzm
	 *
	 */
	private class OnCheckChangedListener_AdvancedOptions implements OnCheckedChangeListener
	{
		/**
		 * Holds the position
		 */
		private int position;

		/**
		 * Constructor that accepts the position for the checkbox.
		 * @param position the position
		 */
		public OnCheckChangedListener_AdvancedOptions(int position)
		{
			this.position = position;
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
		{
			ad.setTurnedOn(position, isChecked);
		}

	}

	/**
	 * Displays the help screen
	 */
	public void showHelp(){
		final Dialog dialog = new Dialog(SEAdvancedEmailOptions.this);
		dialog.setContentView(R.layout.options_help);
		dialog.setTitle("Options Help");
		dialog.setCancelable(true);
		Button closeButton = (Button) dialog.findViewById(R.id.home_help_close_button);
		closeButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				dialog.dismiss();     
			}
		});
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			showHelp();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
