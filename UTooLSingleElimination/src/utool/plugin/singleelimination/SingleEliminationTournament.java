package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import java.util.Timer;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import utool.plugin.Player;

/**
 * Data class that represents a Single Elimination Tournament
 * 
 * @author hoguet
 *
 * Date: 10-4-2012
 */
public class SingleEliminationTournament extends TournamentLogic{

	/**
	 * The default score given to a player who wins by default (bye round or forfeit)
	 */
	public static final int DEFAULT_DEFAULT_WIN = 2;

	/**
	 * The default round timer setting; no timer
	 */
	public static final int DEFAULT_NO_TIMER = -1; 

	/**
	 * Indicates player confirmed no timer
	 */
	public static final int CONFIRMED_NO_TIMER = -2;
	
	/**
	 * Counter used for generating unique matchup IDs.
	 */
	private static int matchIdCounter = 0;

	/**
	 * The score given to a player who wins by default (bye round or forfeit)
	 */
	private int defaultWin;

	/**
	 * The duration of a round in seconds
	 */
	private int roundDuration;

	/**
	 * The round timer
	 */
	private Timer roundTimer;

	/**
	 * The seconds left in the current round
	 */
	private int remainingSeconds;
	
	/**
	 * The handler to be notified when round timer is updated.  Is passed in when round timer is set
	 */
	private Handler timerHandler;

	/**
	 * Indicates whether or not the timer is paused.
	 */
	private boolean timerPaused = false;

	/**
	 * List of matchups in the tournament
	 */
	private ArrayList<Matchup> matchups;

	/**
	 * Signifies if tournament has started or not
	 */
	private boolean started;

	/**
	 * Signifies if a tournament is finished
	 */
	private boolean finished;

	/**
	 * Is the current round of the tournament
	 */
	private int round;
	
	/**
	 * A list of text views that should be updated with time
	 */
	private ArrayList<TextView> timerDisplays;

	/**
	 * Constructor
	 * Note: all matchups passed in will have their tournament set to this tournament
	 * 
	 * Since no players list is given,
	 * the players list will be initialized to those found in matchups and can be added/removed after construction
	 * 
	 * @param tournamentId The tournament id all the way from the Core
	 * @param matchups  the initial matchups of the tournament (can be modified but not manually added/removed after creation)
	 */
	protected SingleEliminationTournament(long tournamentId, ArrayList<Matchup> matchups){
		this(tournamentId, null, matchups);
	}

	/**
	 * Constructor
	 * Note: all matchups passed in will have their tournament set to this tournament
	 * 
	 * @param tournamentId The tournament id all the way from the Core
	 * @param players the players of the tournament (can be added/removed after construction
	 * @param matchups the initial matchups of the tournament (can be modified but not manually added/removed after creation)
	 */
	protected SingleEliminationTournament(long tournamentId, List<Player> players, ArrayList<Matchup> matchups){
		this.tournamentId = tournamentId;
		this.players = (players != null) ? players : new ArrayList<Player>();
		

		if(matchups != null){
			this.matchups = matchups;

			//Look through each matchup and add each player found in the matchups to the tournament's player list.
			for(Matchup m : matchups){
				if(m.getPlayerOne() != null && !players.contains(m.getPlayerOne())&&(!m.getPlayerOne().getUUID().equals(Player.BYE))){
					players.add(m.getPlayerOne());				
				}
				if(m.getPlayerTwo() !=null && !players.contains(m.getPlayerTwo())&&(!m.getPlayerTwo().getUUID().equals(Player.BYE))){
					players.add(m.getPlayerTwo());
				}

				m.setTournament(this);
			}

		}else{
			this.matchups = new ArrayList<Matchup>();
		}

		started = false;
		finished = false;
		round = 0;

		//set round timer to default (no timer) and default win points to default (2)
		roundDuration = DEFAULT_NO_TIMER;
		defaultWin = DEFAULT_DEFAULT_WIN;

		timerDisplays = new ArrayList<TextView>();
		
		matchIdCounter = 0;
	}
	
