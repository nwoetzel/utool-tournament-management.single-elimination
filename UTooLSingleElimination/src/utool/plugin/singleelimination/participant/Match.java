package utool.plugin.singleelimination.participant;

import utool.plugin.Player;

/**
 * Defines a single match within a round
 * @author waltzm
 * @version 12/3/2012
 * 
 * TODO This class could probably be removed and use Matchup instead.
 */
public class Match 
{
	
	/**
	 * Player 1's score in the match
	 */
	private double player1Score = 0;
	
	/**
	 * Player 1's score in the match
	 */
	private double player2Score = 0;

	/**
	 * First player in matchup
	 */
	private Player playerOne;

	/**
	 * Second player in matchup.
	 */
	private Player playerTwo;
	
	/**
	 * The winner of the matchup
	 */
	private Player winner;

	/**
	 * Matchup ID.  May not be needed in final version but was added for testing to determine correct assignment of parent matchups.
	 */
	private long id;
	
	/**
	 * holds the round the matchup is associated with
	 */
	private int round = 0;



	/**
	 * Constructor used when two players are known
	 * @param mid the unique id of the match
	 * @param one first player
	 * @param two second player
	 * @param winner the winner of the matchup
	 * @param round the round of the match
	 */
	public Match(long mid, Player one, Player two, Player winner, int round)
	{
		this.playerOne = one;
		this.playerTwo = two;	
		this.winner = winner;
		this.id = mid;
		this.round = round;

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
	 * @param playerOneScore the score player 1 got
	 * @param playerTwoScore the score player 2 got
	 */
	public void setScores(double playerOneScore, double playerTwoScore)
	{		
		this.player1Score = playerOneScore;
		this.player2Score = playerTwoScore;
		
		//notify SE gen
//		StandingsGeneratorSE.getInstance(null).recordScore(playerOne.getUUID(), playerTwo.getUUID(), round, playerOneScore, playerTwoScore, this.id);
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
	public double getScoreP1(){
		return this.player1Score;
	}
	
	/**
	 * @return scores
	 */
	public double getScoreP2(){
		return this.player2Score;
	}

	/**
	 * @return matchup id
	 */
	public long getId(){
		return id;
	}
	
	/**
	 * Getter for the round this match is in
	 * @return round
	 */
	public int getRound()
	{
		return round;
	}


	/**
	 * Check if two matchups are equal based on id
	 * @param m matchup to compare this matchup against
	 * @return true if this matchup's id is equal to m's id
	 */
	public boolean equals(Match m){
		if(this.id == m.getId()){
			return true;
		}else{
			return false;
		}
	}


}