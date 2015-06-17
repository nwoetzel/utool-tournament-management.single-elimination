package utool.plugin.singleelimination;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import utool.plugin.activity.AbstractPluginCommonActivity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity is the screen for displaying the players and their 
 * overall standings
 * @author waltzm
 * @author hoguet
 * @version 4/21/2013
 */
public class OverallStandingsActivity extends AbstractPluginCommonActivity
{
	/**
	 * Order the list by the player rank
	 */
	public final static int ORDER_BY_RANK = 1;	

	/**
	 * Order the list by the player name
	 */
	public final static int ORDER_BY_NAME = 2;

	/**
	 * Order the list by the number of round wins
	 */
	public final static int ORDER_BY_ROUND_WINS = 3;

	/**
	 * Order the list by the round losses
	 */
	public final static int ORDER_BY_ROUND_LOSSES = 4;

	/**
	 * Order by the overall win score
	 */
	public final static int ORDER_BY_SCORE = 5;

	/**
	 * Holds whether or not the list is ordered ascending or descending
	 */
	private boolean isAscending = false;

	/**
	 * Holds which ordering scheme is selected
	 */
	private int currentSelection = ORDER_BY_RANK;

	/**
	 * Holds the listview adapter
	 */
	private OverallStandingsAdapter ad;

	/**
	 * Log tag to be used in this class
	 */
	private static String logtag = "SE Overall Standings Activity";

