package utool.plugin.singleelimination.communications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.util.Log;
import android.widget.TextView;
import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.MatchupsActivity;
import utool.plugin.singleelimination.PlayerNotExistantException;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;

/**
 * When a message is received from the host, the message will be parsed and then will call the IncomingCommandHandler? method corresponding to 
 * the message received. TheIncomingCommandHandler? is responsible for notifying the appropriate part of the SE plugin that a message has been received.
 * 
 * This handler is for a participant SE plugin to have the messages received be pushed to its internal state
 * @author waltzm
 * @author hoguet
 * @version 3/22/13
 */
public class IncomingCommandHandlerParticipant extends AbstractIncomingCommandHandler
{	

	/**
	 * A reference to the Matchups Activity used for refreshing the UI.
	 */
	private MatchupsActivity activity;

	/**
	 * Creates an incomingCommandHandlerParticipant
	 * @param tournament2 the tournament to connect to
	 * @param activity the activity associated with this tournament
	 */
	public IncomingCommandHandlerParticipant(SingleEliminationTournament tournament2, MatchupsActivity activity)
	{
		super(tournament2);
		this.activity = activity;
	}

	@Override
	public void handleChangeMatchup(long id, long matchid, String team1name, String team2Name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		super.handleChangeMatchup(id, matchid, team1name, team2Name, team1, team2, round, table);

		//notify the matchup generator of the change

		ArrayList<Matchup> matchups = ((SingleEliminationTournament)t).getMatchups();
		Matchup theOne = Matchup.getMatchupById(matchups, matchid);

		ArrayList<Player> players = ((SingleEliminationTournament)t).getPlayers();
		Player p1 = null;
		Player p2 = null;

		for(Player p : players){
			if(p.getUUID().equals(UUID.fromString(team1.get(0)))){
				p1 = p;
			}else if(p.getUUID().equals(UUID.fromString(team2.get(0)))){
				p2 = p;
			}
		}

		if(p1 == null && Player.BYE.equals(UUID.fromString(team1.get(0)))){
			p1 = new Player(Player.BYE, "BYE");
		}

		if(p2 == null && Player.BYE.equals(UUID.fromString(team2.get(0)))){
			p2 = new Player(Player.BYE, "BYE");
		}

		if(theOne != null && p1 != null && p2 != null){
			theOne.swapPlayerOne(p1);
			theOne.swapPlayerTwo(p2);
		}else{
			Log.e("ICH", "error editing matchup; invalid matchup or player");
		}

		this.activity.refreshMatchupsList(null, null);

	}

	@Override
	public void handleSendBeginNewRound(long id, int round)
	{
		super.handleSendBeginNewRound(id, round);

		if(round > 0){		
			t.setRoundParticipant(round);		
		}else{ //a negative val indicates tournament ended.  -1 expected
			t.endTournament();
		}
		
		activity.updateRoundDisplay();
	}

	@Override
	public void handleSendError(long id, String playerid, String name, String message)
	{
		super.handleSendError(id, playerid, name, message);
		//right now there are no known errors that the SE plugin of a participant could receive
	}

	@Override
	public void handleSendFinalStandings(String id, ArrayList<String> players, ArrayList<Double> w, ArrayList<Double> l, ArrayList<Integer> rw, ArrayList<Integer> rl, ArrayList<Integer> s)
	{
		super.handleSendFinalStandings(id, players, w, l, rw, rl, s);
	}

