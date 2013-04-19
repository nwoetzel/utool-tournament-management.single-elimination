package utool.plugin.singleelimination.test;

import java.util.UUID;

import utool.plugin.Player;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;
import junit.framework.TestCase;

/**
 * This test is to fully test the creation of xml through the Outgoing Command Handler
 * @author waltzm
 * @version 10/25/2012
 */
public class TestToXML extends TestCase{

	/**
	 * match id
	 */
	private long mid = 2;

	/**
	 * tournament id
	 */
	private long tid = 1;

	/**
	 * name of team 1
	 */
	private String n1 = null;

	/**
	 * name of team 2
	 */
	private String n2 = null;

	/**
	 * name of team 3
	 */
	private String n3 = "scissors";

	/**
	 * name of team 4
	 */
	private String n4 = "Spock";

	/**
	 * Player 1
	 */
	private Player p1 = new Player("Bill");

	/**
	 * Player 2
	 */
	private Player p2 = new Player("Bob");

	/**
	 * Player 3
	 */
	private Player p3 = new Player("Jane");

	/**
	 * Player 4
	 */
	private Player p4 = new Player("Jill");

	/**
	 * Team 1 which consists of just p1
	 */
	private String[] t1 = {p1.getUUID().toString()};

	/**
	 * Team 2 which consists of just p2
	 */
	private String[] t2 = {p2.getUUID().toString()};

	/**
	 * Team 3 which consists of p1 and p2
	 */
	private String[] t3 = {p1.getUUID().toString(), p2.getUUID().toString()};

	/**
	 * Team 4 which consists of p3 and p4
	 */
	private String[] t4 = {p3.getUUID().toString(), p4.getUUID().toString()};

	/**
	 * default round of the match
	 */
	private int round = 1;

	/**
	 * default table
	 */
	private String table = "3.1A";

	/**
	 * list of all the players
	 */
	private Player[] players = {p1,p2,p3,p4};
	