	/**
	 * Reset all tournament variables (except for id) to default values
	 */
	public void clearTournament(){
		players = new ArrayList<Player>();;
		matchups = new ArrayList<Matchup>();
		started = false;
		finished = false;
		round = 0;
		roundDuration = DEFAULT_NO_TIMER;
		defaultWin = DEFAULT_DEFAULT_WIN;
		if(roundTimer != null){
			roundTimer.cancel();
			roundTimer = null;			
		}
	}

	/**
	 * Set the round timer
	 * @param roundDuration duration of round in seconds
	 * @param timerHandler the handler that will be notified when timer updates
	 */
	public void setRoundTimer(int roundDuration, Handler timerHandler){

		//timerHandler must not be null.  if it is, don't do anything
		if(timerHandler != null){
			
			if(roundTimer != null){
				roundTimer.cancel();				
				roundTimer = null;
			}
			
			if(roundDuration != DEFAULT_NO_TIMER && roundDuration != CONFIRMED_NO_TIMER){
				roundTimer = new Timer();
				remainingSeconds = roundDuration;
			}
			
			this.roundDuration = roundDuration;
			this.timerHandler = timerHandler;
		}
	}

	/**
	 * Pauses or unpauses the round timer.
	 * @param paused boolean
	 */
	public void pauseTimer(boolean paused){
		timerPaused = paused;

	}
	
	/**
	 * Adds TextView to list of timer displays to be updated when time changes
	 * @param tv timer display to add
	 */
	public void addTimerDisplay(TextView tv){
		timerDisplays.add(tv);
	}
	
	/**
	 * Removes specified timer display from list of timer displays
	 * @param tv timer display to remove
	 * @return true if removed successfully
	 */
	public boolean removeTimerDisplay(TextView tv){
		return timerDisplays.remove(tv);
	}
	
	/**
	 * @return list of timer displays
	 */
	public ArrayList<TextView>getTimerDisplays(){
		return timerDisplays;
	}

	/**
	 * Set the points assigned for a default win.
	 * @param defaultWin the points awarded for a default win
	 */
	public void setDefaultWin(int defaultWin){
		this.defaultWin = defaultWin;
	}

	/**
	 * Add player to the tournament.
	 * @param player The player to add
	 */
	public void addPlayer(Player player){

		if(!players.contains(player)&&(!player.getUUID().equals(Player.BYE)))
		{
			players.add(player);
			getStandingsGenerator().addPlayer(player);
			Log.i("SingleEliminationTournament", "added player to Gen");
		}

		//if tournament not started, assign the player to a matchup
		if(!started){			

			ArrayList<Matchup> bottomRound = getBottomRound();
			boolean added = false;

			for(Matchup m : bottomRound){
				if(m.getPlayerOne() == null || m.getPlayerTwo() == null){								
					m.addPlayer(player);
					added = true;
					break; //break so player not added to multiple matchups
				}
			}

			if(!added){ //if not added, means there were no openings in current matchups. new round needs to be added.

				for(Matchup m : bottomRound){

					matchups.add(new Matchup( m.getPlayerOne(), m, this));
					m.removePlayer(m.getPlayerOne());

					matchups.add(new Matchup(m.getPlayerTwo(), m, this));
					m.removePlayer(m.getPlayerTwo());

				}

				//now that new matchups are created, push the request again
				addPlayer(player);
			}
		}
	}