	@Override
	public void handleSendMatchup(long id, long matchid, long parentId, String team1name, String team2Name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		super.handleSendMatchup(id, matchid, parentId, team1name, team2Name, team1, team2, round, table);

		ArrayList<Matchup> matchups = t.getMatchups();
		
		//If matchup already exists, remove it here  (Will get re-added by end of method with new info)
		Matchup toRemove = Matchup.getMatchupById(matchups, matchid);

		if(toRemove != null){
			matchups.remove(toRemove);
		}

		Matchup parent = null;

		if(parentId != Matchup.NULL_PARENT){
			parent = Matchup.getMatchupById(matchups, parentId);
		}

		ArrayList<Player> players = t.getPlayers();
		Player p1 = null;
		Player p2 = null;

		//if players are "null" leave them as null; don't try matching with players or byes
		if(!team1.get(0).equals("null") || !team2.get(0).equals("null")){

			for(Player p : players){
				if(!team1.get(0).equals("null") && p.getUUID().equals(UUID.fromString(team1.get(0)))){
					p1 = p;
				}else if(!team2.get(0).equals("null") && p.getUUID().equals(UUID.fromString(team2.get(0)))){
					p2 = p;
				}
			}

			if(p1 == null && !team1.get(0).equals("null") && Player.BYE.equals(UUID.fromString(team1.get(0)))){
				p1 = new Player(Player.BYE, "BYE");
			}

			if(p2 == null && !team2.get(0).equals("null") && Player.BYE.equals(UUID.fromString(team2.get(0)))){
				p2 = new Player(Player.BYE, "BYE");
			}

		}


		if( (parent != null || parentId == Matchup.NULL_PARENT) ){//&& p1 != null && p2 != null){
			Matchup newM = new Matchup(p1, p2, parent, t);
			newM.setId(matchid);
			newM.setRoundParticipant(round);
			matchups.add(newM);
			t.setMatchups(matchups);
		}else{
			Log.e("ICH", "Send matchup failed - invalid player or parent");
		}

		this.activity.refreshMatchupsList(null, null);

	}

	@Override
	public void handleSendPlayers(String id, ArrayList<Player> players)
	{
		super.handleSendPlayers(id, players);

		//update playerlist
		t.setPlayers(players);

		//determine if local user changed permission level
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getUUID().equals(t.pid))
			{
				//if player is local user
				int permissionLevel = players.get(i).getPermissionsLevel();
				t.setPermissionLevel(permissionLevel);
				Log.e("ICH Participant", "Setting permission level to: "+permissionLevel);
			}
		}

		//update players in standings generator
		List<Player> old = t.getStandingsGenerator().getPlayers();
		
		//add all of the players that didn't exist in StandingsGenerator to the list		
		for(int i=0;i<players.size();i++)
		{
			//check if exists in old
			if(!old.contains(players.get(i)))
			{
				//if not add new player to list
				t.getStandingsGenerator().addPlayer(players.get(i));
			}
			//do nothing if the player already existed
		}
		
		//remove any players that were removed
		for(int i=0;i<old.size();i++)
		{
			if(!players.contains(old.get(i)))
			{
				t.getStandingsGenerator().removePlayer(old.get(i));
			}
		}

		activity.refreshMatchupsList(null, null);

	}

	@Override
	public void handleSendRoundTimerAmount(long id, String time)
	{
		super.handleSendRoundTimerAmount(id, time);

		if(t.getTimerDisplays().isEmpty()){
			t.addTimerDisplay(activity.getTimerDisplay());
		}

		activity.setTimerTextParticipant(time);

	}

	@Override
	public void handleSendScore(long id, long matchid, String player1, String player2, String score1, String score2, int round)
	{
		super.handleSendScore(id, matchid, player1, player2, score1, score2, round);

		//notify standings generator
		try 
		{
			t.getStandingsGenerator().recordScore(UUID.fromString(player1), UUID.fromString(player2), round, Double.parseDouble(score1), Double.parseDouble(score2), matchid);
		} 
		catch (PlayerNotExistantException e) 
		{
			//Send error
			t.getOutgoingCommandHandler().handleSendError(id, t.pid+"","PlayerNotFound", "One of the players not found");
		}

		//set the specified score for the specified matchup
		ArrayList<Matchup> matchups = ((SingleEliminationTournament)t).getMatchups();

		Matchup theOne = Matchup.getMatchupById(matchups, matchid);

		if(theOne != null){
			theOne.setScores(Double.parseDouble(score1), Double.parseDouble(score2));
		}		

		activity.refreshMatchupsList(null, null);

	}

	@Override
	public void handleSendTournamentName(long id, String name)
	{
		super.handleSendTournamentName(id, name);

		//notify main activity to be able to display name
		t.setTournamentName(name);
		activity.updateTournamentName();
	}

	@Override
	public void handleSendClear(long tid) 
	{
		super.handleSendClear(tid);

		activity.resetDisplayedRound(); //reset displayed round so that it doesnt try to load nonexistent round
		t.clearTournament();
		t.startTournament(); //tournament always started
		t.getStandingsGenerator().resetScores();
		activity.updateRoundDisplay();		
		//clear tournament information

	}

}
