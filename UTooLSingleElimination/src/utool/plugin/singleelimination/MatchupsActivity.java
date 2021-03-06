package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for displaying "Matchups" screen.  
 * @author hoguet
 * 
 * 4-21-13
 *
 */
public class MatchupsActivity extends AbstractPluginMainActivity implements OnGestureListener {

	/**
	 * Tag used for logging
	 * @since 10/4/2012
	 */
	private static final String LOG_TAG = "utool.plugin.SingleEliminationMainActivity";

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
						s = new SaxFeedParser(new IncomingCommandHandlerHost(tournament, tournament.bridge.getMainActivity()));
					}
					else
					{
						s = new SaxFeedParser(new IncomingCommandHandlerParticipant(tournament, tournament.bridge.getMainActivity()));

					}
					s.parse(msg);

					//If incoming message is caused by player connecting, notify host appropriately
					try{
						PlayerMessage message = new PlayerMessage(msg);

						if(message.getMessageType() == MessageType.PlayerRegister){
							//Add incoming player(s?) to tournament

							tournament.bridge.getMainActivity().runOnUiThread(new PlayerListRunnable(message.getPlayerList()){

								@Override
								public void run() {
									for(Player p : this.playerList){

										Toast t;
										Player connectedAs = retrieveIfContains(tournament.getPlayers(), p);
										if(connectedAs != null){
											t = Toast.makeText(tournament.bridge.getMainActivity(), "Somebody connected as "+p.getName(), Toast.LENGTH_SHORT);
											connectedAs.setPermissionsLevel(Player.PARTICIPANT);
										}else{
											t = Toast.makeText(tournament.bridge.getMainActivity(), p.getName()+" has connected.  To add them to the tournament, go to Menu -> Edit.", Toast.LENGTH_SHORT);
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
	private static List<Player> playersToAdd;

	/**
	 * A list containing the UUIDs of players that connected.  Used for detecting possible candidates for Moderator
	 */
	private static List<UUID> playersConnected = new ArrayList<UUID>();

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
	 * The adapter for matchupsList
	 */
	private MatchupsListAdapter matchupsListAdapter;

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


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		//Enable debugging for this plugin
//		android.os.Debug.waitForDebugger();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_matchups_new);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");

		//Get parameters through intent
		if(getIntent().getExtras()==null)
		{
			//error to user and close plugin
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.create();
			alert.setMessage("Error initializing plugin. Incorrect extras provided.");
			alert.show();
			finish();
		}



		//check for added players
		if(!isNewInstance() && tournament != null && !tournament.isFinished()){
			compareAndResolvePlayers(getPlayerList());
		}

	}

	/**
	 * Initialize elements of the activity
	 * Called by onServiceConnected because pluginHelper isn't ready at time of onCreate
	 */
	private void createStuff(){
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

		try
		{
			tournament.bridge.setMainActivity(this);
		}
		catch(Exception e)
		{
			//this keeps throwing null pointer exception on resume
			Log.e("SingleElimination Matchups","ERROR:",e);
		}

		//If host joins and no matchups are made, generate them.
		if (tournament.getMatchups().size() == 0 && getPermissionLevel() == Player.HOST){
			ArrayList<Matchup> matchups = SingleEliminationTournament.generateRandomMatchups(tournament.getPlayers(), tournament);
			tournament.setMatchups(matchups);
		}

		footerView = ((LayoutInflater) MatchupsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.matchup_list_footer, null, false);	

		//initialize timer displays and add to it the timer button
		tournament.addTimerDisplay((Button) findViewById(R.id.mu_timerBtn));

		//Initialize timerhandler; will be used if timer is set.
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

					goToStandings();
				}
			}			

		});

		//Only enable options list for host
		if(tournament.getPermissionLevel() == Player.HOST){
			tournamentNameBtn.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {

					if(playersToAdd == null || playersToAdd.isEmpty()){

						boolean toReturn = homeTournamentsListView_onLongClick(view);

						//Re-grab tournament in case a new one was created via Restart
						if(toReturn){
							tournament = (SingleEliminationTournament)TournamentLogic.getInstance(tournament.getTournamentId());
						}

						return toReturn;
					}else{
						return false;
					}
				}

			});
		}

		//ROUND BUTTON
		Button roundBtn = (Button) findViewById(R.id.mu_roundBtn);

		roundBtn.setText("Round "+displayedRound);

		roundBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				if(playersToAdd == null || playersToAdd.isEmpty()){

					deselectPlayer();
					goToRoundStandings(displayedRound);				
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

		matchupsList = (ListView) findViewById(R.id.mu_matchupsList);
		matchupsList.addFooterView(footerView);
		ArrayList<Matchup> curRound = (getPermissionLevel() == Player.HOST) ? tournament.getRound(displayedRound) : tournament.getRoundParticipant(displayedRound);
		matchupsListAdapter = new MatchupsListAdapter(this, R.id.mu_matchupsList, curRound);
		matchupsList.setAdapter(matchupsListAdapter);
		matchupsList.removeFooterView(footerView);

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

		if(playersToAdd == null || playersToAdd.isEmpty()){
			MenuInflater inflater = getMenuInflater();
			if(getPermissionLevel() == Player.HOST){
				inflater.inflate(R.menu.matchups_menu, menu);
			}else{ //Non-host's menu should only show Help option
				inflater.inflate(R.menu.round_standings_menu, menu);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			showHelp();
			break;
		case R.id.edit:
			handleEdit();
			break;
		case R.id.options:
			goToOptions();
			break;
		case R.id.restart:
			handleRestart();
			break;
		case R.id.terminate:
			handleTerminate();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onResume(){
		super.onResume();

		if(tournament != null){
			tournament.bridge.setMainActivity(this);
			//refresh round button and tournament name button to reflect changes
			updateRoundDisplay();

			Button tournamentNameBtn = (Button) findViewById(R.id.mu_tournamentNameBtn);
			tournamentNameBtn.setText(tournament.getTournamentName(false)); //what param?

			refreshMatchupsList(null);
		}
	}

	/**
	 * Reloads the matchups listview with the given matchups
	 * @param t expected to be null UNLESS new tournament was created via "Restart"
	 */
	public void refreshMatchupsList(SingleEliminationTournament t){

		if(t != null){
			tournament = t;

			//refresh round button which may have changed if tournament was restarted.
			updateRoundDisplay();
		}

		runOnUiThread(new Runnable(){

			@Override
			public void run() {				
				ArrayList<Matchup> allMatchups = tournament.getMatchups();
				ArrayList<Matchup> matchups;
				if(!allMatchups.isEmpty() && getPermissionLevel() != Player.HOST){
					matchups = tournament.getRoundParticipant(displayedRound);
				}else{
					matchups = tournament.getRound(displayedRound);
				}

				//Sort matchups-to-be-displayed by id so that they appear in a consistent order
				Collections.sort(matchups, new Comparator<Matchup>(){
					public int compare(Matchup a, Matchup b){
						if(a.getId() < b.getId()) return -1;
						if(a.getId() > b.getId()) return 1;
						else return 0;
					}
				});

				matchupsListAdapter.setMatchups(matchups);
				matchupsListAdapter.notifyDataSetChanged();
			}
		});
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
				roundBtn.setText("Round "+displayedRound);
			}			
		});		
	}

	/**
	 * Resets displayed round.
	 * Called when tournament is restarted so that UI doesnt try to retrieve from round that no longer exists.
	 */
	public void resetDisplayedRound(){
		displayedRound = 1;
		updateRoundDisplay();
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
					d.setMessage(message);
					d.show();
				} else if (item.equals(edit)){
					handleEdit();
				} else if (item.equals(options)){
					goToOptions();
				} else if (item.equals(terminate)){
					handleTerminate();
				} else if (item.equals(restart)){				
					handleRestart();
				}
			}

		}
		builder.setItems(items, new dialogOnClick(items, tournament));
		builder.show();
		return true;
	}

	/**
	 * Goes to tournament configuration screen
	 */
	private void handleEdit(){
		//Go to tournament configuration					
		Intent i = new Intent();
		i.setAction("utool.core.intent.TOURNAMENT_CONFIG");
		i.putExtra("tournamentId", tournament.getTournamentId());
		startActivityForResult(i, TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE);
	}

	/**
	 * Terminates the tournament
	 */
	private void handleTerminate(){
		tournament.clearTournament();
		TournamentLogic.clearInstance(tournament.getTournamentId());
		try{
			pluginHelper.mICore.close();
		}catch(Exception e){
		}
		finish();
	}

	/**
	 * Restarts the tournament; updates UI accordingly.
	 */
	private void handleRestart(){
		//Restart the tournament by making new set of matchups and resetting rounds/timer
		List<Player> players = tournament.getPlayers();
		long tid = tournament.getTournamentId();
		String tname = tournament.getTournamentName(false);

		UUID pid = tournament.pid;
		tournament.clearTournament(); //call this before clearing instance so that round timer gets closed properly
		TournamentLogic.clearInstance(tid);
		TournamentLogic newT = TournamentLogic.getInstance(tid, players, null, tournament.getPermissionLevel());
		if(newT instanceof SingleEliminationTournament){

			tournament = (SingleEliminationTournament) newT;
			tournament.setMatchups(SingleEliminationTournament.generateRandomMatchups(players, tournament));
			tournament.bridge.setMainActivity(this);
			tournament.pid = pid;
			tournament.setTournamentName(tname);
			tournament.startTournament();

			//Reset timer display
			Button timerBtn = (Button) findViewById(R.id.mu_timerBtn);
			tournament.addTimerDisplay(timerBtn);			
			timerBtn.setText("Set Timer"); //this is the textview
			tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tournament.getTournamentId(), "Set Timer");

			//Refresh UI, defaulting to first round
			resetDisplayedRound();
			refreshMatchupsList(tournament);

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
	public void compareAndResolvePlayers(List<Player> newPlayersList){

		List<Player> oldPlayersList = tournament.getPlayers();

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

					refreshMatchupsList(null);

					if(playersToAdd.isEmpty()){
						addDialog.setVisibility(View.INVISIBLE);
						matchupsList.removeFooterView(footerView);
					}else{
						TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
						addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Tap to place them.");	
					}					

				}

			});

			playersToAdd = newPlayersList;

			TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
			addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Tap to place them.");

		}

		//If removal of players leads to no player vs. player matchups in current round, round should be resolved.
		//Don't resolve in round 1 because host may expect to be adding players to the empty slots or can restart the tournament.
		if(!playersToRemove.isEmpty() && tournament.getRound() > 1){
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
	private boolean removePlayerIfContains(List<Player> list, Player player){

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
		i.putExtra("ActivePid", getPid().toString());
		startActivity(i);
	}

	/**
	 * Goes to round standings activity
	 * @param round of standings to go to
	 */
	public void goToRoundStandings(int round){
		Intent i = getNewIntent(MatchupsActivity.this, RoundStandingsActivity.class);
		i.putExtra("Round", round);
		i.putExtra("ActivePid", getPid().toString());
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
		final Dialog dialog = new Dialog(this);
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
				updateRoundDisplay();
				refreshMatchupsList(null);
			}

			return true;
		}  else if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
			// left to right swipe
			if(displayedRound > 1){
				displayedRound--;
				updateRoundDisplay();
				refreshMatchupsList(null);
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

			createStuff();
			onResume();

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
					List<Player> p = tournament.getPlayers();
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
				Toast.makeText(MatchupsActivity.this, "Msg: "+message, Toast.LENGTH_LONG).show();
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
				p.setPortrait(player.getPortrait());
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

			final Dialog dialog = new Dialog(MatchupsActivity.this);
			dialog.setContentView(R.layout.set_timer_popup);
			dialog.setTitle("Set Timer");
			dialog.setCancelable(true);

			//Define save button (sets and starts round timer)
			Button saveBtn = (Button)dialog.findViewById(R.id.st_saveBtn);
			saveBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {

					int roundDuration = SingleEliminationTournament.CONFIRMED_NO_TIMER;

					//If "no round timer" checkbox is not checked, grab round duration from input field
					CheckBox checkbox = (CheckBox)dialog.findViewById(R.id.st_checkbox);
					if(!checkbox.isChecked()){			

						EditText timeField = (EditText)dialog.findViewById(R.id.st_timeField);
						try{
							int minutes = Integer.parseInt(timeField.getText().toString());
							roundDuration = minutes * 60;
						} catch (Exception e){
							roundDuration = 0;
						}

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

					dialog.dismiss();

				}						
			});

			//Define cancel button
			Button cancelBtn = (Button)dialog.findViewById(R.id.st_cancelBtn);
			cancelBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {							
					dialog.dismiss();
				}						
			});


			dialog.show();
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
	 * Adapter used for populating the main matchups listview
	 * @author hoguet
	 *
	 */
	private class MatchupsListAdapter extends ArrayAdapter<Matchup>{

		/**
		 * The matchups associated with this adapter
		 */
		private ArrayList<Matchup> matchups;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param matchups the matchups OF THE ROUND TO BE DISPLAYED
		 */
		public MatchupsListAdapter(Context context, int textViewResourceId, ArrayList<Matchup> matchups)
		{
			super(context, textViewResourceId, matchups);
			this.matchups = matchups;
		}

		/**
		 * @param matchups OF THE ROUND TO BE DISPLAYED
		 */
		public void setMatchups(ArrayList<Matchup> matchups){
			this.matchups.clear();
			this.matchups.addAll(matchups);
		}

		@Override
		public int getCount()
		{
			return (matchups == null) ? 0 : matchups.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			
			View row = convertView;
			if(row == null){			
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.matchup_list_item, parent, false);
			}
			

			Matchup m = matchups.get(position);			
			
			//MATCHUP TAG LABEL
			TextView matchupTag = (TextView)row.findViewById(R.id.mu_li_matchupTagLabel);
			matchupTag.setText(Long.toString(m.getId()));

			//PLAYER SCORE LABELS
			TextView playerOneScoreLabel = (TextView)row.findViewById(R.id.mu_li_playerOneScore);
			TextView playerTwoScoreLabel = (TextView)row.findViewById(R.id.mu_li_playerTwoScore);

			String playerOneScoreString = "";
			String playerTwoScoreString = "";
			if(m.getScores() != null){						
				Double s1 = Double.valueOf(m.getScores()[0]);
				Double s2 = Double.valueOf(m.getScores()[1]);

				//truncate the decimal if it is 0
				playerOneScoreString = (s1.doubleValue() == s1.intValue()) ? Integer.toString(s1.intValue()) : Double.toString(s1.doubleValue());
				playerTwoScoreString = (s2.doubleValue() == s2.intValue()) ? Integer.toString(s2.intValue()) : Double.toString(s2.doubleValue());
			}

			playerOneScoreLabel.setText(playerOneScoreString);
			playerTwoScoreLabel.setText(playerTwoScoreString);		

			//PLAYER PORTRAITS
			ImageView playerOnePortrait = (ImageView)row.findViewById(R.id.mu_li_playerOneImage);
			Bitmap playerOneBmp = BitmapFactory.decodeResource(getResources(), R.drawable.silhouette);
			if(m.getPlayerOne() != null && m.getPlayerOne().getPortrait() != null){
				playerOneBmp = m.getPlayerOne().getPortrait();
			}
			playerOnePortrait.setImageBitmap(playerOneBmp);

			ImageView playerTwoPortrait = (ImageView)row.findViewById(R.id.mu_li_playerTwoImage);
			Bitmap playerTwoBmp = BitmapFactory.decodeResource(getResources(), R.drawable.silhouette);
			if(m.getPlayerTwo() != null && m.getPlayerTwo().getPortrait() != null){
				playerTwoBmp = m.getPlayerTwo().getPortrait();
			}
			playerTwoPortrait.setImageBitmap(playerTwoBmp);

			//WINNER LABEL
			TextView winnerLabel = (TextView)row.findViewById(R.id.mu_li_playerThreeLabel);
			String winnerLabelString = (m.getWinner() == null) ? "" : m.getWinner().getName();
			winnerLabel.setText(winnerLabelString);

			//PLAYER ONE LABEL
			TextView playerOneLabel = (TextView)row.findViewById(R.id.mu_li_playerOneLabel);
			initializePlayerLabel(playerOneLabel, m.getPlayerOne(), m);

			//PLAYER TWO LABEL
			TextView playerTwoLabel = (TextView)row.findViewById(R.id.mu_li_playerTwoLabel);
			initializePlayerLabel(playerTwoLabel, m.getPlayerTwo(), m);

			return row;
		}

		/**
		 * Helper method which assigns player names to their labels and defines click listeners.
		 * @param playerLabel label to work with
		 * @param player to assign to the label
		 * @param matchup that player belongs to
		 */
		private void initializePlayerLabel(TextView playerLabel, Player player, Matchup matchup){
			String playerLabelString = "";
			if(player != null && player.equals(Player.BYE)){
				playerLabelString = "BYE";
			}else if(player != null){
				playerLabelString = player.getName();
			}
			playerLabel.setText(playerLabelString);

			RelativeLayout parent = (RelativeLayout)playerLabel.getParent();
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

			//Italicize the active player
			if(player != null && tournament.pid.equals(player.getUUID())){
				playerLabel.setTextColor(Color.BLUE);
			}else{
				playerLabel.setTextColor(Color.BLACK);
			}

			//Only enable modify matchups for host
			if(tournament.getPermissionLevel() == Player.HOST){
				playerLabel.setOnLongClickListener(new PlayerLongClickListener(player, matchup){

					@Override
					public boolean onLongClick(View v) {

						//only select player if matchup not already finished and not in middle of adding player.
						if(getMatchup().getWinner() == null && (playersToAdd == null || playersToAdd.isEmpty())){

							//before selecting this longclicked player, deselect old selection if there was one (check is done by method)
							deselectPlayer();

							selectDialog.setVisibility(View.VISIBLE);
							TextView selectDialogText = (TextView) findViewById(R.id.helpText);
							selectDialogText.setText("Tap on another player to swap them.");

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
								refreshMatchupsList(null);

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

								refreshMatchupsList(null);

								//if there are more players, change the display label else hide add dialog
								if(playersToAdd.isEmpty()){
									addDialog.setVisibility(View.INVISIBLE);
									matchupsList.removeFooterView(footerView);
								}else{
									TextView addLabel = (TextView) findViewById(R.id.add_dialog_label);
									addLabel.setText("New Player: "+playersToAdd.get(0).getName()+" Tap to place them.");	
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
									&& tournament.getCurrentRound().contains(getMatchup())){
								goToSetScores(getMatchup());
							}						

						}
					}

				}

			});
		}
	}

}