	/**
	 * Removes given player from the tournament.
	 * Removes player from tournament matchups if tournament hasn't started.
	 * If tournament has started, removed player forfeits their current matchup UNLESS opponent is null, in which case nothing happens.
	 * @param p the player to remove
	 */
	public void removePlayer(Player p){

		for(Matchup m : matchups){

			if(m.containsPlayer(p)){

				//to fully remove from tournament/standings & replace with bye, matchup must be in first round and scores not set
				if(m.getScores() == null && m.getChildren().isEmpty()){
					m.swapPlayer(p, new Player(Player.BYE, "BYE"));
					players.remove(p);
					getStandingsGenerator().removePlayer(p);

					//if matchup is not finished but is in 2nd or later round, remove from matchup, tournament but not standings
				}else if(m.getWinner() == null){
					m.swapPlayer(p, new Player(Player.BYE, "QUIT")); 
					players.remove(p);
					//purpose of replacing with a BYE is to allow for new player to be added to slot

				}else{
					//do nothing if matchup is finished and in 2nd or later round
				}
			}
		}
	}




	/**
	 * Sets tournament rounds
	 * @param matchups of the tournament in original state - no winners
	 */
	public void setMatchups(ArrayList<Matchup> matchups){
		this.matchups = matchups;

		for(Matchup m : matchups){

			Player one = m.getPlayerOne();
			if(one != null && !this.players.contains(one)&&(!one.getUUID().equals(Player.BYE))){
				this.players.add(one);
			}

			Player two = m.getPlayerTwo();
			if(two != null && !this.players.contains(two)&&(!two.getUUID().equals(Player.BYE))){
				this.players.add(two);
			}
		}
	}

	/**
	 * Increments the current round.
	 */
	public void advanceRound()
	{
		round++;

		//send out email notifications
		this.getAutomaticMessageHandler().sendOutNotifications();

		//reset round timer
		remainingSeconds = roundDuration;
		
		//send message to participants
		this.getOutgoingCommandHandler().handleSendBeginNewRound(tournamentId, round);

	}
	
	/**
	 * Used to manually set the round of the tournament.  Should only be used on participant side;
	 * host should rely on startTournament and advanceRound (called by setScores and setWinner) to manage round
	 * @param round
	 */
	public void setRoundParticipant(int round){
		this.round = round;
	}
	
	/**
	 * Retrieves matchups of specified round.  This is different than getRound() because it bases its search on the
	 * matchup's roundParticipant variable rather than counting the round based on parent/children heirarchy, which can't
	 * be used in the case of participants who do not keep track of it.
	 * @param round of matchups to retrieve
	 * @return list of matchups in specified round
	 */
	public ArrayList<Matchup> getRoundParticipant(int round){
		
		ArrayList<Matchup> toReturn = new ArrayList<Matchup>();
		for(Matchup m : matchups){
			if(m.getRoundParticipant() == round){
				toReturn.add(m);
			}
		}
		
		return toReturn;
	}

	/**
	 *	Helper method used to retrieve matchups of specified round.  Used by getBottomRound and getCurrentRound
	 * @param round to retrieve
	 * @return matchups of specified round
	 */
	public ArrayList<Matchup> getRound(int round){

		ArrayList<Matchup> toReturn = new ArrayList<Matchup>();

		if(!matchups.isEmpty()){
			
			Matchup finalMatchup = null;
			for(Matchup m : matchups){
				if(m.getParent() == null){
					finalMatchup = m;
					break;
				}
			}
			
			int numRounds = finalMatchup.getRound();

			ArrayList<Matchup> parents = new ArrayList<Matchup>();
			parents.add(finalMatchup);

			int stopPt = round;
			if(stopPt == 0){
				stopPt = 1;
			}

			//if stopPt is higher than number of rounds, return null
			if(stopPt > numRounds){
				toReturn = null;
			}else{

				for(int i = numRounds-1; i >= stopPt; i--){
					ArrayList<Matchup> nextParents = new ArrayList<Matchup>();

					for(Matchup p : parents){

						for(Matchup m : matchups){

							if(m.getParent() != null && m.getParent().equals(p)){
								nextParents.add(m);
							}
						}				
					}
					parents = nextParents;
				}
				toReturn = parents;		
			}
		}
		return toReturn;
	}

