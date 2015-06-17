package utool.plugin.singleelimination;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.singleelimination.participant.Match;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


/**
 * This activity is the screen for displaying the players and their 
 * overall standings
 * 
 * Round should be passed into the extra's, otherwise the default is round 1
 * @author waltzm
 * @author hoguet
 * @version 4-21-13
 */
public class RoundStandingsActivity extends AbstractPluginCommonActivity
{
	/**
	 * Holds the listview adapter
	 */
	private RoundStandingsAdapter ad;

	/**
	 * Holds whether or not the list is ordered ascending or descending
	 */
	private boolean isAscending = false;

	/**
	 * Holds the matches of the tournament
	 */
	private ArrayList<Match> matchez;

	/**
	 * What name to call players who have not been decided yet
	 */
	private static String playerUndecided = "UNDECIDED";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_standings_round);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");
		

		//setup adapter
		ListView l = (ListView)findViewById(R.id.round_standings_list);

		//retrieve matchups
		Bundle b = getIntent().getExtras();
		int round = b.getInt("Round", 1);
		ArrayList<Match> matches = this.getMatchupsForRound(round);

		matchez = matches;

		matches = this.orderByMatch();

		ad=new RoundStandingsAdapter(this, R.id.round_standings_list, matches, b.getString("ActivePid"));
		l.setAdapter(ad);

		//hide everything
		findViewById(R.id.frameLayout_round).setVisibility(FrameLayout.INVISIBLE);
		findViewById(R.id.frameLayout_round_column).setVisibility(FrameLayout.INVISIBLE);

		//Setup ordering of the columns
		TextView match = (TextView) findViewById(R.id.order_by_match_round);
		match.setOnTouchListener(new FakeButtonOnTouchListener());
		match.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<Match> players = orderByMatch();
				ad.setMatches(players);
				ad.notifyDataSetChanged();
			}
		});

		//setup correct round number
		TextView num = (TextView) findViewById(R.id.round_standings_round_number);
		num.setText(""+round);
	}

	/**
	 * Gets the matchups from the given round in the tournament
	 * @param round the round to get the matches for
	 * @return the matches
	 */
	public ArrayList<Match> getMatchupsForRound(int round) 
	{
		ArrayList<Match> matches = new ArrayList<Match>();

		TournamentLogic tl = TournamentLogic.getInstance(getTournamentId());
		if(tl instanceof SingleEliminationTournament)
		{

			SingleEliminationTournament t = (SingleEliminationTournament)tl;

			ArrayList<Matchup> matchups;

			if(t.getPermissionLevel() == Player.HOST){
				matchups = t.getRound(round);
			}else{
				matchups = t.getRoundParticipant(round);
			}

			//			ArrayList<Matchup> old = ((SingleEliminationTournament)TournamentLogic.getInstance(getTournamentId())).getBottomRound();
			//			ArrayList<Matchup> matchups = ((SingleEliminationTournament)TournamentLogic.getInstance(getTournamentId())).getBottomRound();
			//			if(matchups==null)
			//			{
			//				return new ArrayList<Match>();
			//			}
			//			int index=1;
			//			while(index<round)
			//			{				
			//				//get the parents of the old matchups and put in matchups
			//				matchups.clear();
			//
			//				for(int i=0;i<old.size();i++)
			//				{
			//					matchups= addParent(matchups,old.get(i).getParent());
			//				}
			//
			//				if(matchups.size()<1)
			//				{
			//					//no matchups left so round is too large
			//					return matches;
			//				}
			//
			//				//create shallow clone
			//				old = new ArrayList<Matchup>();
			//				for(int i=0;i<matchups.size();i++)
			//				{
			//					old.add(matchups.get(i));
			//				}
			//
			//				index++;
			//			}

			if(matchups == null){
				return new ArrayList<Match>();
			}else{


				//list of matchups in matchups
				//convert to matches
				for(int i=0;i<matchups.size();i++)
				{
					Matchup mu = matchups.get(i);

					Player p1 = mu.getPlayerOne();
					if(p1==null)
					{
						p1=new Player(playerUndecided);
					}
					Player p2 = mu.getPlayerTwo();
					if(p2==null)
					{
						p2=new Player(playerUndecided);
					}

					Match m = new Match(mu.getId(), p1, p2, mu.getWinner(), round);
					if(mu.getScores()!=null)
					{
						m.setScores(mu.getScores()[0], mu.getScores()[1]);
					}
					else
					{
						m.setScores(0, 0);
					}
					matches.add(m);
				}
			}

		}


		return matches;
	}

	/**
	 * Displays the help screen
	 */
	public void showHelp(){
		final Dialog dialog = new Dialog(RoundStandingsActivity.this);
		dialog.setContentView(R.layout.round_standings_help);
		dialog.setTitle("Round Standings Help");
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
	 * Rounds decimal to one place
	 * @param d the double to round
	 * @return the rounded decimal
	 */
	@TargetApi(9)
	public static double roundOneDecimal(double d) 
	{
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		twoDForm.setRoundingMode(RoundingMode.HALF_UP);
		return Double.valueOf(twoDForm.format(d));
	}

	/**
	 * Rounds decimal to two place
	 * @param d the double to round
	 * @return the rounded decimal
	 */
	@TargetApi(9)
	public static double roundTwoDecimal(double d) 
	{
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		twoDForm.setRoundingMode(RoundingMode.HALF_UP);
		return Double.valueOf(twoDForm.format(d));
	}

	/**
	 * Orders the list by column passed in. 
	 * @return new list of matches
	 */
	private ArrayList<Match> orderByMatch()
	{
		ArrayList<Match> matches = new ArrayList<Match>();
		for(int i=0;i<matchez.size();i++)
		{
			matches.add(matchez.get(i));
		}


		//column clicked again so swap order
		isAscending = !isAscending;

		Collections.sort(matches, new MatchComparable(isAscending));

		return matches;
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.round_standings_menu, menu);
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
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 12/11/2012
	 */
	private class RoundStandingsAdapter extends ArrayAdapter<Match>{

		/**
		 * Holds the list of matches
		 */
		private ArrayList<Match> matches;
		
		/**
		 * UUID of the current user's player
		 */
		private UUID activePid;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param matches the matches
		 * @param activePid the UUID of the current user's player
		 */
		public RoundStandingsAdapter(Context context, int textViewResourceId, ArrayList<Match> matches, String activePid)
		{
			super(context, textViewResourceId, matches);
			this.matches = matches;
			this.activePid = UUID.fromString(activePid);
		}

		/**
		 * Setter for matches
		 * Must call notifyDataSetChanged() 
		 * after calling this method
		 * @param matches the new matches
		 */
		public void setMatches(ArrayList<Match> matches) 
		{
			this.matches = matches;		
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.standings_round_row, parent, false);

			//setup player pics
			Match m = matches.get(position);
			
			ImageView img1 = (ImageView)row.findViewById(R.id.prof_pic_one);
			Player p = m.getPlayerOne();
			if(p != null && p.getPortrait() != null){
				img1.setImageBitmap(p.getPortrait());
			}else{
				img1.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.silhouette));
			}

			ImageView img2 = (ImageView)row.findViewById(R.id.prof_pic_two);
			p = m.getPlayerTwo();
			if(p != null && p.getPortrait() != null){
				img2.setImageBitmap(p.getPortrait());
			}else{
				img2.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.silhouette));
			}

			//setup player information
			TextView match = (TextView)row.findViewById(R.id.match_round);
			match.setText(""+m.getId());

			TextView p1name = (TextView)row.findViewById(R.id.name_one_round);
			p1name.setText(""+m.getPlayerOne().getName());
			if(m.getWinner() != null && m.getWinner().equals(m.getPlayerOne())){
				p1name.setTypeface(null, Typeface.BOLD);
			}
			if(m.getPlayerOne().getUUID().equals(activePid)){
				p1name.setTextColor(Color.CYAN);
			}else{
				p1name.setTextColor(Color.WHITE);
			}

			TextView p2name = (TextView)row.findViewById(R.id.name_two_round);
			p2name.setText(""+m.getPlayerTwo().getName());
			if(m.getWinner() != null && m.getWinner().equals(m.getPlayerTwo())){
				p2name.setTypeface(null, Typeface.BOLD);
			}
			if(m.getPlayerTwo().getUUID().equals(activePid)){
				p2name.setTextColor(Color.CYAN);
			}else{
				p2name.setTextColor(Color.WHITE);
			}

			TextView p1score = (TextView)row.findViewById(R.id.score_one_round);			
			//get and format text
			double pts1 = m.getScoreP1();
			String ptz1 = pts1+"";
			if(pts1 == (double)((int)pts1))
			{
				//strip .0
				ptz1 = (int)pts1+"";
			}
			else
			{
				//round to 1 decimal place if over 10
				if(pts1>=10)
				{
					ptz1 = roundOneDecimal(pts1)+"";
				}
				else
				{
					ptz1 = roundTwoDecimal(pts1)+"";
				}
			}	
			p1score.setText(ptz1);

			TextView p2score = (TextView)row.findViewById(R.id.score_two_round);		
			//get and format text
			double pts = m.getScoreP2();
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
					ptz = roundOneDecimal(pts)+"";
				}
				else
				{
					ptz = roundTwoDecimal(pts)+"";
				}
			}

			p2score.setText(ptz);

			row.invalidate();
			return row;
		}
	}

	/**
	 * Comparable used for ranking according to player wins
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class MatchComparable implements Comparator<Match>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for organizing based on match id
		 * @param ascending true if ascending
		 */
		public MatchComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(Match p1, Match p2) 
		{
			Long i = p1.getId();
			Long i2 = p2.getId();
			int c = i.compareTo(i2);
			if(ascending)
				return c;
			else
				return c*-1;

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
