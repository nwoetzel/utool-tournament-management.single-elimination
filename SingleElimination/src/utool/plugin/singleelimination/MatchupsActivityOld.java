package utool.plugin.singleelimination;



public class MatchupsActivityOld{
	
}

//package utool.plugin.singleelimination;
//
//import java.util.ArrayList;
//import utool.plugin.Player;
//import utool.plugin.activity.AbstractPluginCommonActivity;
//import utool.plugin.singleelimination.R;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.drawable.BitmapDrawable;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.View;
//import android.view.Window;
//import android.view.View.OnClickListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ExpandableListView;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
///**
// * Activity for displaying "Matchups" screen.  
// * @author hoguet
// * 
// * 10-14-12
// *
// */
//public class MatchupsActivity extends AbstractPluginCommonActivity {
//	
//	/**
//	 * Handler used for receiving messages from timer
//	 */
//	public Handler timerHandler;
//
//	/**
//	 * The tournament associated with this activity
//	 */
//	private SingleEliminationTournament tournament;
//
//	/**
//	 * Adapter that attaches children to groups
//	 */
//	private ExpandableRoundsListAdapter expAdapter;
//
//	/**
//	 * Groups of expandList.  Corresponds with rounds in the tournament
//	 */
//	private ArrayList<ExpandableRoundsListGroup> expListItems;
//
//	/**
//	 * ExpandableList that contains tournament matchup info
//	 */
//	private ExpandableListView expandList;
//
//	/**
//	 * Used when player selected to be removed; that player is stored here so it can be handled.
//	 */
//	private Player selectedPlayerToRemove;
//
//	private static final int USE_DEFAULT_SIZE = -1;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) 
//	{
//		super.onCreate(savedInstanceState);
//		
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//		setContentView(R.layout.activity_matchups_main);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
//		
//		TextView titleLabel = (TextView) findViewById(R.id.title);
//		titleLabel.setText("Single Elimination");
//		
//		TournamentLogic t = TournamentLogic.getInstance(getTournamentId());
//		if(t instanceof SingleEliminationTournament)
//		{
//			tournament = (SingleEliminationTournament) t;
//		}
//		else
//		{
//			//will throw null pointer in the next next line
//			Log.e("MatchupsActivity","Tournament created incorrectly. Tournament is not of type SingleEliminationTournament.");
//		}
//		
//
//		if (tournament.getMatchups().size() == 0){
//			//used to be tournament.generateRandomMatchups?
//			ArrayList<Matchup> matchups = SingleEliminationTournament.generateRandomMatchups(tournament.getPlayers(), tournament);
//			tournament.setMatchups(matchups);
//		}
//		
//		timerHandler = new Handler(){
//			
//			@Override
//	        public void handleMessage(Message msg) {
//				
//					String secondsString = ""+tournament.getSeconds();
//					if(tournament.getSeconds() < 10){
//						secondsString = "0"+tournament.getSeconds();
//					}
//	        	
//	               setTimerText(tournament.getMinutes()+":"+secondsString); //this is the textview
//	               
//	               if(tournament.getSeconds() == tournament.getRoundDuration()){
//	            	   refreshRoundsList();
//	               }
//	        }
//		};
//		
//		//currently hard coded setting of round timer
//		tournament.setRoundTimer(35, timerHandler);	
//		
//		
//		
//		//TOURNAMENT NAME BUTTON
//		Button tournamentNameBtn = (Button) findViewById(R.id.mu_tournamentNameBtn);
//		tournamentNameBtn.setText(tournament.getTournamentName(false)); //what param?
//		tournamentNameBtn.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				
//				//go to standings screen
//				Intent i = getNewIntent(MatchupsActivity.this,SingleEliminationStandingsActivityDescriptive.class);
//				startActivity(i);
//				
//			}			
//			
//		});
//		
//		//ROUND BUTTON
//		Button roundBtn = (Button) findViewById(R.id.mu_roundBtn);
//		
//		String timeString = "";
//		if(tournament.getRoundDuration() != SingleEliminationTournament.DEFAULT_NO_TIMER){
//			timeString = ""+ tournament.getMinutes() +":"+tournament.getSeconds();
//		}
//		roundBtn.setText("Round "+tournament.getRound()+timeString);
//		
//		//TODO those onclicks and shit
//		
//
//		expandList = (ExpandableListView) findViewById(R.id.expandableRoundsList);
//		expListItems = setStandardGroups(tournament.getMatchups(), USE_DEFAULT_SIZE);
//		expAdapter = new ExpandableRoundsListAdapter(MatchupsActivity.this, expListItems, this);
//		expandList.setAdapter(expAdapter);
//
//		//ADD PLAYERS BUTTON FUNCTIONALITY
//		final Button addPlayersBtn = (Button) findViewById(R.id.addPlayersBtn);
//		addPlayersBtn.setOnClickListener(new OnClickListener(){
//
//
//			public void onClick(View v) {
//				int popupWidth = 600;
//				int popupHeight = 400;
//
//				// Inflate the scores_popup_layout.xml
//				RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.popup);
//				LayoutInflater layoutInflater = (LayoutInflater) MatchupsActivity.this
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				final View layout = layoutInflater.inflate(R.layout.add_players_popup_layout, viewGroup);
//
//				// Creating the PopupWindow
//				final PopupWindow popup = new PopupWindow(MatchupsActivity.this);
//				popup.setContentView(layout);
//				popup.setWidth(popupWidth);
//				popup.setHeight(popupHeight);
//				popup.setFocusable(true);
//
//				// Clear the default translucent background
//				popup.setBackgroundDrawable(new BitmapDrawable());
//
//				// Displaying the popup at the specified location, + offsets
//				popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);
//
//				// Getting a reference to Close button, and close the popup when clicked.
//				Button close = (Button) layout.findViewById(R.id.close);
//				close.setOnClickListener(new OnClickListener() {
//
//
//					public void onClick(View v) {
//						popup.dismiss();
//					}
//				});
//
//				Button addPlayerBtn = (Button) layout.findViewById(R.id.addPlayerBtn);
//				addPlayerBtn.setOnClickListener(new OnClickListener(){
//
//					public void onClick(View v){
//
//						EditText et = (EditText) layout.findViewById(R.id.playerNameText);
//						String newPlayerName = et.getText().toString();
//						Player newPlayer = new Player(newPlayerName);
//
//						tournament.addPlayer(newPlayer);
//
//						refreshRoundsList();
//						popup.dismiss();
//
//
//					}
//				});
//
//
//
//			}
//
//		});
//
//
//		//REMOVE PLAYERS BUTTON FUNCTIONALITY
//		final Button removePlayersBtn = (Button) findViewById(R.id.removePlayersBtn);
//		removePlayersBtn.setOnClickListener(new OnClickListener(){
//
//			public void onClick(View v) {
//				int popupWidth = 500;
//				int popupHeight = 600;
//
//				// Inflate the scores_popup_layout.xml
//				RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.popup);
//				LayoutInflater layoutInflater = (LayoutInflater) MatchupsActivity.this
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				final View layout = layoutInflater.inflate(R.layout.remove_players_popup_layout, viewGroup);
//
//				// Creating the PopupWindow
//				final PopupWindow popup = new PopupWindow(MatchupsActivity.this);
//				popup.setContentView(layout);
//				popup.setWidth(popupWidth);
//				popup.setHeight(popupHeight);
//				popup.setFocusable(true);
//
//				// Clear the default translucent background
//				popup.setBackgroundDrawable(new BitmapDrawable());
//
//				// Displaying the popup at the specified location, + offsets
//				popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);
//
//				ListView lv = (ListView) layout.findViewById(R.id.playersListView);
//				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);			
//
//				ArrayAdapter<Player> lvAdapter = new ArrayAdapter<Player>(layout.getContext(), R.layout.players_list_item, tournament.getPlayers());
//				lv.setAdapter(lvAdapter);
//
//				lv.setOnItemClickListener(new OnItemClickListener(){
//
//
//					public void onItemClick(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
//
//						ListView lv = (ListView) layout.findViewById(R.id.playersListView);
//						selectedPlayerToRemove = (Player) lv.getItemAtPosition(arg2);
//					}
//
//				});
//
//
//
//				// Getting a reference to Close button, and close the popup when clicked.
//				Button close = (Button) layout.findViewById(R.id.close);
//				close.setOnClickListener(new OnClickListener() {
//
//					public void onClick(View v) {
//						popup.dismiss();
//					}
//				});
//
//				Button removePlayerBtn = (Button) layout.findViewById(R.id.removePlayerBtn);
//				removePlayerBtn.setOnClickListener(new OnClickListener(){
//
//					public void onClick(View v){
//
//						tournament.removePlayer(selectedPlayerToRemove);
//
//
//						refreshRoundsList();
//						popup.dismiss();
//
//
//
//					}
//				});
//
//
//
//			}
//
//		});
//
//		//MODIFY MATCHUPS BUTTON FUNCTIONALITY
//		final Button modifyMatchupsBtn = (Button) findViewById(R.id.modifyMatchupsBtn);
//		modifyMatchupsBtn.setOnClickListener(new OnClickListener(){
//
//			public void onClick(View v) {
//				int popupWidth = 500;
//				int popupHeight = 600;
//
//				// Inflate the scores_popup_layout.xml
//				RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.popup);
//				LayoutInflater layoutInflater = (LayoutInflater) MatchupsActivity.this
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				final View layout = layoutInflater.inflate(R.layout.modify_matchups_popup_layout, viewGroup);
//
//				// Creating the PopupWindow
//				final PopupWindow popup = new PopupWindow(MatchupsActivity.this);
//				popup.setContentView(layout);
//				popup.setWidth(popupWidth);
//				popup.setHeight(popupHeight);
//				popup.setFocusable(true);
//
//				// Clear the default translucent background
//				popup.setBackgroundDrawable(new BitmapDrawable());
//
//				// Displaying the popup at the specified location, + offsets
//				popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);
//
//				ListView lv = (ListView) layout.findViewById(R.id.modifyMatchupsListView);
//				lv.setChoiceMode(ListView.CHOICE_MODE_NONE);	
//
//				ModifyMatchupsListAdapter lvAdapter = new ModifyMatchupsListAdapter(layout.getContext(), R.layout.players_list_item, tournament.getBottomRound(), tournament.getPlayers(), lv);
//				lv.setAdapter(lvAdapter);
//
//				// Getting a reference to Close button, and close the popup when clicked.
//				Button close = (Button) layout.findViewById(R.id.close);
//				close.setOnClickListener(new OnClickListener() {
//
//					public void onClick(View v) {
//
//						//Make sure that the new set of matchups are valid.  If not, don't let user close this screen.
//						boolean b = SingleEliminationTournament.validateMatchups(tournament.getBottomRound());
//						if(b){
//							refreshRoundsList();    						
//							popup.dismiss();
//						}else{
//							TextView warning = (TextView) layout.findViewById(R.id.tvWarning);
//							warning.setText("Duplicate player or null vs. null!");
//						}
//
//					}
//				});
//
//
//
//			}
//
//		});
//
//		//START BUTTON FUNCTIONALITY
//		final Button startTournamentBtn = (Button) findViewById(R.id.startTournamentBtn);
//		startTournamentBtn.setOnClickListener(new OnClickListener(){
//
//			public void onClick(View v) {
//				int originalSize = tournament.getMatchups().size();
//				tournament.startTournament();				
//				refreshRoundsList(originalSize);
//				
//				addPlayersBtn.setEnabled(false);
//				modifyMatchupsBtn.setEnabled(false);
//				startTournamentBtn.setEnabled(false);
//				
//			}
//		});
//		
//		//STANDINGS BUTTON FUNCTIONALITY
//		Button standings = (Button) findViewById(R.id.matchup_to_standings);
//		standings.setOnClickListener(new OnClickListener(){
//
//			public void onClick(View v) {
//				//start standings
//				Intent i = getNewIntent(MatchupsActivity.this,SingleEliminationStandingsActivityDescriptive.class);
//				startActivity(i);
//			}
//		});
//		
//		//name of tournament
//		((TextView)findViewById(R.id.mathcups_name)).setText("Name: " + tournament.getTournamentName(true));
//
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		return true;
//	}
//
//
//	@Override
//	protected void onResume(){
//		super.onResume();
//		refreshRoundsList();
//		if(SingleEliminationStandingsActivityDescriptive.isPluginClosing){
//			finish();
//		}
//	}
//
//	/**
//	 * Refreshes the rounds list using the default size
//	 */
//	public void refreshRoundsList(){
//		refreshRoundsList(USE_DEFAULT_SIZE);
//	}
//
//	/**
//	 * Refreshes the ExpandableListView containing tournament rounds.
//	 */
//	public void refreshRoundsList(int size){
//		expListItems = setStandardGroups(tournament.getMatchups(), size);
//		expAdapter = new ExpandableRoundsListAdapter(MatchupsActivity.this, expListItems, this);
//		expandList.setAdapter(expAdapter);
//	}
//
//	/**
//	 * Creates ExpandableRoundsListGroups based on matchups
//	 * @param matchups the matchups of tournament; list is populated with their info
//	 * @return ArrayList of ExpandableRoundsListGroups created from matchups
//	 */
//	public ArrayList<ExpandableRoundsListGroup> setStandardGroups(ArrayList<Matchup> matchups, int size) {
//
////		int sizeForRoundCalculation = size;
////		if(size < 0){
////			sizeForRoundCalculation = matchups.size();
////		}
////
////		double doubleRounds = Math.log10(matchups.size()) / Math.log10(2);
////
////		int numRounds;
//
//		//truncate double to int. if not an exact number, add one to ensure enough rounds
////		if(doubleRounds % 1 != 0){
////			numRounds = (int)doubleRounds;
////			numRounds ++;
////		}else{
////			numRounds = (int)doubleRounds;
////		}
//		
//		ArrayList<ExpandableRoundsListGroup> toReturn = new ArrayList<ExpandableRoundsListGroup>();
//
//		Matchup finalRound = null;
//		for(Matchup m : matchups){
//			if(m.getParent() == null){
//				finalRound = m;
//				break;
//			}
//		}
//		
//		int numRounds = 1;
//		ArrayList<Matchup> children = finalRound.getChildren();
//		if(children.size() == 0){
//			children = null;
//		}
//		while(children != null){
//			numRounds ++;
//			
//			ArrayList<Matchup> childrenCopy = children;
//			children = null;
//			for(Matchup m : childrenCopy){
//				if(m.getChildren().size() > 0){
//					children = m.getChildren();
//				}
//			}
//		}
//		
//		ArrayList<Matchup> curRound = new ArrayList<Matchup>();
//		curRound.add(finalRound);
//		
//		boolean moreRounds = true;
//		int i = numRounds;
//		while(moreRounds){
//			
//			ArrayList<Matchup> nextRound = new ArrayList<Matchup>();
//			
//			ExpandableRoundsListGroup erlg = new ExpandableRoundsListGroup();
//			erlg.setName("Round " + i);
//
//			ArrayList<ExpandableRoundsListChild> erlc = new ArrayList<ExpandableRoundsListChild>();
//			
//			for(Matchup m : curRound){
//								
//				ExpandableRoundsListChild e = new ExpandableRoundsListChild();
//				e.setMatchup(m);
//				e.setName(m.printMatchup2());
//				e.setTag(null);
//				erlc.add(e);
//				
//				for(Matchup c : m.getChildren()){
//					nextRound.add(c);
//				}
//				
//				if(nextRound.size() == 0){
//					moreRounds = false;
//				}
//				
//			}
//			
//			erlg.setItems(erlc);
//			toReturn.add(erlg);
//
//			curRound = nextRound;
//			
//			i--;
//		}
//
//
//		return toReturn;
//
//	}
//	
//	public void setTimerText(String timeText){
//		TextView tv = (TextView)findViewById(R.id.tvRoundTimer);
//		tv.setText(timeText);
//	}
//	
//	/**
//	 * Goes to standings activity
//	 */
//	public void goToStandings()
//	{
//		Intent i = getNewIntent(MatchupsActivity.this, SingleEliminationStandingsActivityDescriptive.class);
//		i.putExtra("Final_Standings", true);
//		startActivity(i);
//	}
//	
//	/**
//	 * Goes to set scores activity
//	 */
//	public void goToSetScores(Matchup m){
//		Intent i = getNewIntent(MatchupsActivity.this, SetScoresActivity.class);
//		i.putExtra("matchup_id", m.getId());
//		startActivity(i);
//	}
//
//}
//
//