	/**
	 * Holds the players of the tournament
	 */
	private ArrayList<Participant> playerz;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_standings_overall);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");
		

		//setup adapter
		ListView l = (ListView)findViewById(R.id.overall_standings_list);


		//get list from StandingsGenerator
		playerz = TournamentLogic.getInstance(getTournamentId()).getStandingsGenerator().getParticipants();

		//order arraylist  by rank ascending
		ArrayList<Participant> players = this.orderByColumn(ORDER_BY_RANK);

		Bundle b = getIntent().getExtras();
		ad=new OverallStandingsAdapter(this, R.id.overall_standings_list, players, b.getString("ActivePid"));
		l.setAdapter(ad);


		//hide everything
		findViewById(R.id.frameLayout_losses).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.frameLayout_overall).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.frameLayout_score).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.frameLayout_wins).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.frameLayout_overall_column).setVisibility(FrameLayout.INVISIBLE);
		
		//Setup ordering of the columns
		TextView rank = (TextView) findViewById(R.id.order_by_rank_overall);
		rank.setOnTouchListener(new FakeButtonOnTouchListener());
		rank.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);
				
				ArrayList<Participant> players = orderByColumn(ORDER_BY_RANK);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView name = (TextView) findViewById(R.id.order_by_name_overall);
		name.setOnTouchListener(new FakeButtonOnTouchListener());
		name.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);
				
				ArrayList<Participant> players = orderByColumn(ORDER_BY_NAME);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView score = (TextView) findViewById(R.id.order_by_s_overall);
		score.setOnTouchListener(new FakeButtonOnTouchListener());
		score.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);
				
				ArrayList<Participant> players = orderByColumn(ORDER_BY_SCORE);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView wins = (TextView) findViewById(R.id.order_by_w_overall);
		wins.setOnTouchListener(new FakeButtonOnTouchListener());
		wins.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);
				
				ArrayList<Participant> players = orderByColumn(ORDER_BY_ROUND_WINS);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView losses = (TextView) findViewById(R.id.order_by_l_overall);
		losses.setOnTouchListener(new FakeButtonOnTouchListener());
		losses.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);
				
				ArrayList<Participant> players = orderByColumn(ORDER_BY_ROUND_LOSSES);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});


	}

	/**
	 * Orders the list by column passed in. 
	 * @param columnClicked the column clicked
	 * @return new list of participants
	 */
	private ArrayList<Participant> orderByColumn(int columnClicked)
	{
		ArrayList<Participant> players = new ArrayList<Participant>();
		for(int i=0;i<playerz.size();i++)
		{
			players.add(playerz.get(i));
		}
		//determine ascending or not
		if(currentSelection == columnClicked)
		{
			//column clicked again so swap order
			isAscending = !isAscending;
		}
		else
		{
			//new col clicked, so order ascending
			isAscending =true;
		}

		//save current selection
		this.currentSelection = columnClicked;
		switch(columnClicked)
		{
		case ORDER_BY_RANK:
			Collections.sort(players, new ParticipantRankComparable(isAscending, players.size()));
			break;
		case ORDER_BY_NAME:
			Collections.sort(players, new ParticipantNameComparable(isAscending));
			break;
		case ORDER_BY_ROUND_WINS:
			Collections.sort(players, new ParticipantWinsComparable(isAscending));
			break;
		case ORDER_BY_ROUND_LOSSES:
			Collections.sort(players, new ParticipantLossesComparable(isAscending));
			break;
		case ORDER_BY_SCORE:
			Collections.sort(players, new ParticipantScoreComparable(isAscending));
			break;

		}

		Log.d(logtag,"Ascending: "+isAscending);

		return players;
	}
	
	/**
	 * Displays the help screen
	 */
	public void showHelp(){
		final Dialog dialog = new Dialog(OverallStandingsActivity.this);
		dialog.setContentView(R.layout.standings_help);
		dialog.setTitle("Standings Help");
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
		inflater.inflate(R.menu.overall_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			Log.d(logtag,"help clicked");
			//show help
			showHelp();



			break;
		default:
			Log.d(logtag, "didnt find menu item");
			break;
		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * Comparable used for ranking according to player rank
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantRankComparable implements Comparator<Participant>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;
		
		/**
		 * The number of players
		 */
		private int size=1;
		

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 * @param size number of players
		 */
		public ParticipantRankComparable(boolean ascending, int size)
		{
			this.ascending = ascending;
			this.size=size;
		}

		public int compare(Participant p1, Participant p2) 
		{
			if(p1.getStanding(size) == p2.getStanding(size))
			{
				return 0;
			}
			else if(p1.getStanding(size) > p2.getStanding(size))
			{
				if(ascending)
					return 1;
				else
					return -1;
			}
			else
			{
				if(ascending)
					return -1;
				else
					return 1;
			}
		}
	}


	/**
	 * Comparable used for ranking according to player name
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantNameComparable implements Comparator<Participant>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantNameComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(Participant p1, Participant p2) 
		{
			int c = p1.getName().compareTo(p2.getName());
			if(ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * Comparable used for ranking according to player wins
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantWinsComparable implements Comparator<Participant>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantWinsComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(Participant p1, Participant p2) 
		{
			Integer i = p1.getTotalRoundWins();
			Integer i2 = p2.getTotalRoundWins();
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * Comparable used for ranking according to player losses
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantLossesComparable implements Comparator<Participant>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantLossesComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(Participant p1, Participant p2) 
		{
			Integer i = p1.getTotalRoundLosses();
			Integer i2 = p2.getTotalRoundLosses();
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}


	/**
	 * Comparable used for ranking according to player wins
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantScoreComparable implements Comparator<Participant>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantScoreComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(Participant p1, Participant p2) 
		{
			Double i = p1.getTotalWins();
			Double i2 = p2.getTotalWins();
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 12/11/2012
	 */
	private class OverallStandingsAdapter extends ArrayAdapter<Participant>{

		/**
		 * Holds the list of players
		 */
		private ArrayList<Participant> players;
		
		/**
		 * The UUID of the current user
		 */
		private UUID activePid;


		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param players the players
		 * @param activePid the pid of the using player
		 */
		public OverallStandingsAdapter(Context context, int textViewResourceId, ArrayList<Participant> players, String activePid)
		{
			super(context, textViewResourceId, players);
			this.players = players;
			this.activePid = UUID.fromString(activePid);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.standings_overall_row, parent, false);

			//setup player profile
			ImageView img = (ImageView)row.findViewById(R.id.prof_pic);
			Participant p = players.get(position);
			if(p != null && p.getPortrait() != null){
				img.setImageBitmap(p.getPortrait());
			}else{
				img.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.silhouette));
			}
			
			//setup player information

			TextView rank = (TextView)row.findViewById(R.id.rank_overall);
			rank.setText(""+p.getStanding(players.size()));

			TextView name = (TextView)row.findViewById(R.id.name_overall);
			name.setText(""+p.getName());
			if(p.getId().equals(activePid)){
				name.setTextColor(Color.CYAN);
			}else{
				name.setTextColor(Color.WHITE);
			}

			TextView wins = (TextView)row.findViewById(R.id.wins_overall);
			wins.setText(""+p.getTotalRoundWins());

			TextView losses = (TextView)row.findViewById(R.id.losses_overall);
			losses.setText(""+p.getTotalRoundLosses());

			TextView score = (TextView)row.findViewById(R.id.score_overall);

			//get and format text
			double pts = p.getTotalWins();
			String ptz = pts+"";
			if(pts == (double)((int)pts))
			{
				//strip .0
				ptz = (int)pts+"";
			}
			else
			{
				//round to 1 decimal place if over 10
				if(pts>=10)
				{
					ptz = RoundStandingsActivity.roundOneDecimal(pts)+"";
				}
				else
				{
					ptz = RoundStandingsActivity.roundTwoDecimal(pts)+"";
				}
			}
			score.setText(ptz);

			row.invalidate();
			return row;
		}


		/**
		 * Sets the players
		 * Must notify this that stuff changed
		 * @param players the new player list
		 */
		public void setPlayers(ArrayList<Participant> players)
		{
			this.players = players;
		}
	}
	
	
	
	/**
	 * Makes the View look clickable by changing the background color
	 * when clicked. The onClick listener should be used to change
	 * the background color back to black for complete look.
	 * @author waltzm
	 * @version 12/27/12
	 */
	private class FakeButtonOnTouchListener implements OnTouchListener
	{

		public boolean onTouch(View arg0, MotionEvent event) 
		{
			if(event.getAction()==0)
			{
				arg0.setBackgroundColor(Color.GRAY);
			}
			else
			{
				arg0.setBackgroundColor(Color.BLACK);
			}
			return false;
		}
		
	}

}