	/**
	 * Get matchups of tournament's current round
	 * @return matchups of current round
	 */
	public ArrayList<Matchup> getCurrentRound(){
		ArrayList<Matchup> m = getRoundParticipant(round);
		if(getPermissionLevel() == Player.HOST){
			for(Matchup mp : getRound(round)){
				if(!m.contains(mp)){
					m.add(mp);
				}
			}
		}
		return m;
	}

	/**
	 * Used to retrieve the bottom round (or first round) of the tournament.
	 * @return ArrayList of Matchups in the bottom round
	 */
	public ArrayList<Matchup> getBottomRound(){
		return getRound(1);
	}

	/**
	 * Flags tournament as started and deals with any null matchups that occur due to odd number of players.
	 */
	public void startTournament(){

		if(!started){

			started = true;
			round = 1;			

			//start round timer (does nothing if roundTimer is null)
			startRoundTimer();
		}
	}


	/**
	 * Starts the Round timer
	 */
	public void startRoundTimer(){
		if(roundTimer != null){

			roundTimer.scheduleAtFixedRate(new TimerTask(){

				@Override
				public void run() {

					//each second, update the timer displays unless paused
					if(!timerPaused){
						updateTimerDisplays();
					}

				}    			

			}, 0, 1000); //fire each second
		}
	}


	/**
	 * Updates the roundTimerMinutes and roundTimerSeconds variables.
	 */
	private void updateTimerDisplays(){

		//decrement remaining seconds
		if(remainingSeconds > 0){
			remainingSeconds--;
		}

		//in all cases send message to update display
		timerHandler.obtainMessage(1).sendToTarget();
	}
	
	/**
	 * If there are no remaining player vs player matchups in the current round, all outstanding BYE matchups
	 * are automatically resolved and the tournament progresses to the next round.
	 * 
	 * Method should not be used by participants
	 */
	public void resolveCurrentRoundIfDone(){
		if(!isFinished()){
			boolean roundDone = true;

			ArrayList<Matchup> currentRound = getCurrentRound();
			for(Matchup m : currentRound){				
				if(m.getPlayerOne() != null && !m.getPlayerOne().getUUID().equals(Player.BYE)
						&& m.getPlayerTwo() != null && !m.getPlayerTwo().getUUID().equals(Player.BYE)
						&& m.getWinner() == null){

					//round is done unless there is a matchup with two actual players that isn't finished
					roundDone = false;
				}
			}

			if(roundDone){

				//Resolve BYE rounds
				for(Matchup m : currentRound){

					if(m.getPlayerOne() != null && m.getPlayerTwo() != null){

						if(m.getPlayerOne().getUUID().equals(Player.BYE) && m.getPlayerTwo().getUUID().equals(Player.BYE)){

							//in case of bye vs bye matchup, simply move a bye forward but no scores/winner
							m.getParent().addPlayer(new Player(Player.BYE, "BYE"));			

						}else if(m.getPlayerOne().getUUID().equals(Player.BYE)){

							//doing this manually rather than calling regular setScores/setWinner methods to avoid infinite recursion
							double[] d = new double[2];
							d[1] = getDefaultWin();
							d[0] = 0;
							m.setScoresAttribute(d);
							m.setWinnerAttribute(m.getPlayerTwo());
							m.getParent().addPlayer(m.getWinner());

							try {
								getStandingsGenerator().recordScore(m.getPlayerOne().getUUID(), m.getPlayerTwo().getUUID(), getRound(), m.getScores()[0], m.getScores()[1], m.getId());
							} catch (PlayerNotExistantException e)
							{
								//Should never happen
								throw new RuntimeException("Player Removed from Tournament Standings Generator, was still in a Matchup that got a score recorded");
							}							

						}else if(m.getPlayerTwo().getUUID().equals(Player.BYE)){

							double[] d = new double[2];
							d[0] = getDefaultWin();
							d[1] = 0;
							m.setScoresAttribute(d);
							m.setWinnerAttribute(m.getPlayerOne());
							m.getParent().addPlayer(m.getWinner());

							try {
								getStandingsGenerator().recordScore(m.getPlayerOne().getUUID(), m.getPlayerTwo().getUUID(), getRound(), m.getScores()[0], m.getScores()[1], m.getId());
							} catch (PlayerNotExistantException e)
							{
								//Should never happen
								throw new RuntimeException("Player Removed from Tournament Standings Generator, was still in a Matchup that got a score recorded");
							}			
						}
					}
				}
				advanceRound();
			}
		}
	}

