package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.util.Log;
import utool.plugin.Player;
import utool.plugin.singleelimination.communications.OutgoingCommandHandler;



/**
 * This class is responsible for generating and storing the standings
 * of all of the participants of the tournament
 * @date 10/6/2012
 * @author waltzm
 *
 */
public class StandingsGeneratorSE 
{

	/**
	 * list of participating players in the tournament
	 */
	private List<Player> players;

	/**
	 * Internal list of players with more detailed information and the same indicies as players
	 */
	private ArrayList<Participant> participants;

	/**
	 * The tournament id from the Core
	 */
	private long tournamentId;

	/**
	 * Outgoing command handler for this object.
	 * Never access this directly, use getOutgoingCommandHandler()
	 */
	private OutgoingCommandHandler outgoingCommandHandler;

	/**
	 * log tag for standings generator
	 */
	private static final String LOG_TAG = "StandingsGenerator";

	/**
	 * private constructor to maintain singleton nature
	 * @param tournamentId The tournament id from the core
	 * @param players participating in the tournament
	 */
	public StandingsGeneratorSE(long tournamentId, List<Player> players)
	{
		this.tournamentId = tournamentId;

		if(players==null)
		{
			players = new ArrayList<Player>();
		}

		List<Player> p = new ArrayList<Player>();
		for(int i=0;i<players.size();i++)
		{
			p.add(players.get(i));
		}
		this.players = p;

		participants = new ArrayList<Participant>();

		//create list of participants
		for(int i=0;i<players.size();i++)
		{
			//add each player to participant list in same order
			this.participants.add(new Participant(players.get(i).getUUID(), players.get(i).getName()));
		}

		Player[] playz = new Player[players.size()];
		for(int i=0;i<players.size();i++)
		{
			playz[i] = players.get(i);
		}

	}

	/**
	 * Get the OutgoingCommandHandler for this object
	 * @return OutgoingCommandHandler
	 */
	private OutgoingCommandHandler getOutgoingCommandHandler(){
		if (outgoingCommandHandler == null){
			synchronized (this) {
				if (outgoingCommandHandler == null){
					outgoingCommandHandler = SingleEliminationTournament.getInstance(tournamentId).getOutgoingCommandHandler();
				}
			}
		}
		return outgoingCommandHandler;
	}

	/**
	 * Adds a player to the list of participating players
	 * Player is added with 0-0-0 as their record and are not given in record
	 * for previous rounds.
	 * @param id unique player id
	 * @param name player name linked to the id
	 */
	public void addPlayer(UUID id, String name)
	{
		this.addPlayer(new Player(id,name));
	}

	/**
	 * Adds a player to the list of participating players
	 * Player is added with 0-0-0 as their record and are not given in record
	 * for previous rounds.
	 * @param p player to be added
	 */
	public void addPlayer(Player p)
	{
		Log.e(LOG_TAG,"Player added to list: "+p);
		
		Participant par = new Participant(p.getUUID(), p.getName());
		
		if(!participants.contains(par))
		{
			Log.d(LOG_TAG,"Player actually added to list: "+p);
			this.players.add(p);
			
			SingleEliminationTournament tournament = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId);
			//Have to fill in rounds that participant missed if they were added when tournament in progress
			for(int i = 1; i < tournament.getRound(); i++){
				par.addScoresForRound(tournament.getDefaultWin(), 0.0, i);
			}
			this.participants.add(par);
			
			Log.d(LOG_TAG,"Players: "+players);
			Log.d(LOG_TAG,"Participants: "+participants);
		}

