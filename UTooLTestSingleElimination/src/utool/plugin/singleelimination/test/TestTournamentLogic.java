package utool.plugin.singleelimination.test;

import java.util.ArrayList;

import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.StandingsGeneratorSE;
import utool.plugin.singleelimination.TournamentLogic;
import utool.plugin.singleelimination.communications.OutgoingCommandHandler;
import utool.plugin.singleelimination.participant.SingleEliminationPartTournament;
import junit.framework.TestCase;

/**
 * Tests the tournament Logic class to ensure its operations work as intended
 * @author waltzm
 * @version 12/6/2012
 */
public class TestTournamentLogic  extends TestCase
{
	/**
	 * unique tournaemnt id
	 */
	long tid = 34551;

	/**
	 * holds the tournament object used in the tests
	 */
	TournamentLogic t;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		//create a tournament
		t = TournamentLogic.getInstance(tid, null, null, Player.PARTICIPANT);
	}

	/**
	 * tests getting of the tournament id
	 */
	public void testGetId()
	{
		assertEquals(tid,t.getTournamentId());
	}

	/**
	 * tests egtter and setter for tournaemnt name
	 */
	public void testGetSetName()
	{
		assertEquals(null,t.getTournamentName(false));

		t.setTournamentName("Tourny 1");

		assertEquals("Tourny 1", t.getTournamentName(false));

		//TODO test with true param
	}

	/**
	 * tests getting of the tournament id
	 */
	public void testGetSetPlayers()
	{
		assertEquals(0,t.getPlayers().size());
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player("Tom"));

		t.setPlayers(players);

		assertEquals(players, t.getPlayers());

	}


	/**
	 * Tests the retrieval of the standigns gen
	 */
	public void getStandingsGen()
	{
		StandingsGeneratorSE gen = t.getStandingsGenerator();
		assertNotNull(gen);

		assertEquals(gen, t.getStandingsGenerator());
	}

	/**
	 * tests the retrieval of the OCH
	 */
	public void testOCH()
	{
		OutgoingCommandHandler och = t.getOutgoingCommandHandler();
		assertNotNull(och);

		assertEquals(och, t.getOutgoingCommandHandler());
	}

	/**
	 * tests the get instance method with 1 parameter
	 */
	public void testGetInstance1param()
	{
		//make sure first tourny is ceated
		assertEquals(t, TournamentLogic.getInstance(tid));
		//test tourny not created if invalid id
		assertEquals(null, TournamentLogic.getInstance(238293));
		//make sure tourny was not created
		assertEquals(null, TournamentLogic.getInstance(238293));

		//test instance variables
		assertEquals(t.getTournamentId(), tid);
		assertEquals(t.getTournamentName(false), null);
		assertNotNull(t.getOutgoingCommandHandler());
		assertNotNull(t.getStandingsGenerator());
		assertEquals(t.getPlayers().size(), 0);

	}

	/**
	 * tests the get instance method with 2 parameters
	 */
	public void testGetInstance2param()
	{
		long id = 2349;
		TournamentLogic t =TournamentLogic.getInstance(id, null);
		//make sure first tourny is ceated
		assertNotNull(t);

		//test instance variables
		assertEquals(t.getTournamentId(), id);
		assertEquals(t.getTournamentName(false), null);
		assertNotNull(t.getOutgoingCommandHandler());
		assertNotNull(t.getStandingsGenerator());
		assertEquals(t.getPlayers().size(), 0);

		ArrayList<Matchup> matchups = new ArrayList<Matchup>();
		long id2 = 4534534;
		TournamentLogic tn =TournamentLogic.getInstance(id2, matchups);
		//make sure first tourny is ceated
		assertNotNull(tn);

		//test instance variables
		assertEquals(tn.getTournamentId(), id2);
		assertEquals(tn.getTournamentName(false), null);
		assertNotNull(tn.getOutgoingCommandHandler());
		assertNotNull(tn.getStandingsGenerator());
		assertEquals(tn.getPlayers().size(), 0);
	}

	/**
	 * tests the get instance method with 4 parameters
	 */
	public void testGetInstance4param()
	{
		long id = 234235;
		TournamentLogic t =TournamentLogic.getInstance(id, null, null, Player.HOST);
		//make sure first tourny is ceated
		assertNotNull(t);

		//test instance variables
		assertEquals(t.getTournamentId(), id);
		assertEquals(t.getTournamentName(false), null);
		assertNotNull(t.getOutgoingCommandHandler());
		assertNotNull(t.getStandingsGenerator());
		assertEquals(t.getPlayers().size(), 0);
		assertEquals(true, t instanceof SingleEliminationTournament);

		ArrayList<Matchup> matchups = new ArrayList<Matchup>();
		ArrayList<Player> playerz = new ArrayList<Player>();
		playerz.add(new Player("R"));
		long id2 = 798098098;
		TournamentLogic tn =TournamentLogic.getInstance(id2,playerz, matchups, Player.PARTICIPANT );
		//make sure first tourny is ceated
		assertNotNull(tn);

		//test instance variables
		assertEquals(tn.getTournamentId(), id2);
		assertEquals(tn.getTournamentName(false), null);
		assertNotNull(tn.getOutgoingCommandHandler());
		assertNotNull(tn.getStandingsGenerator());
		assertEquals(tn.getPlayers(), playerz);
		assertEquals(true, tn instanceof SingleEliminationPartTournament);
	}



}