	/**
	 * Used to force creation of new matchup; entails doubling tournament size with empty brackets.
	 * Place given player in one of the new matchups in current round.
	 * @param p
	 */
	public void expandBracket(Player p){
		
		Matchup oldFinal = null;
		for(Matchup m : matchups){
			if(m.getParent() == null){
				oldFinal = m;
				break;
			}
		}
		
		int numRounds = Math.max(oldFinal.getRound(), oldFinal.getRoundParticipant());
		
		//Save newMatchups to send to participants at end of method
		ArrayList<Matchup> newMatchups = new ArrayList<Matchup>();

		Matchup newFinal = new Matchup(null, this);
		newFinal.setRoundParticipant(numRounds+1);
		oldFinal.setParent(newFinal);
		matchups.add(newFinal);
		newMatchups.add(newFinal);

		//if currently in the final round, only one more matchup required
		if(numRounds == round){
			Matchup oldFinalSibling = new Matchup(p, new Player(Player.BYE, "BYE"), newFinal, this);
			oldFinalSibling.setRoundParticipant(numRounds);
			matchups.add(oldFinalSibling);
			newMatchups.add(oldFinalSibling);

		}else{

			Matchup parent = new Matchup(newFinal, this);
			parent.setRoundParticipant(numRounds);
			matchups.add(parent);
			newMatchups.add(parent);

			ArrayList<Matchup> parents = new ArrayList<Matchup>();
			parents.add(parent);

			//fill in bracket down to the current round.
			for(int i = numRounds-1; i > round; i--){

				ArrayList<Matchup> nextParents = new ArrayList<Matchup>();

				for(Matchup m : parents){
					Matchup childOne = new Matchup(m, this);
					Matchup childTwo = new Matchup(m, this);
					childOne.setRoundParticipant(i);
					childTwo.setRoundParticipant(i);

					nextParents.add(childOne);
					nextParents.add(childTwo);	
					matchups.add(childOne);
					matchups.add(childTwo);
					newMatchups.add(childOne);
					newMatchups.add(childTwo);
				}

				parents = nextParents;
			}



			//now fill in extra matchups for current round which will contain the given Player and the rest BYEs
			Matchup firstOne = parents.remove(0);
			Matchup left = new Matchup(p, new Player(Player.BYE, "BYE"), firstOne, this);
			Matchup right = new Matchup(new Player(Player.BYE, "BYE"), new Player(Player.BYE, "BYE"), firstOne, this);
			left.setRoundParticipant(round);
			right.setRoundParticipant(round);
			
			matchups.add(left);
			matchups.add(right);
			newMatchups.add(left);
			newMatchups.add(right);

			for(Matchup m : parents){
				Matchup left2 = new Matchup(new Player(Player.BYE, "BYE"), new Player(Player.BYE, "BYE"), m, this);
				Matchup right2 = new Matchup(new Player(Player.BYE, "BYE"), new Player(Player.BYE, "BYE"), m, this);
				left2.setRoundParticipant(round);
				right2.setRoundParticipant(round);
				
				matchups.add(left2);
				matchups.add(right2);
				newMatchups.add(left2);
				newMatchups.add(right2);
			}

		}
		
		//Notify participants of new matchups
		for(Matchup m : newMatchups){
			String[] team1 = new String[1];
			String[] team2 = new String[1];
			
			team1[0] = "null";
			team2[0] = "null";
			
			if(m.getPlayerOne() != null){
				team1[0] = m.getPlayerOne().getUUID().toString();
			}
			if(m.getPlayerTwo() != null){
				team2[0] = m.getPlayerTwo().getUUID().toString();
			}
			getOutgoingCommandHandler().handleSendMatchup(tournamentId, m.getId(), null, null, team1, team2, m.getRoundParticipant(), null);
		}

	}
	
