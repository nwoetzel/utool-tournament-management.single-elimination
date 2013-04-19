package utool.plugin.singleelimination.test;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.singleelimination.communications.AbstractIncomingCommandHandler;

/**
 * When a message is received from the host, the message will be parsed and then will call the IncomingCommandHandler? method corresponding to the message received. TheIncomingCommandHandler? is responsible for notifying the appropriate part of the SE plugin that a message has been received.
 * This implementation does nothing except call the appropriate test method.
 * @author waltzm
 * @version 10/25/2012
 */
public class IncomingCommandHandlerForTests extends AbstractIncomingCommandHandler 
{

	/**
	 * the testcase to send msgs to
	 */
	private TestFromXML tester;

	/**
	 * Constructor that takes the testcase to send messages to
	 * @param testFromXML the testcase to send msgs to
	 */
	public IncomingCommandHandlerForTests(TestFromXML testFromXML) 
	{
		super(null);
		tester = testFromXML;
	}

	@Override
	public void handleChangeMatchup(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		tester.changeMatchupTest(id,matchid,team1name,team2name,team1,team2,round,table);
	}

	@Override
	public void handleSendBeginNewRound(long id, int round)
	{
		tester.sendBeginNewRoundTest(id, round);
	}

	@Override
	public void handleSendError(long id, String playerid, String name, String message)
	{
		tester.sendErrorTest(id, playerid, name, message);
	}
	
	@Override
	public void handleSendFinalStandings(String id, ArrayList<String> players, ArrayList<Double> w, ArrayList<Double> l, ArrayList<Integer> rw, ArrayList<Integer> rl, ArrayList<Integer> s)
	{
		tester.sendFinalStandingsTest(id, players, w, l, rw, rl, s);
	}

	
	public void handleSendMatchup(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		tester.sendMatchupTest(id,matchid,team1name,team2name,team1,team2,round,table);
	}

	@Override
	public void handleSendPlayers(String id, ArrayList<Player> players)
	{
		tester.sendPlayersTest(id, players);
	}

	@Override
	public void handleSendRoundTimerAmount(long id, String time)
	{
		tester.sendRoundTimerTest(id, time);
	}

	@Override
	public void handleSendScore(long id, long matchid, String team1name, String team2name, String score1, String score2, int round)
	{
		tester.sendScoreTest(id, matchid, team1name, team2name, Double.parseDouble(score1), Double.parseDouble(score2), round);
	}

	@Override
	public void handleSendTournamentName(long id, String name)
	{
		tester.sendTournamentNameTest(id, name);
	}
}
