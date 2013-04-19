package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import utool.plugin.Player;

/**
 * Data class that represents a single Matchup between two players
 * 
 * @author hoguet
 *
 * Date: 10-4-2012
 */
public class Matchup {

	/**
	 * Used to designate a forfeiture when setting scores.  
	 * Expected use: setScores(0, FORFEIT) which will set winner to player one
	 * 
	 * Note: Use of SingleEliminationTournament.DEFAULT_WIN may cause forfeit to not be used.
	 */
	public static final double FORFEIT = -1;

	public static final long NULL_PARENT = -1;

	/**
	 * The tournament this matchup belongs to
	 */
	private SingleEliminationTournament tournament; //needed to access round of tournament by matchups

	/**
	 * Counter used for generating matchup IDs.
	 */
	private static long idCounter = 0;

	/**
	 * Winner of the matchup
	 */
	private Player winner;

	/**
	 * Players' scores
	 */
	private double[] scores;

	/**
	 * First player in matchup
	 */
	private Player playerOne;

	/**
	 * Second player in matchup.
	 */
	private Player playerTwo;

	/**
	 * Flag that indicates if player one forfeited while player two was null.  This causes player two to automatically win when they are added to this match
	 */
	private boolean playerOneForfeit;

	/**
	 * Flag that indicates if player one forfeited while player two was null.  This causes player two to automatically win when they are added to this match
	 */
	private boolean playerTwoForfeit;

	/**
	 * The next matchup in the tournament; the winner of this matchup gets sent to the parent matchup.
	 */
	private Matchup parent;

	/**
	 * Matchups who have this matchup as a parent  
	 * TODO since there would ever only be two children, it is possible that there could instead be leftChild/rightChild 
	 * and binary tree operations could be performed for matchups.  However I haven't figured out how to incorporate this yet.
	 */
	private ArrayList<Matchup> children;

	/**
	 * Matchup ID.  May not be needed in final version but was added for testing to determine correct assignment of parent matchups.
	 */
	private long id;

	/**
	 * Used for the participant to know what round the matchup belongs to, since it does not have access to the parent
	 */
	private int round = -5;


	/**
	 * Constructor used when two players are known
	 * @param one first player
	 * @param two second player
	 * @param parent the matchup that the winner gets sent to
	 * @param tournament the tournament this matchup is part of.  It can be null
	 */
	public Matchup(Player one, Player two, Matchup parent, SingleEliminationTournament tournament){
		this.playerOne = one;
		this.playerTwo = two;
		this.parent = parent;
		this.tournament = tournament;

		children = new ArrayList<Matchup>();

		idCounter++;
		this.id = idCounter;

		if(parent != null){
			parent.addChild(this);
		}

		playerOneForfeit = false;
		playerTwoForfeit = false;
	}

	/**
	 * Possible to make matchup with one player in event of waiting for second player to be determined or for odd # players
	 * @param one Player one of match up
	 * @param parent the matchup that the winner gets sent to
	 * @param tournament the tournament this matchup is part of.  It can be null
	 */
	public Matchup(Player one, Matchup parent, SingleEliminationTournament tournament){
		this.playerOne = one;
		this.parent = parent;
		this.tournament = tournament;

		children = new ArrayList<Matchup>();

		idCounter++;
		this.id = idCounter;

		if(parent != null){
			parent.addChild(this);
		}

		playerOneForfeit = false;
		playerTwoForfeit = false;
	}

	/**
	 * Possible to make matchup with no players in case they aren't known yet.
	 * @param parent the matchup that the winner gets sent to
	 * @param tournament the tournament this matchup is part of.  It can be null
	 */	
	public Matchup(Matchup parent, SingleEliminationTournament tournament)
	{
		this.parent = parent;
		this.tournament= tournament;

		children = new ArrayList<Matchup>();

		idCounter++;
		this.id = idCounter;

		if(parent != null){
			parent.addChild(this);
		}

		playerOneForfeit = false;
		playerTwoForfeit = false;
	}

	/**
	 * Used for participant communication so that exact matchup can be created.
	 * Host should not use this method.
	 * @param id
	 */
	public void setId(long id){
		this.id = id;
	}

	/**
	 * Adds given matchup as a child
	 * @param m new child
	 */
	public void addChild(Matchup m){
		if(!children.contains(m)){
			children.add(m);
		}
	}

