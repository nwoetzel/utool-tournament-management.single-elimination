package utool.plugin.singleelimination.communications;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import android.util.Log;
import utool.plugin.Player;
import utool.plugin.dummy.DummyMessage;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;

/**
 * When a part of the SE plugin wants to send a message to either the host or 
 * all participants, they call the corresponding method within OutgoingCommandHandler. 
 * OutgoingCommandHandler? will then turn the command into the accepted xml command, 
 * and finally send it to the host.
 * @author waltzm
 * @version 10/20/2012
 */
public class OutgoingCommandHandler extends AbstractOutgoingCommandHandler
{

	/**
	 * Constructor for the OutgoingCommandHandler
	 * @param tournamentLogic The tournament to associate this object with
	 */
	public OutgoingCommandHandler(TournamentLogic tournamentLogic)
	{
		super(tournamentLogic);
		this.tournament = tournamentLogic;
		/*
		 * List of methods and where they are called
		 * 
		 * 1. BeginNewRound
		 * 		* StandingsGeneratorSE in progressToNextRound
		 * 2. handleSendError
		 * 		* IncomingCommandHandler when input is not as expected
		 * 3. handleSendFinalStandings
		 * 		* StandingsGeneratorSE when send final standings is called
		 * 4. handleSendPlayers
		 * 		* StandingsGenerator when it is instantiated it sends the list it received
		 * 		* standingsGenerator in add player
		 * 		* Standings Generator in both remove players
		 * 5. handleSendScore
		 * 		*StandingsGenerator when recordScore is called and there isn't an error
		 * 6. handleSendTournamentName
		 * 		*SingleEliminationMainActivity in onCreate
		 * 7. handleSendMatchup
		 * 		*called from Matchup
		 */

		/*
		 * TODO: list of outgoing command handler methods that are not, but need to be called at some point
		 * 1. handleChangeMatchup
		 * 2. handleSendError (wherever applicable)
		 * 3. sendFinalStandings in StandingsGenerator must be called when tournament ends
		 * 4. handleSendRoundTimerAmount
		 */

	}
	

	/**
	 * Notifies the connect participants two things:
	 * 1. Tournament has ended
	 * 2. Final scores fore everyone
	 * The plugin should already know the final scores since it has been recieving
	 * score updates, however this serves as a checker for correct information being passed.
	 * If the plugin detected that a score is different than what it was expecting it will send back
	 * an error message.
	 * @param id of the tournament
	 * @param players list of player ids in the tournament 
	 * @param wins for each player overall
	 * @param losses for each player overall
	 * @param roundWins for each player overall
	 * @param roundLosses for each player overall
	 * @param standing for each player at the end of the tournament
	 */
	public void handleSendFinalStandings(long id, String[] players, Double[] wins, Double[] losses, Integer[] roundWins, Integer[] roundLosses, Integer[] standing)
	{
		handleSendDummyRefresh();
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendFinalStandings" );
			command.addAttribute("id", id+"");

			//add information for each player
			for(int i=0;i<players.length;i++)
			{
				command.addSubCommand("Player");
				command.addSubAttribute("PlayerId",""+players[i]);
				command.addSubAttribute("Wins",""+wins[i]);
				command.addSubAttribute("Losses",""+losses[i]);
				command.addSubAttribute("RoundWins",""+roundWins[i]);
				command.addSubAttribute("RoundLosses",""+roundLosses[i]);
				command.addSubAttribute("Standing",""+standing[i]);
			}

			sendCommand( command , hostOnly);
		}
		catch(ParserConfigurationException e)
		{
			Log.e( "OutgoingCommandHandler", "Error sending change matchup command", e );
		}
	}

	/**
	 * Informs participants of a match between team1 and team2 during round
	 * "round" and at table "table". The table can be null and if so will not be sent.
	 * Round should be the current round going on.
	 * @param id of the tournament
	 * @param matchId of the match being sent
	 * @param team1Name name of team 1 if applicable
	 * @param team2Name name of team 2 if applicable
	 * @param team1 of the match
	 * @param team2 of the match
	 * @param round of the match
	 * @param table where the match will occur
	 */
	public void handleSendMatchup(long id,long matchId,long parentId, String team1Name, String team2Name, String[] team1, String[] team2, int round, String table)
	{
		
		Log.i("OUTGOING", "sending matchup "+matchId+" "+team1[0]+" "+team2[0]+" "+round);
		
		handleSendDummyRefresh();
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendMatchup" );
			command.addAttribute("id", id+"");
			command.addAttribute("Matchup", matchId+"");
			command.addAttribute("Parent", parentId+"");

			command.addSubCommand("Team1");
			if(team1Name!=null)
			{
				command.addSubAttribute("Name",team1Name);
			}
			//add team 1 players
			for(int i=0;i<team1.length;i++)
			{
				command.addSubAttribute("Player",team1[i]+"");
			}

			command.addSubCommand("Team2");
			if(team2Name!=null)
			{
				command.addSubAttribute("Name",team2Name);
			}
			//add team 2 players
			for(int i=0;i<team2.length;i++)
			{
				command.addSubAttribute("Player",team2[i]+"");
			}

			command.addAttribute("Round", round+"");
			if(table!=null)
			{
				command.addAttribute("Table", table+"");
			}
			sendCommand( command , hostOnly);
			
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send matchup command", e );
		}
	}


	/**
	 * Sends a score for the matchup to participants. The sending of team names
	 * is just for error checking to make sure everything is working as intended.
	 * 
	 * @param id of the tournament
	 * @param matchId of the match
	 * @param team1name name of the team or id of the single player
	 * @param team2name name of the team or id of the single player
	 * @param team1score score of team 1 for the round
	 * @param team2score score of team 2 for the round
	 * @param round of the match to send a score for
	 */
	public void handleSendScore(long id, long matchId, String team1name, String team2name, double team1score, double team2score, int round)
	{
		
		Log.i("OUTGOING", "sending score "+matchId+" "+team1score+" "+team2score);
		
		if(tournament.getPermissionLevel()==Player.HOST)
		{
			handleSendDummyRefresh();
		}
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendScore" );
			command.addAttribute("id", id+"");
			command.addAttribute("Matchup", matchId+"");

			command.addSubCommand("Team1");
			command.addSubAttribute("Name",team1name);
			command.addSubAttribute("Score",team1score+"");

			command.addSubCommand("Team2");
			command.addSubAttribute("Name",team2name);
			command.addSubAttribute("Score",team2score+"");

			command.addAttribute("Round", round+"");

			sendCommand( command , modOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send score command", e );
		}
	}

	
	/**
	 * Send data to instances of the dummy plugin
	 */
	public void handleSendDummyRefresh()
	{
		SingleEliminationTournament t = (SingleEliminationTournament) tournament;
		List<Matchup> matchups = t.getMatchups();
		String message = "";
		for (int i = 0; i < matchups.size(); i++){
			Matchup m = matchups.get(i);
			if (m != null){
				message += m.printMatchup2() + "\n";
			}
		}
		Log.i("dummySend", message);
		DummyMessage command = new DummyMessage(message, null);
		tournament.bridge.sendMessage(command.getXml());
	}


}
