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
	 * Signifies null parent... TODO I don't think this is used
	 */
	public static final long NULL_PARENT = -1;
	
	/**
	 * Index of left child in parent's children list, if it exists
	 */
	public static final int LEFT_CHILD = 0;
	
	/**
	 * Index of right child in parent's children list, if it exists
	 */
	public static final int RIGHT_CHILD = 1;

	/**
	 * The tournament this matchup belongs to
	 */
	private SingleEliminationTournament tournament; //needed to access round of tournament by matchups

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
	 * The next matchup in the tournament; the winner of this matchup gets sent to the parent matchup.
	 */
	private Matchup parent;

	/**
	 * Matchups who have this matchup as a parent  
	 * TODO since there would ever only be two children, it is possible that there could instead be leftChild/rightChild attributes instead of a list
	 */
	private ArrayList<Matchup> children;

	/**
	 * Matchup ID.  May not be needed in final version but was added for testing to determine correct assignment of parent matchups.
	 */
	private long id;

	/**
	 * Used for the participant to know what round the matchup belongs to, since it does not have access to the parent
	 * -5 is arbitrary value meant to show that it is un-set
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

		this.id = tournament.getNextMatchId();

		if(parent != null){
			parent.addChild(this);
		}
	}

	/**
	 * Possible to make matchup with one player in event of waiting for second player to be determined or for odd # players
	 * @param one Player one of match up
	 * @param parent the matchup that the winner gets sent to
	 * @param tournament the tournament this matchup is part of.  It can be null
	 */
	public Matchup(Player one, Matchup parent, SingleEliminationTournament tournament){
		this(one, null, parent, tournament);
	}

	/**
	 * Possible to make matchup with no players in case they aren't known yet.
	 * @param parent the matchup that the winner gets sent to
	 * @param tournament the tournament this matchup is part of.  It can be null
	 */	
	public Matchup(Matchup parent, SingleEliminationTournament tournament)
	{
		this(null, null, parent, tournament);
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

	/**
	 * Sets the children attribute
	 * @param children
	 */
	public void setChildren(ArrayList<Matchup> children){
		this.children = children;
	}

	/**
	 * @return children
	 */
	public ArrayList<Matchup> getChildren(){
		return children;
	}

	/**
	 * Sets the round attribute
	 * @param round
	 */
	public void setRoundParticipant(int round){
		this.round = round;
	}

	/**
	 * @return round attribute
	 */
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
					
					//Winner progresses to next round.  Add to appropriate slot depending on which child this matchup is.
					if(parent.getChildren().get(LEFT_CHILD).equals(this)){
						parent.addPlayerOne(p);
					}else{ //.get(RIGHT_CHILD).equals(this)
						parent.addPlayerTwo(p);
					}
					
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
	 * Try adding a player to player one slot
	 * @param p player to add
	 * @return true if player was added, false if could not be added (already a player in player one spot)
	 */	
	public boolean addPlayerOne(Player p){
		if(playerOne != null) return false;
		
		playerOne = p;
		
		//Update participants
		int round;
		//send matchup
		if(tournament==null){
			round = 1;
		}
		else{
			round = tournament.getRound()+1;
		}
		String p1="null";
		String p2="null";
		if(playerOne!=null){
			p1 = playerOne.getUUID().toString();
		}
		if(playerTwo!=null){
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
		return true;
	}
	
	/**
	 * Try adding a player to player two slot
	 * @param p player to add
	 * @return true if player was added. false if it could not be (already a player in player two slot)
	 */
	public boolean addPlayerTwo(Player p){
		if(playerTwo != null) return false;
		
		playerTwo = p;
		//Update participants
		int round;
		//send matchup
		if(tournament==null){
			round = 1;
		}
		else{
			round = tournament.getRound()+1;
		}
		String p1="null";
		String p2="null";
		if(playerOne!=null){
			p1 = playerOne.getUUID().toString();
		}
		if(playerTwo!=null){
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
		return true;				
	}



	/**
	 * Adds player to matchup to first open slot if possible.
	 * @param p the player to add
	 * @return true if player added, false if matchup already full or already contains p
	 */
	public boolean addPlayer(Player p){
		
		boolean toReturn = addPlayerOne(p);
		if(!toReturn){
			toReturn = addPlayerTwo(p);
		}
		return toReturn;
		
//		if(playerOne == null){
//			playerOne = p;
//
//			//if existing opponent has forfeited, automatically advance
//			if(playerTwoForfeit){
//				setScores(0, FORFEIT);
//			}
//
//			toReturn = true;
//		}else if(playerTwo == null && !playerOne.equals(p)){
//			playerTwo = p;
//
//			//if existing opponent has forfeited, automatically advance
//			if(playerOneForfeit){
//				setScores(FORFEIT, 0);
//			}
//
//			toReturn = true;
//		}else{
//			toReturn = false; //cant add player if already two players are in matchup
//		}	
		
		
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

		String[] team1 = {"null"};
		String[] team2 = {"null"};
		
		if(playerOne != null) team1[0] = playerOne.getUUID().toString();		
		if(playerTwo != null) team2[0] = playerTwo.getUUID().toString();

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

		String[] team1 = {"null"};
		String[] team2 = {"null"};
		
		if(playerOne != null) team1[0] = playerOne.getUUID().toString();		
		if(playerTwo != null) team2[0] = playerTwo.getUUID().toString();

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
			toReturn = swapPlayerOne(newP);
		}else if(playerTwo != null && playerTwo.equals(oldP)){
			toReturn = swapPlayerTwo(newP);
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

		//Notify standings generator of result.
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
	 * Return the round of the tournament this matchup belongs to.  Equal to the depth of the children of the matchup,
	 * or the round attribute if it has been set.
	 * @return round of the tournament this matchup belongs to
	 */
	public int getRound(){
		return Math.max(round, getRoundRecursive(this));
	}
	
	/**
	 * Helper method which determines the round the given matchup belongs to by finding the depth of its children
	 * @param m the matchup
	 * @return the round m belongs to
	 */
	private static int getRoundRecursive(Matchup m){
		
		ArrayList<Matchup> children = m.getChildren();
		Matchup left = (children.size() >= 1) ? children.get(0) : null;
		Matchup right = (children.size() == 2) ? children.get(1) : null;
		
		if(left == null) return 1;
		if(right == null) return getRoundRecursive(left);
		else return 1 + Math.max(getRoundRecursive(left), getRoundRecursive(right));	
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
		return this.id == m.getId();
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