	/**
	 * Removes given matchup as a child
	 * @param m matchup to remove
	 */
	public void removeChild(Matchup m){
		children.remove(m);
	}

	public void setChildren(ArrayList<Matchup> children){
		this.children = children;
	}

	/**
	 * @return children
	 */
	public ArrayList<Matchup> getChildren(){
		return children;
	}

	public void setRoundParticipant(int round){
		this.round = round;
	}

	public int getRoundParticipant(){
		return round;
	}

	/**
	 * Sets winner to the specified player if the specified player belongs to this matchup and scores have not already been set
	 * Private because users of the matchup are intended to call set scores to define a winner.
	 * @param p the winner
	 * @return true if winner set, false if scores already set
	 */
	private boolean setWinner(Player p){


		if(tournament.getPermissionLevel() != Player.HOST){
			winner = p;
			return true;
		}else{

			boolean toReturn;

			if(scores != null && (p.equals(playerOne) || p.equals(playerTwo))){

				//if new winner is determined by this call, remove the old winner from parent matchup before adding new winner
				if(winner != null && !winner.equals(p)){
					parent.removePlayer(winner);
				}

				winner = p;

				if(parent != null){
					parent.addPlayer(p); //winner progresses to next round
				}else{
					//if parent is null, that means winner is set for final round and tournament is done.
					tournament.endTournament();
				}

				toReturn = true;
			}else{//can't set winner to a player that was not part of matchup
				toReturn = false;
			}

			//Check current round for any remaining non-null non-bye undecided matchups.  If there are none, advance round.
			tournament.resolveCurrentRoundIfDone();

			return toReturn;

		}

	}



	/**
	 * Adds player to matchup if possible.
	 * @param p the player to add
	 * @return true if player added, false if matchup already full or already contains p
	 */
	public boolean addPlayer(Player p){
		
		boolean toReturn = false;
		
		if(playerOne == null){
			playerOne = p;

			//if existing opponent has forfeited, automatically advance
			if(playerTwoForfeit){
				setScores(0, FORFEIT);
			}

			toReturn = true;
		}else if(playerTwo == null && !playerOne.equals(p)){
			playerTwo = p;

			//if existing opponent has forfeited, automatically advance
			if(playerOneForfeit){
				setScores(FORFEIT, 0);
			}

			toReturn = true;
		}else{
			toReturn = false; //cant add player if already two players are in matchup
		}	
		
		//If a player was added, send the updated matchup to participants
		if(toReturn){
			int round;
			//send matchup TODO something doesnt work here on first add
			if(tournament==null)
			{
				round = 1;
			}
			else 
			{
				round = tournament.getRound()+1;
			}
			String p1="null";
			String p2="null";
			if(playerOne!=null)
			{
				p1 = playerOne.getUUID().toString();
			}
			if(playerTwo!=null)
			{
				p2 = playerTwo.getUUID().toString();
			}
			String[] t1 = {p1};
			String[] t2 = {p2};

			if(tournament != null){

				long parentId = NULL_PARENT;
				if(parent != null){
					parentId = parent.getId();
				}

				try{
					tournament.getOutgoingCommandHandler().handleSendMatchup(tournament.getTournamentId(), id, parentId, null, null, t1, t2, round, "Table 1");
				}catch(NullPointerException e){
					//This exception if thrown if message is sent before miCore is initialized.  Ignore because it will get sent again when requested
				}
			}
		}
		
		return toReturn;
		
	}

	/**
	 * Removes given player from the matchup
	 * @param p the player to remove
	 * @return true if player removed, false if player not in this matchup
	 */
	public boolean removePlayer(Player p){
		if(playerOne != null && playerOne.equals(p)){
			playerOne = null;
			return true;
		}else if(playerTwo != null && playerTwo.equals(p)){
			playerTwo = null;
			return true;
		}else{
			return false; //player was not in this matchup
		}
	}

	/**
	 * Used to "swap" player one.  Sets player one to the given player and returns the player that used to be there
	 * @param p new player one
	 * @return old player one
	 */
	public Player swapPlayerOne(Player p){
		Player toReturn = playerOne;
		playerOne = p;

		String[] team1 = {p.getUUID().toString()};
		String[] team2 = {playerTwo.getUUID().toString()};

		//notify outgoing command handler of changed matchup
		tournament.outgoingCommandHandler.handleChangeMatchup(tournament.getTournamentId(), id, null, null, team1, team2, tournament.getRound(), null);

		return toReturn;
	}

