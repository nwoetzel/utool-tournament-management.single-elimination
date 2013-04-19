package utool.plugin.singleelimination;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Holds the Player list with the permissions level for each player. Adjustable by Hosts only
 * @author waltzm
 *
 */
public class SingleEliminationOptionsActivity extends AbstractPluginCommonActivity
{

	/**
	 * Holds the arrayAdapter
	 */
	private OptionsPlayersAdapter ad;

	/**
	 * Log tag to be used in this class
	 */
	private static String logtag = "SE Options Activity";
	
	/**
	 * Holds a reference to the tournament
	 */
	private TournamentLogic t;

	/**
	 * Shared preferences key for getting if the screen has been visited before
	 */
	String firstTimeKey = "utool.plugin.singleelimination.OptionsActivity";

	/**
	 * Holds the error message
	 */
	private String error;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.se_options_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");

		//get list of players
		t = TournamentLogic.getInstance(getTournamentId());
		ArrayList<Player> players = t.getPlayers();
		Log.d(logtag, "Players: "+players.toString());

		//Shorten the player list to only include players with permission level of participant or moderator
		ArrayList<Player> parts = new ArrayList<Player>();
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getPermissionsLevel()==Player.PARTICIPANT||players.get(i).getPermissionsLevel()==Player.MODERATOR)
			{
				//add to list
				parts.add(players.get(i));
			}
		}

		//hide either no_player_text or Player and Moderator
		if(parts.size()<1)
		{
			//no players
			findViewById(R.id.no_player_text).setVisibility(View.VISIBLE);
			findViewById(R.id.op_mod_header).setVisibility(View.INVISIBLE);
			findViewById(R.id.op_player_header).setVisibility(View.INVISIBLE);
			findViewById(R.id.options_apply).setVisibility(View.INVISIBLE);
		}
		else
		{
			//players
			findViewById(R.id.no_player_text).setVisibility(View.GONE);
			findViewById(R.id.op_mod_header).setVisibility(View.VISIBLE);
			findViewById(R.id.op_player_header).setVisibility(View.VISIBLE);
			findViewById(R.id.options_apply).setVisibility(View.VISIBLE);
		}

		//hide everything
		findViewById(R.id.hint_1).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint_2).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint_3).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.hint_4).setVisibility(FrameLayout.INVISIBLE);
		
		//determine if help has been played yet
		SharedPreferences prefs = this.getSharedPreferences("utool.plugin.singleelimination", Context.MODE_PRIVATE);

		// use a default value to true (is first time)
		Boolean firstTime= prefs.getBoolean(firstTimeKey, true); 
		if(firstTime)
		{
			showHelp();

			//setup preferences to remember help has been played
			prefs.edit().putBoolean(firstTimeKey, false).commit();
		}

		//setup adapter
		ListView l = (ListView)findViewById(R.id.option_list);
		ad=new OptionsPlayersAdapter(this, R.id.option_list, parts);
		l.setAdapter(ad);

		//setup email send button
		Button email = (Button)findViewById(R.id.options_send);
		email.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) 
			{
				//pull out email
				EditText email = (EditText)findViewById(R.id.options_email);
				String em = email.getText().toString();
				String data =getTournamentData(t);
				Log.d(logtag,"Data from options: "+data);
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{em});
				i.putExtra(Intent.EXTRA_SUBJECT, t.getTournamentName(false)+": Tournament Matches up to Round "+((SingleEliminationTournament)t).getRound());
				i.putExtra(Intent.EXTRA_TEXT   , data);
				try {
					startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(SingleEliminationOptionsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}				
			}


		});

		//setup advanced options button
		Button adv = (Button)findViewById(R.id.options_advanced);
		adv.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) 
			{
				//launch advanced settings
				Intent i = getNewIntent(SingleEliminationOptionsActivity.this, SEAdvancedEmailOptions.class);
				startActivity(i);
			}
		});


		//setup apply button
		Button apply = (Button)findViewById(R.id.options_apply);
		apply.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) 
			{
				//save all of the permissions done automatically since player list is updated

				//re-send player list with updated permissions to connected devices
				TournamentLogic t = TournamentLogic.getInstance(getTournamentId());

				ArrayList<Player> p = t.getPlayers();//ad.getPlayers();

				Player[] l = new Player[p.size()];

				for(int i=0;i<p.size();i++)
				{
					l[i] = p.get(i);
				}

				t.getOutgoingCommandHandler().handleSendPlayers(getTournamentId(), l);

				finish();
			}

		});
	}


	/**
	 * Displays the help screen
	 */
	public void showHelp(){
		final Dialog dialog = new Dialog(SingleEliminationOptionsActivity.this);
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



	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 12/11/2012
	 */
	private class OptionsPlayersAdapter extends ArrayAdapter<Player>{

		/**
		 * Holds the list of players
		 */
		private ArrayList<Player> players;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param players the players
		 */
		public OptionsPlayersAdapter(Context context, int textViewResourceId, ArrayList<Player> players)
		{
			super(context, textViewResourceId, players);
			this.players = players;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.se_option_row, parent, false);

			// set name to players name
			TextView pName = (TextView)row.findViewById(R.id.se_options_name);
			pName.setText(players.get(position).getName());

			//set the player image to the imageview
			ImageView pPic = (ImageView)row.findViewById(R.id.se_options_pic);
			if(players.get(position).getPortrait()!=null)
			{
				Log.e(logtag, "Attempting to set drawable");
				pPic.setImageBitmap((players.get(position).getPortrait()));
			}

			//set the checkbox listener
			final CheckBox pCheck = (CheckBox)row.findViewById(R.id.se_options_check);
			pCheck.setOnCheckedChangeListener(new OnCheckChangedListener_Options(players.get(position)));
			int p = players.get(position).getPermissionsLevel();
			if(p==Player.HOST)
			{
				//checked and unchangeable
				pCheck.setChecked(true);
				//pCheck.setClickable(false);
				pCheck.setEnabled(false);

				pCheck.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						pCheck.setChecked(true);
					}

				});

			}
			else if(p==Player.PARTICIPANT)
			{
				//unchecked
				pCheck.setChecked(false);
			}
			else if(p == Player.MODERATOR)
			{
				//checked
				pCheck.setChecked(true);

			}
			pCheck.invalidate();
			row.invalidate();
			return row;
		}
	}

	/**
	 * Custom listener to update the player based on if the check box is checked
	 * If checked the player is set to Moderator, otherwise it is set to Participant
	 * @author waltzm
	 *
	 */
	private class OnCheckChangedListener_Options implements OnCheckedChangeListener
	{

		/**
		 * The player to update
		 */
		private Player player;

		/**
		 * Creates a OnCheckListener responsible for keeping the permissions
		 * of Player p in synch with the checkboxes
		 * @param player the player to be edited
		 */
		public OnCheckChangedListener_Options(Player player)
		{
			this.player = player;
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
		{
			if(isChecked)
			{
				player.setPermissionsLevel(Player.MODERATOR);
			}
			else
			{
				player.setPermissionsLevel(Player.PARTICIPANT);
			}

		}

	}

	/**
	 * Returns a String representation of the tournament matches with html formating
	 * Winners will be bolded
	 * @param t reference to the tournament
	 * @return tournament matchups so far
	 */
	public static String getTournamentData(TournamentLogic  t) 
	{
		Log.e(logtag,"Round: "+((SingleEliminationTournament)t).getRound());
		String tdata = "";

		if(t instanceof SingleEliminationTournament)
		{
			SingleEliminationTournament se = (SingleEliminationTournament)t;
			ArrayList<Matchup> m = ((SingleEliminationTournament) t).getBottomRound();
			if(m == null)
			{
				return "No Matches set";
			}
			//for each round
			boolean moreRounds = true;
			int round = 1;

			while(moreRounds)
			{
				//Log.d(logtag,"iteration: "+round);
				//round statement
				tdata+="<h2>Round "+round+": </h2>";
				//matchups in round
				for(int i=0; i<m.size();i++)
				{
					//determine if a name should be bolded
					if(m.get(i).getWinner()!=null)
					{
						//there is a winner
						if(m.get(i).getWinner().getUUID().equals(m.get(i).getPlayerOne().getUUID()))
						{
							//player 1 is winner therefore bold p1
							tdata+="<b>"+m.get(i).getPlayerOne().getName()+"</b>" +" vs. "+m.get(i).getPlayerTwo().getName() +"<br>";	
							
						}
						else
						{
							//player 2 is winner therefore bold p2
							tdata+=m.get(i).getPlayerOne().getName() +" vs. "+"<b>"+m.get(i).getPlayerTwo().getName()+"</b>" +"<br>";	
						}
					}
					else
					{
						//no winner
						tdata+=m.get(i).getPlayerOne().getName() +" vs. "+m.get(i).getPlayerTwo().getName() +"<br>";	
					}
						
				}
				//get next round of matchups
				round++;
				if(round> se.getRound())
				{
					//	Log.d(logtag,"no more cause of round");
					moreRounds = false;
				}
				else
				{
					m = getNextRound(m);
					if(m==null||m.size()<1)
					{
						//		Log.d(logtag,"no more cause of no parents");
						moreRounds = false;
					}
				}
				tdata+="<br>";//extra break
			}
		}

		return tdata;
	}

	/**
	 * Retrieves parents out of matchups and returns unique ones
	 * @param m list of matchups
	 * @return list of m's parent matchups
	 */
	private static ArrayList<Matchup> getNextRound(ArrayList<Matchup> m)
	{
		ArrayList<Matchup> parent = new ArrayList<Matchup>();

		for(int i=0;i<m.size();i++)
		{
			Matchup p = m.get(i).getParent();

			//determine if in list already
			if(p!=null)
			{
				boolean inList=false;
				//iterate through list
				for(int j=0;j<parent.size();j++)
				{
					if(parent.get(j).getId() == p.getId())
					{
						inList = true;
					}
				}
				//if not in list, add
				if(!inList)
				{
					Log.d(logtag,parent.size() +"    "+p.getId());
					parent.add(p);
				}
			}

		}
		return parent;
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

	

	/**
	 * Sends a toast to the screen to notify user msg wasn't sent
	 * @param message notifies user of an error
	 */
	public void notifyError(String message) {

		Log.e(logtag, "Sending email: "+message);   
		error = message;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Error Sending Email: "+error, Toast.LENGTH_LONG).show();
			}
		});

	}



}