	/**
	 * holds the tournament object used in the tests
	 */
	private SingleEliminationTournament tournament;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tournament = (SingleEliminationTournament) TournamentLogic.getInstance(0, null);
		tournament.setPermissionLevel(Player.HOST);
		
	}

	/**
	 * Tests that the change Matchup xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testChangeMatchup() 
	{	
		//check change matchup with 1 person teams
		tournament.getOutgoingCommandHandler().handleChangeMatchup(tid, mid, n1, n2, t1, t2, round, table);
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"changeMatchup\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Player>"+p1.getUUID().toString()+"</Player>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Player>"+p2.getUUID().toString()+"</Player>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"<Table>"+table+"</Table>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//check change matchup with 2 person teams

		tournament.getOutgoingCommandHandler().handleChangeMatchup(tid, mid, n3, n4, t3, t4, round, table);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"changeMatchup\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Name>"+n3+"</Name>"+"\n"+
				"<Player>"+p1.getUUID().toString()+"</Player>"+"\n"+
				"<Player>"+p2.getUUID().toString()+"</Player>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Name>"+n4+"</Name>"+"\n"+
				"<Player>"+p3.getUUID().toString()+"</Player>"+"\n"+
				"<Player>"+p4.getUUID().toString()+"</Player>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"<Table>"+table+"</Table>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//test if table being null works as intended
		tournament.getOutgoingCommandHandler().handleChangeMatchup(tid, mid, n1, n2, t1, t2, round, null);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"changeMatchup\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Player>"+p1.getUUID().toString()+"</Player>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Player>"+p2.getUUID().toString()+"</Player>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);
	}

	/**
	 * Tests that the send Matchup xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendMatchup() 
	{	
		//check send matchup with 1 person teams
		tournament.getOutgoingCommandHandler().handleSendMatchup(tid, mid,n1, n2, t1, t2, round, table);
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendMatchup\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Player>"+p1.getUUID().toString()+"</Player>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Player>"+p2.getUUID().toString()+"</Player>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"<Table>"+table+"</Table>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//check send matchup with 2 person teams

		tournament.getOutgoingCommandHandler().handleSendMatchup(tid, mid, n3, n4, t3, t4, round, table);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"sendMatchup\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Name>"+n3+"</Name>"+"\n"+
				"<Player>"+p1.getUUID().toString()+"</Player>"+"\n"+
				"<Player>"+p2.getUUID().toString()+"</Player>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Name>"+n4+"</Name>"+"\n"+
				"<Player>"+p3.getUUID().toString()+"</Player>"+"\n"+
				"<Player>"+p4.getUUID().toString()+"</Player>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"<Table>"+table+"</Table>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//test if table being null works as intended
		tournament.getOutgoingCommandHandler().handleSendMatchup(tid, mid, n1, n2, t1, t2, round, null);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"sendMatchup\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Player>"+p1.getUUID().toString()+"</Player>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Player>"+p2.getUUID().toString()+"</Player>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);
	}


	/**
	 * Tests that the send Score xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendScore() 
	{	
		//check scores with 1 person teams
		tournament.getOutgoingCommandHandler().handleSendScore(tid, mid, p1.getUUID().toString(), p2.getUUID().toString(), 3d, 0d, round);
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendScore\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Name>"+p1.getUUID().toString()+"</Name>"+"\n"+
				"<Score>"+"3.0"+"</Score>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Name>"+p2.getUUID().toString()+"</Name>"+"\n"+
				"<Score>"+"0.0"+"</Score>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//check send score with 2 person teams
		tournament.getOutgoingCommandHandler().handleSendScore(tid, mid, n3, n4, 2d, 1d, round);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"sendScore\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Matchup>"+mid+"</Matchup>"+"\n"+
				"<Team1>"+"\n"+
				"<Name>"+n3+"</Name>"+"\n"+
				"<Score>"+"2.0"+"</Score>"+"\n"+
				"</Team1>"+"\n"+
				"<Team2>"+"\n"+
				"<Name>"+n4+"</Name>"+"\n"+
				"<Score>"+"1.0"+"</Score>"+"\n"+
				"</Team2>"+"\n"+
				"<Round>"+round+"</Round>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);
	}


	/**
	 * Tests that the send Players xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendPlayers() 
	{	
		//test for 4 players, no ghosts and no seeds
		tournament.getOutgoingCommandHandler().handleSendPlayers(tid, players);
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendPlayers\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p1.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+p1.getName()+"</Name>"+"\n"+
				"<isGhost>"+p1.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p2.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+p2.getName()+"</Name>"+"\n"+
				"<isGhost>"+p2.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p3.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+p3.getName()+"</Name>"+"\n"+
				"<isGhost>"+p3.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p4.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+p4.getName()+"</Name>"+"\n"+
				"<isGhost>"+p4.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"</Player>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//test seeds
		Player s1 = new Player(new UUID(0,45), "Bob",false, 1);
		Player s2 = new Player(new UUID(0,56), "Tim",false, 2);
		Player s3 = new Player(new UUID(0,23), "Bob",false, 4);
		Player s4 = new Player(new UUID(0,53), "Mike",false, 3);
		Player[] seeds = {s1,s2,s3,s4};
		tournament.getOutgoingCommandHandler().handleSendPlayers(tid,seeds);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"sendPlayers\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s1.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s1.getName()+"</Name>"+"\n"+
				"<isGhost>"+s1.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s1.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s2.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s2.getName()+"</Name>"+"\n"+
				"<isGhost>"+s2.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s2.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s3.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s3.getName()+"</Name>"+"\n"+
				"<isGhost>"+s3.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s3.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s4.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s4.getName()+"</Name>"+"\n"+
				"<isGhost>"+s4.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s4.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

		//test ghosts
		s1 = new Player(new UUID(0,45), "Bob",true, 1);
		s2 = new Player(new UUID(0,56), "Tim",false, 2);
		s3 = new Player(new UUID(0,23), "Bob",true, 4);
		s4 = new Player(new UUID(0,53), "Mike",false, 3);
		Player[] ghost = {s1,s2,s3,s4};
		tournament.getOutgoingCommandHandler().handleSendPlayers(tid,ghost);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"sendPlayers\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s1.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s1.getName()+"</Name>"+"\n"+
				"<isGhost>"+s1.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s1.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s2.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s2.getName()+"</Name>"+"\n"+
				"<isGhost>"+s2.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s2.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s3.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s3.getName()+"</Name>"+"\n"+
				"<isGhost>"+s3.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s3.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+s4.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Name>"+s4.getName()+"</Name>"+"\n"+
				"<isGhost>"+s4.isGhost()+"</isGhost>"+"\n"+
				"<permissionLevel>"+Player.DEVICELESS+"</permissionLevel>"+"\n"+
				"<Seed>"+s4.getSeedValue()+"</Seed>"+"\n"+
				"</Player>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected,xml);

	}

	/**
	 * Tests that the send final standing xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendFinalStandings() 
	{
		String[] p = {p1.getUUID().toString(),p2.getUUID().toString(),p3.getUUID().toString(),p4.getUUID().toString()};
		Double[]  wins = {5d,6d,2d,0d};
		Double[]  losses = {0d,2d,5d,6d};
		Integer[]  roundWins = {1,2,3,0};
		Integer[]  roundLosses = {0,2,3,1};
		Integer[]  standing = {1,3,2,4};
		tournament.getOutgoingCommandHandler().handleSendFinalStandings(tid, p, wins, losses, roundWins, roundLosses, standing);
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendFinalStandings\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p1.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Wins>"+wins[0]+"</Wins>"+"\n"+
				"<Losses>"+losses[0]+"</Losses>"+"\n"+
				"<RoundWins>"+roundWins[0]+"</RoundWins>"+"\n"+
				"<RoundLosses>"+roundLosses[0]+"</RoundLosses>"+"\n"+
				"<Standing>"+standing[0]+"</Standing>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p2.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Wins>"+wins[1]+"</Wins>"+"\n"+
				"<Losses>"+losses[1]+"</Losses>"+"\n"+
				"<RoundWins>"+roundWins[1]+"</RoundWins>"+"\n"+
				"<RoundLosses>"+roundLosses[1]+"</RoundLosses>"+"\n"+
				"<Standing>"+standing[1]+"</Standing>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p3.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Wins>"+wins[2]+"</Wins>"+"\n"+
				"<Losses>"+losses[2]+"</Losses>"+"\n"+
				"<RoundWins>"+roundWins[2]+"</RoundWins>"+"\n"+
				"<RoundLosses>"+roundLosses[2]+"</RoundLosses>"+"\n"+
				"<Standing>"+standing[2]+"</Standing>"+"\n"+
				"</Player>"+"\n"+
				"<Player>"+"\n"+
				"<PlayerId>"+p4.getUUID().toString()+"</PlayerId>"+"\n"+
				"<Wins>"+wins[3]+"</Wins>"+"\n"+
				"<Losses>"+losses[3]+"</Losses>"+"\n"+
				"<RoundWins>"+roundWins[3]+"</RoundWins>"+"\n"+
				"<RoundLosses>"+roundLosses[3]+"</RoundLosses>"+"\n"+
				"<Standing>"+standing[3]+"</Standing>"+"\n"+
				"</Player>"+"\n"+			
				"</command>"+"\n";

		assertEquals(expected ,xml);
	}

	/**
	 * Tests that the send round timer amount xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendRoundTimerAmount() 
	{
		//Test sending time through single time string
		tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tid, "10:23:45");
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendRoundTimerAmount\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Time>"+"10:23:45"+"</Time>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected ,xml);

		//Test sending time through hh mm ss
		tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tid,10,23,45);
		xml = tournament.bridge.getLastXML();  

		expected  = "<command type=\"sendRoundTimerAmount\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Time>"+"10:23:45"+"</Time>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected ,xml);
	}

	/**
	 * Tests that the send tournament name xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendTournamentName() 
	{
		//Test sending the tournament name
		tournament.getOutgoingCommandHandler().handleSendTournamentName(tid, "Tournament1");
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendTournamentName\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Name>"+"Tournament1"+"</Name>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected ,xml);

	}


	/**
	 * Tests that the send error xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendError() 
	{
		//Test sending an error
		tournament.getOutgoingCommandHandler().handleSendError(tid, p1.getUUID().toString(), "NullPointer", "r is null");
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendError\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<PlayerId>"+p1.getUUID().toString()+"</PlayerId>"+"\n"+
				"<ErrorName>"+"NullPointer"+"</ErrorName>"+"\n"+
				"<ErrorMessage>"+"r is null"+"</ErrorMessage>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected ,xml);

	}

	/**
	 * Tests that the send begin new round xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendBeginNewRound() 
	{
		//Test sending a valid round
		tournament.getOutgoingCommandHandler().handleSendBeginNewRound(tid, 1);
		String xml = tournament.bridge.getLastXML();  

		String expected  = "<command type=\"sendBeginNewRound\">"+"\n"+
				"<id>"+tid+"</id>"+"\n"+
				"<Round>"+1+"</Round>"+"\n"+
				"</command>"+"\n";

		assertEquals(expected ,xml);

		//test sending invalid round
		tournament.bridge.clearLastMessage();

		tournament.getOutgoingCommandHandler().handleSendBeginNewRound(tid, 0);
		xml = tournament.bridge.getLastXML();  

		assertNull(xml);

		tournament.bridge.clearLastMessage();

		tournament.getOutgoingCommandHandler().handleSendBeginNewRound(tid, -1);
		xml = tournament.bridge.getLastXML();  

		assertNull(xml);

	}

}