	/**
	 * 
	 * @param p new player two
	 * @return old player two
	 */
	public Player swapPlayerTwo(Player p){
		Player toReturn = playerTwo;
		playerTwo = p;

		String[] team1 = {playerOne.getUUID().toString()};
		String[] team2 = {p.getUUID().toString()};

		//notify outgoing command handler of changed matchup
		tournament.outgoingCommandHandler.handleChangeMatchup(tournament.getTournamentId(), id, null, null, team1, team2, tournament.getRound(), null);

		return toReturn;
	}

	/**
	 * Swaps oldP with newP if oldP is in this matchup.
	 * @param oldP player to swap out
	 * @param newP player to swap in
	 * @return player that was swapped out or null if oldP was not in this matchup
	 */
	public Player swapPlayer(Player oldP, Player newP){

		Player toReturn = null;

		if(playerOne != null && playerOne.equals(oldP)){
			toReturn = playerOne;
			playerOne = newP;
		}else if(playerTwo != null && playerTwo.equals(oldP)){
			toReturn = playerTwo;
			playerTwo = newP;
		}

		if(toReturn != null){

			String[] team1 = {playerOne.getUUID().toString()};
			String[] team2 = {playerTwo.getUUID().toString()};

			//notify outgoing command handler of changed matchup
			tournament.outgoingCommandHandler.handleChangeMatchup(tournament.getTournamentId(), id, null, null, team1, team2, tournament.getRound(), null);

		}

		return toReturn;

	}

