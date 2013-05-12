package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.UUID;

import android.graphics.Bitmap;


/**
 * This class holds the information for a single participant throughout the SE tournament
 * @author waltzm
 * @version 10/6/2012
 */
public class Participant
{
	/**
	 * unique id for the player
	 */
	private UUID id;

	/**
	 * holds the round the player was eliminated in. -1 means not eliminated
	 */
	private int roundEliminatedIn  = -1;

	/**
	 * stores the number of wins per round, where index 0 holds the results for round 1
	 */
	private ArrayList<Double> winsPerRound;

	/**
	 * stores the number of losses per round, where index 0 holds the results for round 1
	 */
	private ArrayList<Double> lossesPerRound;

	/**
	 * stores whether or not the participant won for each round, where index 0 holds the results for round 1
	 */
	private ArrayList<Boolean> rounds;

	/**
	 * Stores the name of the participant
	 */
	private String name;
	
	/**
	 * Portrait of the participant
	 */
	private Bitmap portrait;
	
	/**
	 * Creates a participant with the given id and name
	 * @param id of the player
	 * @param name of the player
	 */
	public Participant(UUID id, String name){
		this(id, name, null);
	}

	/**
	 * Creates a participant with the given id and name and portrait
	 * @param id of the player
	 * @param name of the player
	 * @param portrait of the player
	 */
	public Participant(UUID id, String name, Bitmap portrait)
	{
		this.id = (id);
		this.name = name;
		this.winsPerRound = new ArrayList<Double>();
		this.lossesPerRound = new ArrayList<Double>();
		this.rounds  = new ArrayList<Boolean>();
		this.portrait = portrait;
	}

	/**
	 * Updates the players wins and losses per round, as long as the player hasn't been eliminated.
	 * Sets the player as eliminated if the lost.
	 * Returns false if the score is for a round after the participant has been eliminated
	 * @param playerOneScore your score in the given round
	 * @param playerTwoScore opponent's in the given round
	 * @param round of the scores
	 * @return if the score was added
	 */
	public boolean addScoresForRound(double playerOneScore, double playerTwoScore, int round)
	{
		if(round <1)
		{
			throw new NullPointerException("ERROR: The round that scores are being set for is "+round);
		}
		if(roundEliminatedIn!=-1&&round!=roundEliminatedIn)
		{
			//is eliminated and therefore should not be getting scores
			return false;
		}
		roundEliminatedIn = -1;
		//set the wins, losses and if they won
		this.winsPerRound.add((round-1), playerOneScore);
		this.lossesPerRound.add((round-1), playerTwoScore);

		if(playerOneScore>playerTwoScore)
		{
			//record a victory for the player
			this.rounds.add(round-1, true);
		}
		else
		{
			//record a loss for the player
			this.rounds.add(round-1, false);

			//set the player to eliminated
			roundEliminatedIn = round;
		}
		return true;

	}


	/**
	 * Getter for unique player id
	 * @return the id
	 */
	public UUID getId() 
	{
		return id;
	}
	
	/**
	 * Getter for portrait
	 * @return portrait
	 */
	public Bitmap getPortrait(){
		return portrait;
	}

	/**
	 * Returns total wins score thus far
	 * @return total wins
	 */
	public double getTotalWins()
	{
		double wins=0;
		for(int i =0;i<winsPerRound.size();i++)
		{
			wins+=winsPerRound.get(i);
		}
		return wins;	
	}

	/**
	 * Returns total loss score thus far
	 * @return total losses
	 */
	public double getTotalLosses()
	{
		double loss=0;
		for(int i =0;i<lossesPerRound.size();i++)
		{
			loss+=lossesPerRound.get(i);
		}
		return loss;	
	}

	/**
	 * Returns total round wins thus far
	 * @return total round wins
	 */
	public int getTotalRoundWins()
	{
		int wins=0;
		for(int i =0;i<rounds.size();i++)
		{
			if(rounds.get(i))
			{
				wins++;
			}
		}
		return wins;	
	}

	/**
	 * Returns total round losses thus far
	 * @return total round losses
	 */
	public int getTotalRoundLosses()
	{
		int loss=0;
		for(int i =0;i<rounds.size();i++)
		{
			if(!rounds.get(i))
			{
				loss++;
			}
		}
		return loss;	
	}

	/**
	 * Getter for wins per round overall
	 * @return the winsPerRound
	 */
	public ArrayList<Double> getWinsPerRound() {
		return winsPerRound;
	}

	/**
	 * Getter for rounds lost overall
	 * @return the lossesPerRound
	 */
	public ArrayList<Double> getLossesPerRound() {
		return lossesPerRound;
	}

	/**
	 * Getter for which rounds have been won or lost
	 * @return the roundsWon
	 */
	public ArrayList<Boolean> getRounds() {
		return rounds;
	}

	/**
	 * Returns whether or not a score has been recorded for the passed in round.
	 * If the player has been eliminated, returns true.
	 * @param round in question
	 * @return false if the player has no score for the round
	 */
	public boolean isCompletedWithRound(int round)
	{
		if(roundEliminatedIn!=-1|| this.rounds.size()>=round)
		{
			return true;
		}

		return false;
	}


	/**
	 * Overrides the equals method
	 * Returns true if both participants have the same unique id
	 * @param o participant to check equality against
	 * @return true if the participants have the same id, and false otherwise
	 */
	public boolean equals(Object o)
	{
		if(!(o instanceof Participant))
		{
			//if passed in object isn't a participant, it isn't equal
			return false;	
		}

		Participant p =(Participant)o;
		if( p.getId().equals(this.id))
		{
			//same id therefore same participant
			return true;
		}

		return false;
	}

	/**
	 * Getter for the standing for the player where 1 is not eliminated yet, 
	 * and the rest are what the final players standing will be
	 * @param numPlayers the total number of players
	 * @return the standing
	 */
	public int getStanding(int numPlayers) 
	{
		//if not eliminated, than in first place
		if(roundEliminatedIn==-1)
		{
			return 1;
		}

		double lastPlace = Math.log(numPlayers)/Math.log(2)+1;
		int place=(int) lastPlace;
		//round lastplace up
		if(lastPlace!=(double)place)
		{
			place++;
		}
		//if eliminated in the first round, get place last
		for(int i=1;i<=(int)lastPlace;i++)
		{
			if(roundEliminatedIn==i)
			{
				return place;
			}
			place--;
		}

		return 1;
	}

	/**
	 * Getter for the name of the participant
	 * @return name
	 */
	public String getName() 
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name+":"+id;
	}
	
	/**
	 * Getter for when the player was eliminated
	 * Returns -1 if not eliminated yet
	 * Returns 0 for round 1, 1 for round 2, etc.
	 * @return roundEliminated in
	 */
	public int getRoundEliminatedIn()
	{
		return roundEliminatedIn;
		
	}


}