		this.sendPlayersToOCH();

	}
	
	/**
	 * Updates the OCh for new players
	 */
	private void sendPlayersToOCH()
	{
		//send players to OCH if host
		if(SingleEliminationTournament.getInstance(tournamentId).getPermissionLevel()==Player.HOST)
		{
			Player[] playz = new Player[players.size()];
			for(int i=0;i<players.size();i++)
			{
				playz[i] = players.get(i);
			}
			getOutgoingCommandHandler().handleSendPlayers(tournamentId, playz);
		}
	}

	/**
	 * Removes the player from the list of players, if they exist
	 * @param p player to be removed
	 * @return true if the player is removed, false if the player isn't found in the list
	 */
	public boolean removePlayer(Player p)
	{
		int index = this.participants.indexOf(new Participant(p.getUUID(), p.getName()));

		if(index>0)
		{
			this.participants.remove(index);
			this.players.remove(index);	

			this.sendPlayersToOCH();

			return true;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Removes the player from the list of players, if they exist
	 * @param playerid of the player to remove
	 * @return true if the player is removed, false if the player isn't found in the list
	 */
	public boolean removePlayer(UUID playerid)
	{
		return this.removePlayer(new Player(playerid,""));
	}


	/**
	 * Tells the match standings to the generator so that it can update the standings
	 * @param player1 of the match (by player id)
	 * @param player2 of the match (by payer id)
	 * @param round of the match
	 * @param playerOneScore of the match
	 * @param playerTwoScore of the match
	 * @param matchId id of the match beign recorded
	 * @return false if either player does not exist,  true otherwise
	 * @throws PlayerNotExistantException Thrown if either player is not found
	 */
	public boolean recordScore(UUID player1, UUID player2, int round, double playerOneScore, double playerTwoScore, long matchId) throws PlayerNotExistantException
	{
		//Log.d("Standings Generator","Recording a match: "+player1+", "+player2+", "+round+", "+playerOneScore+", "+playerTwoScore);

		//check that both players exist
//		int p1 = participants.indexOf(new Participant(player1, ""));
//		int p2 = participants.indexOf(new Participant(player2,""));
//		List's indexOf method does not work as this above code seems to expect
//		Below is a fix to achieve the desired functionality
//		--Justin
		int p1 = -1;
		int p2 = -1;
		Participant participantOne = new Participant(player1, "");
		Participant participantTwo = new Participant(player2, "");
		for (int i = 0; i < participants.size(); i++){
			if (participants.get(i).equals(participantOne)){
				p1 = i;
			}
			if (participants.get(i).equals(participantTwo)){
				p2 = i;
			}
			if (p1 != -1 && p2 != -1){
				break;
			}
		}
		if(p1<0||p2<0)
		{
			//test if either is a bye
			if(player1.equals(Player.BYE))
			{
				//indicates a bye on p1 which is fine
				if(p2<0)
				{
					//p2 is non existent
					throw new PlayerNotExistantException("Player 2, "+player2+",  is not in the Standings Generator list of players");
				}

			}

			else if(player2.equals(Player.BYE))
			{
				//indicates a bye on p2 which is fine
				if(p1<0)
				{
					//p1 is non existent
					throw new PlayerNotExistantException("Player 1, "+player1+",  is not in the Standings Generator list of players");
				}

			}
			else
			{
				//neither were byes but didn't exist in list
				Log.e(LOG_TAG , "Players: "+ this.players);
				Log.e(LOG_TAG , "Participants: "+ this.participants);
				Log.e(LOG_TAG , "Player 1: "+ player1);
				Log.e(LOG_TAG , "Player 2: "+ player2);
				throw new PlayerNotExistantException("One of the Players is not in the Standings Generator list of players");
			}
		}

		boolean status = true;
		//add the score for the players
		if(p1>-1)
		{
			//p1 isn't a bye
			if( participants.get(p1).addScoresForRound(playerOneScore, playerTwoScore, round)==false)
			{
				//score wasn't recorded
				status = false;
			}
		}
		if(p2>-1)
		{
			//p2 isn't a bye
			if(participants.get(p2).addScoresForRound(playerTwoScore, playerOneScore, round)==false)
			{
				//score wasn't recorded
				status= false;
			}


		}		
		//send to the core if worked
		if(status)
		{
			//if host, send the score	
			if(TournamentLogic.getInstance(tournamentId).getPermissionLevel()==Player.HOST)
			{
				getOutgoingCommandHandler().handleSendScore(tournamentId, matchId, player1.toString(), player2.toString(), playerOneScore, playerTwoScore, round);
			}
		}

		return status;
	}

	/**
	 * returns the said players standing
	 * returns -1 if the player does not exist
	 * @param playerid of the player
	 * @return the standing of the player
	 */
	public int getPlayerStanding(UUID playerid)
	{
		//locate the player
		int index = participants.indexOf(new Participant(playerid,""));

		if(index==-1)
		{
			//doesnt exist
			return -1;
		}

		Participant p = participants.get(index);

		//return the standing
		return (int) p.getStanding(participants.size());
	}

	/**
	 * returns the wins for that player up to and through the given round
	 * returns -1 if the player does not exist or the round is out of bounds
	 * @param playerid of the player
	 * @param round to stop at
	 * @return the number of wins the player has
	 */
	public double getPlayerWins(UUID playerid, int round)
	{
		//locate the player
		int index = participants.indexOf(new Participant(playerid,""));

		if(index==-1)
		{
			return -1;
		}

		//make sure round is allowable
		if(round<1)
		{
			return -1;
		}

		Participant p = participants.get(index);
		ArrayList<Double> wins = p.getWinsPerRound();
		//sum the wins of the player up to the round
		double sum = 0;
		for(int i=0;i<wins.size()&&i<round;i++)
		{
			sum+=wins.get(i);
		}

		//return the wins
		return sum;
	}

	/**
	 * returns the losses for that player up to and through the passed in round
	 * returns -1 if the player does not exist or the round is out of bounds
	 * @param playerid of the player
	 * @param round to stop at
	 * @return the number of losses the player has
	 */
	public double getPlayerLosses(UUID playerid, int round)
	{
		//locate the player
		int index = participants.indexOf(new Participant(playerid,""));

		if(index==-1)
		{
			return -1;
		}

		//make sure round is allowable
		if(round<1)
		{
			return -1;
		}

		Participant p = participants.get(index);
		ArrayList<Double> losses = p.getLossesPerRound();
		//sum the losses of the player up to round
		double sum = 0;
		for(int i=0;i<losses.size()&&i<round;i++)
		{
			sum+=losses.get(i);
		}

		//return the wins
		return sum;
	}

	/**
	 * returns the round wins for that player up to and through the given round
	 * returns -1 if the player does not exist or the round is out of bounds
	 * @param playerid of the player
	 * @param round to stop at
	 * @return the round wins the player has
	 */
	public int getPlayerRoundWins(UUID playerid, int round)
	{
		//locate the player
		int index = participants.indexOf(new Participant(playerid,""));
		if(index==-1)
		{
			return -1;
		}
		Participant p = participants.get(index);

		//make sure round is allowable
		if(round<1)
		{
			return -1;
		}

		ArrayList<Boolean> rounds = p.getRounds();

		//sum the number of rounds won
		int num = 0;
		for(int i=0;i<rounds.size()&&i<round;i++)
		{
			if(rounds.get(i)==true)
			{
				num++;
			}
		}

		//return the wins
		return num;
	}

	/**
	 * returns the round losses for that player up to and through the given round number
	 * returns -1 if the player does not exist or the round is out of bounds
	 * @param playerid of the player
	 * @param round to stop after
	 * @return the number of round losses the player achieved
	 */
	public int getPlayerRoundLosses(UUID playerid, int round)
	{
		//locate the player
		int index = participants.indexOf(new Participant(playerid,""));
		if(index==-1)
		{
			return -1;
		}
		Participant p = participants.get(index);

		//make sure round is allowable
		if(round<1)
		{
			return -1;
		}

		ArrayList<Boolean> rounds = p.getRounds();

		//sum the number of rounds lost
		int num = 0;
		for(int i=0;i<rounds.size()&&i<round;i++)
		{
			if(rounds.get(i)==false)
			{
				num++;
			}
		}

		//return the losses
		return num;
	}

	/**
	 * Returns the list of participating players
	 * @return list of participants
	 */
	public List<Player> getPlayers() 
	{
		return players;
	}

	/**
	 * Returns the list of participating players
	 * @return list of participants
	 */
	public ArrayList<Participant> getParticipants() 
	{
		return participants;
	}

	/**
	 * Clear the scores set so far and return to round 1
	 */
	public void resetScores()
	{
		//clear participants and remake
		participants = new ArrayList<Participant>();
		//create list of participants
		for(int i=0;i<players.size();i++)
		{
			//add each player to participant list in same order
			this.participants.add(new Participant(players.get(i).getUUID(),players.get(i).getName()));
		}

	}


	/**
	 * Sends the final standings to the outgoingcommandhandler if all scores have been entered
	 * 
	 * @return true if scores were sent
	 */
	public boolean sendFinalStandings()
	{
		//scores ready to be sent, now to build the arrays
		String[] players = new String[this.participants.size()];
		Double[] wins =  new Double[this.participants.size()];
		Double[] losses = new Double[this.participants.size()];
		Integer[] roundWins = new Integer[this.participants.size()];
		Integer[] roundLosses = new Integer[this.participants.size()];
		Integer[] standing = new Integer[this.participants.size()];

		//add to each for each participant
		for(int i=0;i<this.participants.size();i++)
		{
			Participant p = participants.get(i);
			players[i] = p.getId().toString();
			wins[i] = p.getTotalWins();
			losses[i] = p.getTotalLosses();
			roundWins[i] = p.getTotalRoundWins();
			roundLosses[i] = p.getTotalRoundLosses();
			standing[i] = p.getStanding(participants.size());
		}

		getOutgoingCommandHandler().handleSendFinalStandings(tournamentId, players, wins, losses, roundWins, roundLosses, standing);

		return true;

	}




}