	/**
	 * Used to randomly create matchups from a list of players
	 * If number of players != a power of 2 (8, 16, 32, etc), some players will not have an opponent in first round.
	 * 
	 * @param players the list of players to put into matchups 
	 * @param tournament the tournament the matchups should be assigned to.  It's ok for this to be null
	 * @return arraylist of Matchups made from the players given.
	 */
	public  static ArrayList<Matchup> generateRandomMatchups(List<Player> players, SingleEliminationTournament tournament){

		if(players != null){


			ArrayList<Matchup> matchups = new ArrayList<Matchup>();	

			//N is the number of players that matchups are created for.  May be different than actual number of players.
			int n = 1;
			while(n < players.size()){ //if there are 10 players, n will be 16.  if 16, n will be 16.  etc
				n *= 2;
			}

			Matchup finalRound = new Matchup(null, tournament); // no parent matchup
			matchups.add(finalRound);

			//handle 0, 1, 2 player cases, which means no additional matchups are created
			if(players.size() <= 2){

				//add the 0, 1, or 2 players to the only matchup of tournament
				for(Player p : players){
					finalRound.addPlayer(p);
				}
				
				if(finalRound.getPlayerOne() == null){
					finalRound.addPlayer(new Player(Player.BYE, "BYE"));
				}
				if(finalRound.getPlayerTwo() == null){
					finalRound.addPlayer(new Player(Player.BYE, "BYE"));
				}

			}else{

				int roundSize = 2; //starts at second last round where there will be 2 matchups (semi finals)
				ArrayList<Matchup> parents = new ArrayList<Matchup>();
				parents.add(finalRound);

				ArrayList<Matchup> bottomRound = new ArrayList<Matchup>();

				while(roundSize <= n/2){

					if(roundSize != n/2){

						//start building next round's parents
						ArrayList<Matchup> nextParents = new ArrayList<Matchup>();

						//expect parents to have size == roundSize/2			
						for(int i = 0; i < roundSize/2; i++){ 

							Matchup left = new Matchup(parents.get(i), tournament);
							Matchup right = new Matchup(parents.get(i), tournament);

							nextParents.add(left);
							nextParents.add(right);

							matchups.add(left);
							matchups.add(right);			

						}

						parents = nextParents;	

					}else{ //bottom round so save the matchups in separate list

						for(int i = 0; i < roundSize/2; i++){

							Matchup left = new Matchup(parents.get(i), tournament);
							Matchup right = new Matchup(parents.get(i), tournament);

							bottomRound.add(left);
							bottomRound.add(right);

						}
					}

					roundSize *=2;
				}


				ArrayList<Player> playersSafeToRemoveFrom = new ArrayList<Player>();
				for(Player p : players){
					playersSafeToRemoveFrom.add(p);
				}

				//assign players to bottom round of tournament randomly.  
				//if players do not fit evenly into brackets, some will be assigned no opponent and should automatically win round
				Random r = new Random();		
				while(playersSafeToRemoveFrom.size() > 0)
				{
					//in essence this will execute twice. First time will add first player to all matchups, second time will add player to first n matchups
					//where n is the number of matchups without a Bye
					for(Matchup m : bottomRound)
					{
						//for each matchup, remove up to one player from playersSafeToRemoveFrom and add to the matchup
						if(playersSafeToRemoveFrom.size() > 1)
						{
							//if playersToRemoveFrom has more than one left, remove one at random and add to the matchup
							m.addPlayer(playersSafeToRemoveFrom.remove(r.nextInt(playersSafeToRemoveFrom.size()-1)));
						}
						else if(playersSafeToRemoveFrom.size() == 1)
						{
							//if there is only one player left, add it to the matchup
							m.addPlayer(playersSafeToRemoveFrom.remove(0));
						}
						else
						{
							//no players left in matchup, therfore start add a BYE to the matchup
							m.addPlayer(new Player(Player.BYE,"BYE"));
						}
					}

				}

				//add matchups of bottom round to matchups
				for(Matchup m : bottomRound){
					matchups.add(m);
				}

			}

			return matchups;
		}else{
			return null;
		}
	}

