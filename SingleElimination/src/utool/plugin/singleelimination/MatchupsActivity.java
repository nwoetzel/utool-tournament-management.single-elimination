package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import utool.networking.XmlMessageTypeException;
import utool.networking.packet.PlayerMessage;
import utool.networking.packet.PlayerMessage.MessageType;
import utool.networking.packet.PluginTerminationMessage;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginMainActivity;
import utool.plugin.singleelimination.communications.IncomingCommandHandlerHost;
import utool.plugin.singleelimination.communications.IncomingCommandHandlerParticipant;
import utool.plugin.singleelimination.communications.OutgoingCommandHandler;
import utool.plugin.singleelimination.communications.SaxFeedParser;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for displaying "Matchups" screen.  
 * @author hoguet
 * 
 * 10-14-12
 *
 */
public class MatchupsActivity extends AbstractPluginMainActivity implements GestureDetector.OnGestureListener {

	/**
	 * Tag used for logging
	 * @since 10/4/2012
	 */
	private static final String LOG_TAG = "utool.plugin.SingleEliminationMainActivity";


	/**
	 * A reference to this activity
	 */
	private MatchupsActivity activity = this;

	/**
	 * Runnable used by thread to receive messages
	 */
	Runnable receiveRunnable = new Runnable() 
	{
		public void run() 
		{
			try {
				while (true)
				{
					String msg = pluginHelper.mICore.receive();
					if (msg.equals("-1")){
						return;
					} else if (PluginTerminationMessage.isPluginTerminationMessage(msg)){
						terminatePluginInstance();
					}
					//determine which IncomingCOmmandHandler to attach to this plugin
					SaxFeedParser s;
					if(tournament.getPermissionLevel() == Player.HOST)
					{
						s = new SaxFeedParser(new IncomingCommandHandlerHost(tournament, activity));
					}
					else
					{
						s = new SaxFeedParser(new IncomingCommandHandlerParticipant(tournament, activity));

					}
					s.parse(msg);

					//If incoming message is caused by player connecting, notify host appropriately
					try{
						PlayerMessage message = new PlayerMessage(msg);
						
						if(message.getMessageType() == MessageType.PlayerRegister){
							//Add incoming player(s?) to tournament

							activity.runOnUiThread(new PlayerListRunnable(message.getPlayerList()){

								@Override
								public void run() {
									for(Player p : this.playerList){

										Toast t;
										Player connectedAs = retrieveIfContains(tournament.getPlayers(), p);
										if(connectedAs != null){
											t = Toast.makeText(activity, "Somebody connected as "+p.getName(), Toast.LENGTH_SHORT);
											connectedAs.setPermissionsLevel(Player.PARTICIPANT);
										}else{
											t = Toast.makeText(activity, p.getName()+" has connected.  To add them to the tournament, go to Edit Tournament.", Toast.LENGTH_SHORT);
											playersConnected.add(p.getUUID()); //save UUID so the permission can get set when/if they are added later
										}
										t.show();
									}
								}

							});

						}else if(message.getMessageType() == MessageType.PlayerList){
							Log.i("PLAYERMESSAGE", message.getXml());
						}

					}catch(XmlMessageTypeException e){
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Min distance to be recognized as a swipe
	 */
	private static final int SWIPE_MIN_DISTANCE = 120;

	/**
	 * Max distance to not be considered a swipe
	 */
	private static final int SWIPE_MAX_OFF_PATH = 250;

	/**
	 * Minimum velocity to be considered a swipe
	 */
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	/**
	 * Request code used when going to tournament config screen
	 */
	private static final int TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE = 6;

	/**
	 * Handler used for receiving messages from timer
	 */
	public Handler timerHandler;

	/**
	 * The player that is selected to be moved
	 */
	private static Player selectedPlayer;

	/**
	 * The label of player that is selected to be moved
	 */
	private static TextView selectedPlayerLabel;

	/**
	 * The matchup that selected player belongs to
	 */
	private static Matchup selectedMatchup;

	/**
	 * A list of pending players waiting to be added
	 */
	private static ArrayList<Player> playersToAdd;

	private static ArrayList<UUID> playersConnected = new ArrayList<UUID>();

	/**
	 * A reference to the "New Matchups" footer view which appears when adding players
	 */
	private View footerView;

	/**
	 * The tournament associated with this activity
	 */
	private SingleEliminationTournament tournament;

	/**
	 * ListView containing the matchups being displayed
	 */
	private ListView matchupsList;

	/**
	 * Save dialog as variable as it will be referenced throughout
	 */
	private RelativeLayout selectDialog;

	/**
	 * Dialog that appears when players are added.
	 */
	private RelativeLayout addDialog;

	/**
	 * GestureDetector used for detecting swipes to view different rounds
	 */
	private GestureDetector gestureDetector;

	/**
	 * The round of matchups being displayed currently
	 */
	private int displayedRound;

	/**
	 * Key for indicating whether screen has been accessed
	 */
	private static final String firstTimeKey = "utool.plugin.singleelimination.Matchups";


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		//Enable debugging for this plugin
//				android.os.Debug.waitForDebugger();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_matchups_new);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");

		//Get parameters through intent
		if(getIntent().getExtras()!=null)
		{
			Log.d(LOG_TAG, "List: "+getPlayerList());
		}
		else
		{
			//error to user and close plugin
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.create();
			alert.setMessage("Error initializing plugin. Incorrect extras provided.");
			alert.show();
			finish();
		}

		//set tournament name
		if(getTournamentName()==null)
		{
			setTournamentName("SE: "+ getTournamentId());
		}

		//Create tournament instances
		if (isNewInstance())
		{
			tournament = (SingleEliminationTournament)TournamentLogic.getInstance(getTournamentId(), getPlayerList(), null, getPermissionLevel());
			tournament.setPermissionLevel(getPermissionLevel());
			tournament.pid = getPid();
			tournament.setTournamentName(getTournamentName());

			//finish setting up playerlist with permissions
			if(getPermissionLevel() == Player.HOST)
			{
				for(int i=0;i<getPlayerList().size();i++)
				{
					if(getPlayerList().get(i).getUUID().equals(getPid()))
					{
						getPlayerList().get(i).setPermissionsLevel(Player.HOST);
					}
				}
			}

		} else {
			tournament = (SingleEliminationTournament)TournamentLogic.getInstance(getTournamentId());
		}		

		tournament.bridge.setMainActivity(this);

		//If host joins and no matchups are made, generate them.
		if (tournament.getMatchups().size() == 0 && getPermissionLevel() == Player.HOST){
			ArrayList<Matchup> matchups = SingleEliminationTournament.generateRandomMatchups(tournament.getPlayers(), tournament);
			tournament.setMatchups(matchups);
		}

		footerView = ((LayoutInflater) MatchupsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.matchup_list_footer, null, false);

		//initialize timer displays and add to it the timer button
		tournament.addTimerDisplay((Button) findViewById(R.id.mu_timerBtn));

		//Initialize timerhandler; will be used if timer is set.  TODO maybe do this just for host
		timerHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {

				String secondsString = ""+tournament.getSeconds();
				if(tournament.getSeconds() < 10){
					secondsString = "0"+tournament.getSeconds();
				}

				String timeString = tournament.getMinutes()+":"+secondsString;

				for(TextView tv : tournament.getTimerDisplays()){					
					setTimerText(timeString, tv); //this is the textview
				}

				//Notify participants of timer update
				tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tournament.getTournamentId(), timeString);

			}
		};

		//tournament should immediately be started when host reaches this screen
		if(!tournament.isStarted()){
			tournament.startTournament();
		}

		SharedPreferences prefs = this.getSharedPreferences("utool.plugin.singleelimination", Context.MODE_PRIVATE);

		// use a default value using new Date()
		Boolean firstTime= prefs.getBoolean(firstTimeKey, true); 
		if(firstTime)
		{
			//			this.setupHelpPopups();
			showHelp();

			//setup preferences to remember help has been played
			prefs.edit().putBoolean(firstTimeKey, false).commit();
		}

		//SELECT DIALOG
		selectDialog = (RelativeLayout) findViewById(R.id.frameLayout_matchups);
		Button selectCancel = (Button) findViewById(R.id.mu_cancel_swap);
		selectCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				deselectPlayer();				
			}
		});