	/**
	 * Checks if given player is in this matchup
	 * @param p player to check for
	 * @return true if matchup contains the given player
	 */
	public boolean containsPlayer(Player p){

		if(p != null && ( (playerOne != null && playerOne.equals(p)) || (playerTwo != null && playerTwo.equals(p)) )){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Sets scores of the match.  Winner automatically set based on scores
	 * @param playerOneScore p1's score
	 * @param playerTwoScore p2's score
	 */
	public void setScores(double playerOneScore, double playerTwoScore){		
		scores = new double[2];
		scores[0] = playerOneScore;
		scores[1] = playerTwoScore;	

		//Notify standings generator of result.  TODO does standings generator handle ties?
		if(tournament != null){

			//Set UUIDs to the players' UUIDs or Bye constant if null
			UUID uuidOne = Player.BYE;
			if(playerOne != null){
				uuidOne = playerOne.getUUID();
			}

			UUID uuidTwo = Player.BYE;
			if(playerTwo != null){
				uuidTwo = playerTwo.getUUID();
			}


			try {
				
				//only notify generator once - from host side
				if(tournament.getPermissionLevel() == Player.HOST){
					tournament.getStandingsGenerator().recordScore(uuidOne, uuidTwo, tournament.getRound(), scores[0], scores[1], id);
				}
				
			} catch (PlayerNotExistantException e)
			{

				//Should never happen
				throw new RuntimeException("Player Removed from Tournament Standings Generator, was still in a Matchup that got a score recorded.\n"+e.getMessage());
			}
		}

		if(playerOneScore > playerTwoScore){
			setWinner(playerOne);
		}else if(playerTwoScore > playerOneScore){ 
			setWinner(playerTwo);
		}else{
			//if a tie, randomly choose winner.  
			//This should possibly be chosen as a setting in advanced options - how to handle ties
			if(new Random().nextBoolean()){
				setWinner(playerOne);
			}else{
				setWinner(playerTwo);
			}
		}


	}

	/**
	 * "Simple" set scores handle which ONLY sets the scores attribute.
	 * Should usually use the other method.
	 * @param scores
	 */
	public void setScoresAttribute(double[] scores){
		this.scores = scores;
	}

	/**
	 * Simple set winner handle which ONLY sets the winner attribute.
	 * Should normally use the regular setWinner method
	 * @param p the winner
	 */
	public void setWinnerAttribute(Player p){
		this.winner = p;
	}

	/**
	 * Sets tournament this match belongs to.
	 * @param tournament the tournament
	 */
	public void setTournament(SingleEliminationTournament tournament){
		this.tournament = tournament;
	}

	/** 
	 * @return player one
	 */
	public Player getPlayerOne(){
		return playerOne;
	}

	/**
	 * @return player two
	 */	
	public Player getPlayerTwo(){
		return playerTwo;
	}

	/**
	 * @return winner
	 */
	public Player getWinner(){
		return winner;
	}

	/**
	 * @return scores
	 */
	public double[] getScores()
	{
		return scores;
	}

	/**
	 * @return parent
	 */
	public Matchup getParent(){
		return parent;
	}

	/**
	 * @return matchup id
	 */
	public long getId(){
		return id;
	}

	/**
	 * @return tournament of this matchup
	 */
	public SingleEliminationTournament getTournament(){
		return tournament;
	}

	/**
	 * Return the round of the tournament this matchup belongs to.  Equal to the depth of the children of the matchup.
	 * @return round of the tournament this matchup belongs to
	 */
	public int getRound(){

		int toReturn = 1;

		ArrayList<Matchup> nextChildren = children;		

		while(!nextChildren.isEmpty()){
			toReturn++;
			nextChildren = nextChildren.get(0).getChildren();
		}

		return toReturn;
	}

	/**
	 * @return playerOneForfeit
	 */
	public boolean oneForfeited(){
		return playerOneForfeit;
	}

	/**
	 * @return playerTwoForfeit
	 */
	public boolean twoForfeited(){
		return playerTwoForfeit;
	}

	/**
	 * @param forfeit sets playerOneForfeit
	 */
	public void playerOneSetForfeit(boolean forfeit){
		playerOneForfeit = forfeit;
		if(playerTwo != null){
			setScores(FORFEIT, 0);
		}
	}

	/**
	 * @param forfeit sets playerTwoForfeit
	 */
	public void playerTwoSetForfeit(boolean forfeit){
		playerTwoForfeit = forfeit;
		if(playerOne != null){
			setScores(0, FORFEIT);
		}
	}

	/**
	 * @param m the parent matchup
	 * @return true if parent set successfully
	 */
	public boolean setParent(Matchup m){

		if(parent == null){
			parent = m;
			parent.addChild(this);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * A helper method that finds a matchup from a list by  id
	 * @param matchups the list of Matchups to look through
	 * @param id of matchup to retrieve
	 * @return Matchup with specified id, null if there is no matchup with that id
	 */
	public static Matchup getMatchupById(List<Matchup> matchups, long id){
		
		Matchup toReturn = null;
		for(Matchup m : matchups){
			if(m.getId() == id){
				toReturn = m;
				break;
			}
		}
		
		return toReturn;
	}

	/**
	 * Check if two matchups are equal based on id
	 * @param m matchup to compare this matchup against
	 * @return true if this matchup's id is equal to m's id
	 */
	public boolean equals(Matchup m){
		if(this.id == m.getId()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Currently used for testing purposes.  Generates a string with matchup's info in it.
	 * @return a String with matchup info on it
	 */
	public String printMatchup(){

		String parent = "null";
		if(this.parent != null){
			parent = ""+this.parent.getId();
		}

		String playerOne = "null";
		if(this.playerOne != null){
			playerOne = this.playerOne.getName();
		}

		String playerTwo = "null";
		if(this.playerTwo != null){
			playerTwo = this.playerTwo.getName();
		}

		String toReturn = "";
		toReturn += "Match ID: "+id+"\n"; 
		toReturn += "Parent: "+parent+"\n";
		toReturn += "One: " +playerOne+"\n";
		toReturn += "Two: " +playerTwo+"\n";

		return toReturn;

	}

	/**
	 * Another method currently used for testing purposes.  Generates string with matchup's info in it.
	 * @return a String with matchup info on it
	 */
	public String printMatchup2(){
		String parent = "null";
		if(this.parent != null){
			parent = ""+this.parent.getId();
		}

		String playerOne = "null";
		if(this.playerOne != null){
			playerOne = this.playerOne.getName();
		}

		String playerTwo = "null";
		if(this.playerTwo != null){
			playerTwo = this.playerTwo.getName();
		}

		String toReturn = "("+id+") "+playerOne+" vs. "+playerTwo+" (Parent: "+parent+") [Score: "+printScores()+"]";

		return toReturn;
	}

	/**
	 * Helper method that parses the scores of the round into a string
	 * @return a String with scores on it.  -1 - -1 if scores are null
	 */
	public String printScores(){
		String toReturn = "0 to 0";

		if(scores != null){
			toReturn = scores[0] + " to " + scores[1];
		}

		return toReturn;
	}


}