	/**
	 * Checks validity of matches (valid matchups = no duplicate players).
	 * Should only be used for matchups for one round.  Will NOT work for full list of matchups for a tournament.
	 * @param matchups The matchups  (of one round) to check
	 * @return true if there are no duplicate players in matchups.  false if there are
	 */
	public static boolean validateMatchups(ArrayList<Matchup> matchups){

		//problem if matchups for more than one round.  will be returning false if there 2+ rounds of matchups b/c player (validly) belongs to more than 1 matchup
		ArrayList<Player> playersInMatchups = new ArrayList<Player>();

		for(Matchup m : matchups){
			int nullCount = 0;

			//Check if player one is non-null duplicate
			if(m.getPlayerOne() != null && !playersInMatchups.contains(m.getPlayerOne())){
				playersInMatchups.add(m.getPlayerOne());
			}else if(m.getPlayerOne() != null){
				return false;
			}else{
				nullCount++;
			}

			//Check if player 2 is non-null duplicate
			if(m.getPlayerTwo() != null && !playersInMatchups.contains(m.getPlayerTwo())){
				playersInMatchups.add(m.getPlayerTwo());
			}else if(m.getPlayerTwo() != null){
				return false;
			}else{
				nullCount++;
			}

			//Check for null vs null match ups
			if(nullCount == 2){
				return false;
			}
		}

		return true;
	}


	/**
	 * @return ArrayList of Matchups in the tournament
	 */
	public ArrayList<Matchup> getMatchups(){
		return matchups;
	}
	
	/**
	 * Expected to be called by matchup ctor; used so that a unique match id is used for each match in a tournament.
	 * @return id
	 */
	protected int getNextMatchId(){
		return matchIdCounter++;
	}

	/**
	 * @return true if tournament is started
	 */
	public boolean isStarted(){
		return started;
	}

	/**
	 * 	@return current round of tournament.  0 if not started
	 */
	public int getRound(){
		return round;
	}

	/**
	 * @return round duration in second
	 */
	public int getRoundDuration(){
		return roundDuration;
	}

	/**
	 * @return minutes remaining in current round
	 */
	public int getMinutes(){		
		int roundTimerSeconds = remainingSeconds % 60;
		int roundTimerMinutes = (remainingSeconds - roundTimerSeconds) / 60;

		return roundTimerMinutes;
	}

	/**
	 * @return seconds (0-59) remaining in current round
	 */
	public int getSeconds(){
		return remainingSeconds % 60;
	}

	/**
	 * @return points awarded in case of default win
	 */
	public int getDefaultWin(){
		return defaultWin;
	}

	/**
	 * End the tournament
	 */
	public void endTournament(){
		finished = true;

		//stop timer
		if(roundTimer != null){
			roundTimer.cancel();
		}
		
		if(getPermissionLevel() == Player.HOST){
			this.getOutgoingCommandHandler().handleSendBeginNewRound(tournamentId, -1);
		}		
	}

	/**
	 * Get whether the tournament has ended
	 * @return True if ended
	 */
	public boolean isFinished(){
		return finished;
	}


}