		//ADD DIALOG
		addDialog = (RelativeLayout) findViewById(R.id.frameLayout_matchups_add);		


		//TOURNAMENT NAME BUTTON
		Button tournamentNameBtn = (Button) findViewById(R.id.mu_tournamentNameBtn);
		tournamentNameBtn.setText(tournament.getTournamentName(false)); //what param?
		tournamentNameBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				if(playersToAdd == null || playersToAdd.isEmpty()){
					//if there are outstanding playersToAdd, do not let user leave the screen.

					deselectPlayer();

					//go to standings screen
					Intent i = getNewIntent(MatchupsActivity.this,OverallStandingsActivity.class);
					startActivity(i);			
				}
			}			

		});

		//Only enable options list for host
		if(tournament.getPermissionLevel() == Player.HOST){
			tournamentNameBtn.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {

					boolean toReturn = homeTournamentsListView_onLongClick(view);

					//Re-grab tournament in case a new one was created via Restart
					if(toReturn){
						tournament = (SingleEliminationTournament)TournamentLogic.getInstance(tournament.getTournamentId());
					}

					return toReturn;
				}

			});
		}

		//ROUND BUTTON
		Button roundBtn = (Button) findViewById(R.id.mu_roundBtn);

		roundBtn.setText("Round "+tournament.getRound());

		roundBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				if(playersToAdd == null || playersToAdd.isEmpty()){

					deselectPlayer();
					goToRoundStandings(tournament.getRound());				
				}

			}

		});		

		//TIMER BUTTON
		Button timerBtn = (Button) findViewById(R.id.mu_timerBtn);

		//if round timer is set, display time, else display "set timer"
		String timeString = "Set Timer";
		if(tournament.getRoundDuration() == SingleEliminationTournament.CONFIRMED_NO_TIMER){
			timeString = "No Timer";

		}else if(tournament.getRoundDuration() != SingleEliminationTournament.DEFAULT_NO_TIMER){
			timeString = ""+ tournament.getMinutes() +":"+tournament.getSeconds();
		}

		timerBtn.setText(timeString);

		//only enable set timer button for host
		if(tournament.getPermissionLevel() == Player.HOST){
			timerBtn.setOnClickListener(new TimerButtonClickListener());
		}

		//FILL LIST WITH CUR ROUND
		displayedRound = tournament.getRound();
		refreshMatchupsList(tournament.getRound(displayedRound), null);

		matchupsList.setClickable(true);

		//ADD SET SCORES FUNCTIONALITY TO LIST ITEMS
		matchupsList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				//CLICK SHOULD ONLY RESPOND TO HOST OR MOD
				if(tournament.getPermissionLevel() == Player.HOST || tournament.getPermissionLevel() == Player.MODERATOR){

					//only go to set scores screen if not in middle of set scores action
					if(selectedPlayer == null && selectedPlayerLabel == null && (playersToAdd == null || playersToAdd.isEmpty())){

						TextView tag = (TextView)view.findViewById(R.id.mu_li_matchupTagLabel);
						long mid = Long.parseLong(tag.getText().toString());

						Matchup toGoTo = null;
						for(Matchup m : tournament.getMatchups()){
							if(m.getId() == mid){
								toGoTo = m;
								break;
							}
						}
						if(toGoTo.getPlayerOne() != null && toGoTo.getPlayerTwo() != null
								&& !toGoTo.getPlayerOne().getUUID().equals(Player.BYE) && !toGoTo.getPlayerTwo().getUUID().equals(Player.BYE)
								&& tournament.getCurrentRound().contains(toGoTo)
								&& !tournament.isFinished()){

							//go to set scores screen only if match in question has two non-null players,
							//and no BYEs, and the matchup belongs to the current round
							goToSetScores(toGoTo);
						}
					}

					//else do nothing
				}

			}
		});



		//Make layout swipeable
		gestureDetector = new GestureDetector(this, this);

		matchupsList.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent e) {
				gestureDetector.onTouchEvent(e);
				return false;
			}
		});

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


	@Override
	protected void onResume(){
		super.onResume();

		//refresh round button and tournament name button to reflect changes
		Button roundBtn = (Button) findViewById(R.id.mu_roundBtn);
		if(!tournament.isFinished()){
			roundBtn.setText("Round "+tournament.getRound());
		}else{
			roundBtn.setText("Done");
		}

		Button tournamentNameBtn = (Button) findViewById(R.id.mu_tournamentNameBtn);
		tournamentNameBtn.setText(tournament.getTournamentName(false)); //what param?

		refreshMatchupsList(tournament.getRound(displayedRound), null);
	}

	/**
	 * Reloads the matchups listview with the given matchups
	 * @param matchups matchups to populate listview with
	 * @param t expected to be null UNLESS new tournament was created via "Restart"
	 */
	public void refreshMatchupsList(ArrayList<Matchup> matchups, SingleEliminationTournament t){

		if(t != null){
			tournament = t;

			//refresh round button which may have changed if tournament was restarted.
			Button roundBtn = (Button) findViewById(R.id.mu_roundBtn);
			roundBtn.setText("Round "+tournament.getRound());

		}

		if(matchups == null){
			matchups = tournament.getCurrentRound();
		}


		runOnUiThread(new Runnable(){

			@Override
			public void run() {

				matchupsList = (ListView) findViewById(R.id.mu_matchupsList);

				SimpleAdapter adapter = initializeAdapter();
				adapter.setViewBinder(new MatchupsViewBinder());        

				matchupsList.setAdapter(adapter);

			}

		});

	}


	/**
	 * 
	 * @return the initialized SimpleAdapter with correct content for displayed round
	 */
	private SimpleAdapter initializeAdapter(){
		// create the grid item mapping
		String[] from = new String[] {
				"matchupTagLabel",
				"playerOneLabel", 
				"playerOneImage", 
				"playerOneScore", 
				"playerTwoLabel", 
				"playerTwoImage", 
				"playerTwoScore",
				"winnerLabel"
		};
		int[] to = new int[] { 
				R.id.mu_li_matchupTagLabel,
				R.id.mu_li_playerOneLabel, 
				R.id.mu_li_playerOneImage,
				R.id.mu_li_playerOneScore,
				R.id.mu_li_playerTwoLabel,
				R.id.mu_li_playerTwoImage,
				R.id.mu_li_playerTwoScore,
				R.id.mu_li_playerThreeLabel};

		//NOTE this makes the matchups param worthless
		ArrayList<Matchup> matchups;				

		ArrayList<Matchup> allMatchups = tournament.getMatchups();
		if(!allMatchups.isEmpty() && allMatchups.get(0).getRoundParticipant() > 0){
			matchups = tournament.getRoundParticipant(displayedRound);
		}else{
			matchups = tournament.getRound(displayedRound);
		}

		// prepare the list of all records
		List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
		for(Matchup m : matchups){

			String playerOneScoreString = null;
			String playerTwoScoreString = null;
			if(m.getScores() != null){						
				Double s1 = Double.valueOf(m.getScores()[0]);
				Double s2 = Double.valueOf(m.getScores()[1]);

				//truncate the decimal if it is 0
				playerOneScoreString = (s1.doubleValue() == s1.intValue()) ? Integer.toString(s1.intValue()) : Double.toString(s1.doubleValue());
				playerTwoScoreString = (s2.doubleValue() == s2.intValue()) ? Integer.toString(s2.intValue()) : Double.toString(s2.doubleValue());
			}

			Bitmap playerOneBmp = BitmapFactory.decodeResource(getResources(), R.drawable.silhouette);
			if(m.getPlayerOne() != null && m.getPlayerOne().getPortrait() != null){
				playerOneBmp = m.getPlayerOne().getPortrait();
			}

			Bitmap playerTwoBmp = BitmapFactory.decodeResource(getResources(), R.drawable.silhouette);
			if(m.getPlayerTwo() != null && m.getPlayerTwo().getPortrait() != null){
				playerTwoBmp = m.getPlayerTwo().getPortrait();
			}

			String winnerLabelString = "";
			if(m.getWinner() != null){
				winnerLabelString = m.getWinner().getName();
			}


			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("matchupTagLabel", Long.toString(m.getId()));
			map.put("playerOneLabel", m.getPlayerOne());
			map.put("playerOneImage", playerOneBmp);
			map.put("playerOneScore", playerOneScoreString);
			map.put("playerTwoLabel", m.getPlayerTwo());
			map.put("playerTwoImage", playerTwoBmp);
			map.put("playerTwoScore", playerTwoScoreString);
			map.put("winnerLabel", winnerLabelString);			
			fillMaps.add(map);
		}

		return new SimpleAdapter(activity, fillMaps, R.layout.matchup_list_item, from, to);
	}

	/**
	 * Updates the round button according to current round.
	 * Called when participant receives beginNewRound message
	 */
	public void updateRoundDisplay(){

		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Button roundBtn = (Button) findViewById(R.id.mu_roundBtn);
				if(!tournament.isFinished()){
					roundBtn.setText("Round "+tournament.getRound());
				}else{
					roundBtn.setText("Done");
				}				
			}			
		});		
	}
	
	/**
	 * Resets displayed round.
	 * Called when tournament is restarted so that UI doesnt try to retrieve from round that no longer exists.
	 */
	public void resetDisplayedRound(){
		displayedRound = 1;
	}

	/**
	 * Updates the tournament name button to the current tournament name.
	 * Called when participant receives sendTournamentName message
	 */
	public void updateTournamentName(){

		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Button tournamentNameBtn = (Button) findViewById(R.id.mu_tournamentNameBtn);
				tournamentNameBtn.setText(tournament.getTournamentName(false));
			}
		});
	}


	/**
	 * Updates timer text with new time
	 * @param timeText text to update with
	 * @param display the display to update with timetext
	 */	
	public void setTimerText(String timeText, TextView display){

		String timeString = "";
		if(tournament.getRoundDuration() != SingleEliminationTournament.DEFAULT_NO_TIMER){
			timeString = timeText;
		}

		display.setText(timeString);
	}

	/**
	 * Updates all timer displays with given string
	 * Used by participant instances
	 * @param timeText string to update displays with
	 */
	public void setTimerTextParticipant(String timeText){
		this.runOnUiThread(new StringRunnable(timeText) {
			public void run() {
				for(TextView tv: tournament.getTimerDisplays()){
					tv.setText(text);
				}
			}
		});		
	}

	/**
	 * 
	 * @return the Round Timer button
	 */
	public TextView getTimerDisplay(){
		return (Button) findViewById(R.id.mu_timerBtn);
	}

	/**
	 * OnLongClickListener for tournament
	 * @param view View
	 * @return Boolean
	 * @see OnLongClickListener
	 */
	private boolean homeTournamentsListView_onLongClick(View view) {
		final String details = "Details";
		final String edit = "Edit";
		final String options = "Options";
		final String terminate = "Terminate";
		final String restart = "Restart";

		List<String> menuItems = new LinkedList<String>();
		//Details
		menuItems.add(details);
		AlertDialog.Builder builder = new AlertDialog.Builder(MatchupsActivity.this);
		builder.setTitle(tournament.getTournamentName(false));

		menuItems.add(edit);
		menuItems.add(options);
		menuItems.add(terminate);
		menuItems.add(restart);

		String[] items = new String[0];
		items = menuItems.toArray(items);

		/**
		 * Inner class for handling the menu choice
		 */
		class dialogOnClick implements DialogInterface.OnClickListener {
			private String[] items;
			private SingleEliminationTournament tournament;

			public dialogOnClick(String[] items, SingleEliminationTournament tournament){
				this.items = items;
				this.tournament = tournament;
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String item = items[which];
				if (item.equals(details)){
					//Show advanced details
					AlertDialog.Builder d = new AlertDialog.Builder(MatchupsActivity.this);
					d.setTitle("Details");

					String statusString = "In Progress";
					if(tournament.isFinished()){
						statusString = "Finished";
					}

					String message = "Tournament Name: " + tournament.getTournamentName(false) + "\n\n"
							+ "Tournament UUID: " + tournament.getTournamentId() + "\n\n"
							+ "Status: " + statusString;
					//					if (tournament.getTournamentLocation() == TournamentLocationEnum.RemoteConnected || tournament.getTournamentLocation() == TournamentLocationEnum.RemoteDiscovered){
					//						message += "\n\n" + "Server: " + tournament.getServerAddress() + ":" + tournament.getServerPort();
					//					}
					d.setMessage(message);
					d.show();
				} else if (item.equals(edit)){
					//Go to tournament configuration					
					Intent i = new Intent();
					i.setAction("utool.core.intent.TOURNAMENT_CONFIG");
					i.putExtra("tournamentId", tournament.getTournamentId());
					startActivityForResult(i, TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE);
				} else if (item.equals(options)){
					goToOptions();
				} else if (item.equals(terminate)){
					tournament.endTournament();
					//TODO close miCore
					finish();
				} else if (item.equals(restart)){				

					//Restart the tournament by making new set of matchups and resetting rounds/timer
					ArrayList<Matchup> newMatchups = SingleEliminationTournament.generateRandomMatchups(tournament.getPlayers(), null);
					long tid = tournament.getTournamentId();
					String tname = tournament.getTournamentName(false);

					tournament.clearTournament(); //call this before clearing instance so that round timer gets closed properly
					TournamentLogic.clearInstance(tid);
					TournamentLogic newT = TournamentLogic.getInstance(tid, tournament.getPlayers(), newMatchups, tournament.getPermissionLevel());
					if(newT instanceof SingleEliminationTournament){

						tournament = (SingleEliminationTournament) newT;
						tournament.bridge.setMainActivity(activity);
						tournament.pid = activity.getPid();
						tournament.setTournamentName(tname);
						tournament.startTournament();

						//Reset timer display
						Button timerBtn = (Button) findViewById(R.id.mu_timerBtn);
						tournament.addTimerDisplay(timerBtn);			
						timerBtn.setText("Set Timer"); //this is the textview
						tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tournament.getTournamentId(), "Set Timer");

						//Refresh UI, defaulting to first round
						resetDisplayedRound();
						refreshMatchupsList(tournament.getCurrentRound(), tournament);

						//Update participants by clearing and resending players/matchups
						tournament.getOutgoingCommandHandler().handleSendClear(tid);						

						//send players 
						Player[] playersToSend = new Player[tournament.getPlayers().size()];
						for(int i = 0; i < playersToSend.length; i++){
							playersToSend[i] = tournament.getPlayers().get(i);
						}
						tournament.getOutgoingCommandHandler().handleSendPlayers(tid, playersToSend);

						//send matchups
						for(Matchup m : tournament.getMatchups()){
							String[] team1 = new String[1];
							String[] team2 = new String[1];

							team1[0] = "null";
							if(m.getPlayerOne() != null){
								team1[0] = m.getPlayerOne().getUUID().toString();
							}

							team2[0] = "null";
							if(m.getPlayerTwo() != null){
								team2[0] = m.getPlayerTwo().getUUID().toString();
							}

							tournament.getOutgoingCommandHandler().handleSendMatchup(tid, m.getId(), null, null, team1, team2, m.getRound(), null);
						}

					}else{
						Log.e("MatchupsActivity", "Something went horribly wrong when restarting tournament.");
					}

				}
			}

		}
		builder.setItems(items, new dialogOnClick(items, tournament));
		builder.show();
		return true;
	}



	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(data != null){
			Bundle b = data.getExtras();
			Object[] resultArray = (Object[])b.get("playerList");

			ArrayList<Player> newPlayersList = new ArrayList<Player>();
			for(Object o : resultArray){
				newPlayersList.add((Player)o);
			}

			//Don't allow players to be added to a finished tournament
			if(!tournament.isFinished()){
				compareAndResolvePlayers(newPlayersList);
			}

			if(!tournament.getTournamentName(false).equals((String)b.get("tournamentName"))){
				//Set tournament name in case it was changed
				tournament.setTournamentName((String)b.get("tournamentName"));
				Button tournamentNameBtn = (Button) findViewById(R.id.mu_tournamentNameBtn);
				tournamentNameBtn.setText(tournament.getTournamentName(false));

				//Notify participants
				tournament.getOutgoingCommandHandler().handleSendTournamentName(tournament.getTournamentId(), (String)b.get("tournamentName"));
			}


		}


	}

	/**
	 * Helper method that compares the tournament's player list to the newPlayersList and 
	 * updates the tournament player list to match the new one.
	 * @param newPlayersList
	 */
	public void compareAndResolvePlayers(ArrayList<Player> newPlayersList){

		ArrayList<Player> oldPlayersList = tournament.getPlayers();

		ArrayList<Player> playersToRemove = new ArrayList<Player>();
		//go through old players list; if any are missing in new list, remove them
		for(Player p : oldPlayersList){

			if(!removePlayerIfContains(newPlayersList, p)){
				playersToRemove.add(p);
			}

		}

		for(Player p : playersToRemove){
			tournament.removePlayer(p);
		}

		//now check any remaining new players that werent accounted for, and add them

		if(!newPlayersList.isEmpty()){

			addDialog.setVisibility(View.VISIBLE);
			matchupsList.addFooterView(footerView);			
			Button button = (Button) footerView.findViewById(R.id.mu_f_button);
			button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {

					Player playerToAdd = playersToAdd.remove(0);
					for(UUID id : playersConnected){
						if(playerToAdd.getUUID().equals(id)){
							playerToAdd.setPermissionsLevel(Player.PARTICIPANT);
							break;
						}
					}
					tournament.addPlayer(playerToAdd);

					tournament.expandBracket(playerToAdd);	

					refreshMatchupsList(tournament.getRound(displayedRound), null);

					if(playersToAdd.isEmpty()){
						addDialog.setVisibility(View.INVISIBLE);
						matchupsList.removeFooterView(footerView);
					}else{
						TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
						addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Click to place them.");	
					}					

				}

			});

			playersToAdd = newPlayersList;

			TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
			addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Click to place them.");

		}

		//If removal of players leads to no player vs. player matchups in current round, round should be resolved.
		//Don't resolve in round 1 because host may expect to be adding players to the empty slots or can restart the tournament.
		if(tournament.getRound() > 1){
			tournament.resolveCurrentRoundIfDone();
		}
	}

	/**
	 * Custom method checks list for player; if it exists, remove it from list and return true.
	 * Created because the standard ArrayList.contains and .remove  methods were not working.
	 * @param list to go through
	 * @param player to check list for
	 * @return true if player was removed, false if player was not in list
	 */
	private boolean removePlayerIfContains(ArrayList<Player> list, Player player){

		boolean toReturn = false;

		for(Player p : list){
			if(p.equals(player)){
				list.remove(p);
				toReturn = true;
				break;
			}
		}

		return toReturn;		
	}

	/**
	 * Helper method that deselects the currently selected player
	 */
	private void deselectPlayer(){
		if(selectedPlayer != null && selectedPlayerLabel != null && selectedMatchup != null){

			selectedPlayer = null;

			selectedPlayerLabel.setBackgroundColor(getResources().getColor(R.color.White));
			selectedPlayerLabel.setTextColor(getResources().getColor(R.color.Black));
			selectedPlayerLabel = null;					

			selectedMatchup = null;

			selectDialog.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Goes to standings activity
	 */
	public void goToStandings()
	{
		Intent i = getNewIntent(MatchupsActivity.this, OverallStandingsActivity.class);
		i.putExtra("Final_Standings", true);
		startActivity(i);
	}

	/**
	 * Goes to round standings activity
	 * @param round of standings to go to
	 */
	public void goToRoundStandings(int round){
		Intent i = getNewIntent(MatchupsActivity.this, RoundStandingsActivity.class);
		i.putExtra("Round", round);
		startActivity(i);
	}

	/**
	 * Goes to set scores activity
	 * @param m the matchup that scores are being set for
	 */
	public void goToSetScores(Matchup m){
		Intent i = getNewIntent(MatchupsActivity.this, SetScoresActivity.class);
		i.putExtra("matchup_id", m.getId());
		startActivity(i);
	}

	/**
	 * Goes to the options activity.
	 */
	public void goToOptions(){
		Intent i = getNewIntent(MatchupsActivity.this, SingleEliminationOptionsActivity.class);
		startActivity(i);
	}

	/**
	 * Displays the help screen
	 */
	public void showHelp(){
		final Dialog dialog = new Dialog(activity);
		dialog.setContentView(R.layout.matchup_help);
		dialog.setTitle("Matchups Help");
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
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		//Do not allow player to move to another round if a player is selected
		if(selectedPlayer != null || selectedPlayerLabel != null){
			return false;
		}


		if (e1 == null || e2 == null || Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) { 
			return false;  
		}

		/* positive value means right to left direction */
		final float distance = e1.getX() - e2.getX();
		final boolean enoughSpeed = Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY;
		if(distance > SWIPE_MIN_DISTANCE && enoughSpeed) {
			// right to left swipe
			displayedRound++;

			ArrayList<Matchup> newMatchupsList;
			if(!tournament.getMatchups().isEmpty() && tournament.getMatchups().get(0).getRoundParticipant() > 0){
				newMatchupsList = tournament.getRoundParticipant(displayedRound);
			}else{
				newMatchupsList = tournament.getRound(displayedRound);
			}

			if(newMatchupsList == null || newMatchupsList.isEmpty()){
				displayedRound--; //undo displayedRound increment because it went out of bounds
			}else{
				refreshMatchupsList(newMatchupsList, null);
			}

			return true;
		}  else if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
			// left to right swipe
			if(displayedRound > 1){
				displayedRound--;

				ArrayList<Matchup> newMatchupsList;
				if(!tournament.getMatchups().isEmpty() && tournament.getMatchups().get(0).getRoundParticipant() > 0){
					newMatchupsList = tournament.getRoundParticipant(displayedRound);
				}else{
					newMatchupsList = tournament.getRound(displayedRound);
				}

				refreshMatchupsList(newMatchupsList, null);
			}

			return true;
		} else {
			// it didn't qualify; do nothing
			return false;
		}

	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}

	@Override
	public void runOnServiceConnected() {		
		try {
			//create thread to get received messages
			Log.d(LOG_TAG, "Service connected, isNewInstance=" + isNewInstance());


			if (isNewInstance()){
				Thread t  = new Thread(receiveRunnable);
				t.start();

				// Host/Client specific code
				if (pluginHelper.mICore.isClient()){


					OutgoingCommandHandler och = new OutgoingCommandHandler(tournament);
					//					och.sendInitialRequest(tournament.getTournamentId());

					if(tournament.getMatchups().isEmpty()){
						och.handleSendError(tournament.getTournamentId(), "bob", "Test", "Error");
					}


				} else {
					//send tournament name
					OutgoingCommandHandler och = new OutgoingCommandHandler(tournament);
					och.handleSendTournamentName(tournament.getTournamentId(), tournament.getTournamentName(true));

					//send players
					ArrayList<Player> p = tournament.getPlayers();
					Player[] players = new Player[p.size()];
					for(int i=0;i<p.size();i++)
					{
						players[i] = p.get(i);
					}
					och.handleSendPlayers(tournament.getTournamentId(),players);

				}
			}

		} catch (RemoteException e) {
			Log.e(LOG_TAG, e.toString());
			Toast t = new Toast(MatchupsActivity.this);
			t.setText("Error connecting to core service");
			t.show();
		}

	}

	@Override
	public void runOnServiceDisconnected() {
		Log.e(LOG_TAG, "Service has unexpectedly disconnected");
		//If the service has disconnected, either the plugin is closing or the core has died. Do some cleanup.
		TournamentLogic.clearInstance(getTournamentId());
	}

	/**
	 * Terminate this instance of the plugin
	 */
	private void terminatePluginInstance(){
		TournamentLogic.clearInstance(getTournamentId());
		try {
			pluginHelper.mICore.close();
		} catch (Exception e) {
		}
		finish();
	}

	/**
	 * used for displaying toasts to the screen
	 */
	private String message="";

	/**
	 * This method is to be called when a message received from the server is to be displayed
	 * via a toast to the screen
	 * @param s String to display to the screen
	 */
	public void displayMessage(String s) 
	{
		message = s;
		this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(activity, "Msg: "+message, Toast.LENGTH_LONG).show();
			}
		});

	}

	/**
	 * Helper method that operates like list.contains(p) but returns the player from the list instead of a boolean
	 * @param list
	 * @param player
	 * @return the player from the list or null
	 */
	public static Player retrieveIfContains(List<Player> list, Player player){
		Player toReturn = null;
		for(Player p : list){
			if(p.getUUID().equals(player.getUUID())){
				toReturn = p;
				break;
			}
		}

		return toReturn;
	}

	/**
	 * Defines the on click behavior for the "Set Timer" button, which creates a popup with a number of buttons itself
	 * @author hoguet
	 *
	 */
	private class TimerButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View arg0) {

			//ON FIRST PRESS, POP UP SET TIMER LAYOUT
			int popupWidth = 400;
			int popupHeight = 400;

			// Inflate the scores_popup_layout.xml
			RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.popup);
			LayoutInflater layoutInflater = (LayoutInflater) MatchupsActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = layoutInflater.inflate(R.layout.set_timer_popup, viewGroup);

			// Creating the PopupWindow
			final PopupWindow popup = new PopupWindow(MatchupsActivity.this);
			popup.setContentView(layout);
			popup.setWidth(popupWidth);
			popup.setHeight(popupHeight);
			popup.setFocusable(true);

			// Displaying the popup at the specified location, + offsets
			popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);

			//Define save button (sets and starts round timer)
			Button saveBtn = (Button)layout.findViewById(R.id.st_saveBtn);
			saveBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {

					int roundDuration = SingleEliminationTournament.CONFIRMED_NO_TIMER;

					//If "no round timer" checkbox is not checked, grab round duration from input field
					CheckBox checkbox = (CheckBox)layout.findViewById(R.id.st_checkbox);
					if(!checkbox.isChecked()){			

						EditText timeField = (EditText)layout.findViewById(R.id.st_timeField);
						int minutes = Integer.parseInt(timeField.getText().toString());
						roundDuration = minutes * 60;

					}							
					tournament.setRoundTimer(roundDuration, timerHandler);

					if(roundDuration == SingleEliminationTournament.CONFIRMED_NO_TIMER){
						for(TextView tv : tournament.getTimerDisplays()){					
							setTimerText("No Timer", tv); //this is the textview
						}
						tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tournament.getTournamentId(), "No Timer");
					}else{
						tournament.startRoundTimer();
					}

					popup.dismiss();

				}						
			});

			//Define cancel button
			Button cancelBtn = (Button)layout.findViewById(R.id.st_cancelBtn);
			cancelBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {							
					popup.dismiss();
				}						
			});
		}
	}

	/**
	 * A runnable class that takes in a String argument.
	 * @author hoguet
	 *
	 */
	private class StringRunnable implements Runnable{

		/**
		 * The string associated with this runnable
		 */
		String text;

		/**
		 * @param text the string to attach to this runnable
		 */
		public StringRunnable(String text){
			this.text=text;
		}

		@Override
		public void run() {
			//intended to be overridden in-line when instantiated
		}
	}

	/**
	 * A runnable class that takes in a List<Player> argument.
	 * @author hoguet
	 *
	 */
	private class PlayerListRunnable implements Runnable{

		/**
		 * The player list associated with this runnable
		 */
		List<Player> playerList;

		/**
		 * @param playerList the player list to attach to this runnable
		 */
		public PlayerListRunnable(List<Player> playerList){
			this.playerList=playerList;
		}

		@Override
		public void run() {
			//intended to be overridden in-line when instantiated
		}
	}

	/**
	 * ViewBinder for the SimpleAdapter that defines on-click and on-long-click behaviors for elements in the main ListView
	 * @author hoguet
	 *
	 */
	private class MatchupsViewBinder implements SimpleAdapter.ViewBinder{

		@Override
		public boolean setViewValue(View view, Object data, String textRepresentation){
			//Custom handle the player label loadings so that they are long clckable
			if (view.getId() == R.id.mu_li_playerOneLabel || view.getId() == R.id.mu_li_playerTwoLabel) {

				TextView playerLabel = (TextView) view;
				Player player = (Player)data;

				//Set text based on status of player
				String playerLabelString = "";
				if(player != null && player.equals(Player.BYE)){
					playerLabelString = "BYE";
				}else if(player != null){
					playerLabelString = player.getName();
				}     

				//determine the matchup the player belongs to
				RelativeLayout parent = (RelativeLayout)playerLabel.getParent();
				TextView matchupTag = (TextView)parent.findViewById(R.id.mu_li_matchupTagLabel);
				long mid = Long.parseLong(matchupTag.getText().toString());

				Matchup matchup = null;
				for(Matchup m : tournament.getMatchups()){
					if(mid == m.getId()){
						matchup = m;
						break;
					}
				}

				playerLabel.setText(playerLabelString);

				//Bold the winner's name and score
				if(matchup != null && matchup.getWinner() != null && matchup.getWinner().equals(player)){
					playerLabel.setTypeface(null, Typeface.BOLD);
					if(playerLabel.getId() == R.id.mu_li_playerOneLabel){
						TextView scoreLabel = (TextView)parent.findViewById(R.id.mu_li_playerOneScore);
						scoreLabel.setTypeface(null, Typeface.BOLD);
					}else{
						TextView scoreLabel = (TextView)parent.findViewById(R.id.mu_li_playerTwoScore);
						scoreLabel.setTypeface(null, Typeface.BOLD);
					}
				}else{
					playerLabel.setTypeface(null, Typeface.NORMAL);
					if(playerLabel.getId() == R.id.mu_li_playerOneLabel){
						TextView scoreLabel = (TextView)parent.findViewById(R.id.mu_li_playerOneScore);
						scoreLabel.setTypeface(null, Typeface.NORMAL);
					}else{
						TextView scoreLabel = (TextView)parent.findViewById(R.id.mu_li_playerTwoScore);
						scoreLabel.setTypeface(null, Typeface.NORMAL);
					}
				}


				//if player is winner & active player, italicize the winner label
				//				if(player.getUUID().equals(activity.getPid())){
				//					
				//					TextView winnerLabel = (TextView)parent.findViewById(R.id.mu_li_playerThreeLabel);
				//					winnerLabel.setTypeface(null, Typeface.BOLD_ITALIC);
				//				}

				//Italicize the active player
				if(player != null && activity.getPid().equals(player.getUUID())){
					playerLabel.setTextColor(Color.BLUE);
				}else{
					playerLabel.setTextColor(Color.BLACK);
				}

				//Only enable modify matchups for host
				if(tournament.getPermissionLevel() == Player.HOST){
					playerLabel.setOnLongClickListener(new PlayerLongClickListener(player, matchup){

						@Override
						public boolean onLongClick(View v) {

							//only select player if matchup not already finished.
							if(getMatchup().getWinner() == null){

								//before selecting this longclicked player, deselect old selection if there was one (check is done by method)
								deselectPlayer();

								selectDialog.setVisibility(View.VISIBLE);
								TextView selectDialogText = (TextView) findViewById(R.id.helpText);
								selectDialogText.setText("Click on another player to swap them.");

								selectedPlayer = getPlayer();
								selectedMatchup = getMatchup();
								selectedPlayerLabel = (TextView)v;

								selectedPlayerLabel.setBackgroundColor(getResources().getColor(R.color.Blue));
								selectedPlayerLabel.setTextColor(getResources().getColor(R.color.White));

							}

							return true;
						}

					});
				}

				//Only host or moderator will have use for this listener (used for completing a modify matchups/add player operation or going to set scores screen)
				playerLabel.setOnClickListener(new PlayerClickListener(player, matchup){

					@Override
					public void onClick(View v) {

						if(tournament.getPermissionLevel() == Player.HOST || tournament.getPermissionLevel() == Player.MODERATOR){

							//if selected, perform modify matchups operation
							if(selectedPlayer != null && selectedPlayerLabel != null){

								if(getMatchup().getWinner() != null){
									TextView selectDialogText = (TextView) findViewById(R.id.helpText);
									selectDialogText.setText("Cannot swap with player from a finished matchup.");
								}else if(!getPlayer().equals(selectedPlayer)){

									Player checkOne = selectedMatchup.swapPlayer(selectedPlayer, getPlayer());
									Player checkTwo = getMatchup().swapPlayer(getPlayer(), selectedPlayer);

									if(checkOne == null || checkTwo == null){
										Log.i("MatchupsActivity", "Error swapping players.");
									}

									//If either modified matchup has children, handle them.
									if(!selectedMatchup.getChildren().isEmpty() || !getMatchup().getChildren().isEmpty()){

										ArrayList<Matchup> childrenToSwap = getMatchup().getChildren();

										getMatchup().setChildren(selectedMatchup.getChildren());
										selectedMatchup.setChildren(childrenToSwap);

									}

									//After modifying matchups, deselect the player and refresh list
									deselectPlayer();
									refreshMatchupsList(tournament.getRound(displayedRound), null);

								}


							}else if(playersToAdd != null && !playersToAdd.isEmpty()){

								Player old = getPlayer();								

								//only add player to selected spot if player in spot's id is 'BYE' else this is invalid
								if(old.getUUID().equals(Player.BYE)){

									//grab top player from playersToAdd list
									Player playerToAdd = playersToAdd.remove(0);

									for(UUID id : playersConnected){
										if(playerToAdd.getUUID().equals(id)){
											playerToAdd.setPermissionsLevel(Player.PARTICIPANT);
											break;
										}
									}
									tournament.addPlayer(playerToAdd);					
									getMatchup().swapPlayer(old, playerToAdd);
									playerToAdd = null;

									refreshMatchupsList(tournament.getRound(displayedRound), null);

									//if there are more players, change the display label else hide add dialog
									if(playersToAdd.isEmpty()){
										addDialog.setVisibility(View.INVISIBLE);
										matchupsList.removeFooterView(footerView);
									}else{
										TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
										addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Click to place them.");	
									}

								}else{
									//change dialog message; invalid target
									TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
									addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Invalid target.");
								}

							}else{

								//go to the set scores screen
								if(getMatchup().getPlayerOne() != null && getMatchup().getPlayerTwo() != null
										&& !getMatchup().getPlayerOne().getUUID().equals(Player.BYE) && !getMatchup().getPlayerTwo().getUUID().equals(Player.BYE)
										&& tournament.getRound(tournament.getRound()).contains(getMatchup())){
									goToSetScores(getMatchup());
								}						

							}
						}

					}

				});



				return true; 
			}
			return false;
		}

	}

}
