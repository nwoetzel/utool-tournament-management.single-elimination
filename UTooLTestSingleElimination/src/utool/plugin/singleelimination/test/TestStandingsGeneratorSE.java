package utool.plugin.singleelimination.test;

import java.util.ArrayList;
import java.util.UUID;
import junit.framework.TestCase;
import utool.plugin.Player;
import utool.plugin.singleelimination.PlayerNotExistantException;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.StandingsGeneratorSE;
import utool.plugin.singleelimination.TournamentLogic;


/**
 * This test class is meant to fully test the functionality of the StandingsGeneratorSE.java class
 * @author Maria
 * @version 10/13/2012
 */
public class TestStandingsGeneratorSE extends TestCase{

	/**
	 * unique tournament id
	 */
	private long tournamentId = 0;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		SingleEliminationTournament.clearInstance(tournamentId);
	}

	/**
	 * Tests that the various components are initialized properly
	 * @since 10/13/2012
	 */
	public void testConstructorAndGetInstance() 
	{		

		//Check that the getInstance works as intended and returns the same reference at later times
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);

		StandingsGeneratorSE gen = t.getStandingsGenerator();

		assertNotNull(gen);
		//clear gen
		gen.resetScores();

		//assertEquals(StandingsGeneratorSE.getInstance(plyrs), gen);

		//Check that participants has the expected player list
		for(int i=0;i<plyrs.size();i++)
		{
			//check id exists in participants
			assertNotSame(gen.getPlayerStanding(plyrs.get(i).getUUID()),-1);	
		}


		//Check that players has the expected player list
		assertEquals(gen.getPlayers().size(), plyrs.size());

	}



	/**
	 * Tests that the add and remove player function as intended
	 * @since 10/13/2012
	 */
	public void testAddRemovePlayer() 
	{		
		UUID player1id = new UUID(0,345);
		UUID player2id = new UUID(0,678);

		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);

		StandingsGeneratorSE gen = t.getStandingsGenerator();
		//Check that round is correctly updated after going to next round
		gen.resetScores();

		//Check that the add player by id and name works as intended
		int players = gen.getPlayers().size();
		gen.addPlayer(player1id, "Tommy");
		assertEquals(players+1, gen.getPlayers().size());
		assertNotSame(gen.getPlayerStanding(player1id),-1);

		//Check that the add Player by passed in Player works as intended
		players = gen.getPlayers().size();
		gen.addPlayer(new Player(player2id, "Bobby"));
		assertEquals(players+1, gen.getPlayers().size());
		assertNotSame(gen.getPlayerStanding(player2id),-1);

		//check that you can't add multiple of the same id
		players = gen.getPlayers().size();
		gen.addPlayer(player1id, "Don");
		assertEquals(players, gen.getPlayers().size());
		assertNotSame(gen.getPlayerStanding(player1id),-1);

		players = gen.getPlayers().size();
		gen.addPlayer(player2id, "Don");
		assertEquals(players, gen.getPlayers().size());
		assertNotSame(gen.getPlayerStanding(player2id),-1);

		//Check that the removePlayer by passed in player works as intended
		players = gen.getPlayers().size();
		assertTrue(gen.removePlayer(new Player(player1id,"randy")));
		assertEquals(players-1, gen.getPlayers().size());
		assertSame(gen.getPlayerStanding(player1id),-1);

		//Check that the removePlayer by id works as intended
		players = gen.getPlayers().size();
		assertTrue(gen.removePlayer(player2id));
		assertEquals(players-1, gen.getPlayers().size());
		assertSame(gen.getPlayerStanding(player1id),-1);

		//check that you can't remove a removed player (returns false)
		players = gen.getPlayers().size();
		assertFalse(gen.removePlayer(player2id));
		assertEquals(players, gen.getPlayers().size());

		players = gen.getPlayers().size();
		assertFalse(gen.removePlayer(new Player(player1id,"randy")));
		assertEquals(players, gen.getPlayers().size());
	}

	/**
	 * Tests that the record scores works
	 * @throws PlayerNotExistantException shouldn't throw
	 * @since 10/13/2012
	 */
	public void testRecordScore() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);

		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//Check that round is correctly updated after going to next round
		gen.resetScores();

		//add a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		assertEquals(gen.getPlayerWins(new UUID(0,0), 1), 3d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 1), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 1), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 1), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 1), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 1), 3d);


		//Check that it fails if the player doesn't exist
		try
		{
			gen.recordScore(new UUID(0,50), new UUID(0,0), 1, 1, 0,1);
			fail();//should have thrown the exception

		}
		catch(PlayerNotExistantException e)
		{
			assertEquals(e.getMessage(),"One of the Players is not in the Standings Generator list of players");
		}

		try
		{
			assertFalse(gen.recordScore(new UUID(0,0), new UUID(0,50), 1, 1, 0,1));
			fail();//should have thrown the exception

		}
		catch(PlayerNotExistantException e)
		{
			assertEquals(e.getMessage(),"One of the Players is not in the Standings Generator list of players");
		}



		//Check that it succeeds if one of the players is a bye
		gen.resetScores();
		assertTrue(gen.recordScore(new UUID(0,0), Player.BYE, 1, 3, 2,1));
		assertEquals(gen.getPlayerWins(new UUID(0,0), 1), 3d);

		assertTrue(gen.recordScore(Player.BYE, new UUID(0,1), 1, 2, 3,1));
		assertEquals(gen.getPlayerWins(new UUID(0,1), 1), 3d);

		//check that it fails with incorrect player ids

		try
		{
			assertFalse(gen.recordScore(new UUID(0,57), new UUID(0,-1), 1, 3, 0,1));
		}
		catch(PlayerNotExistantException e)
		{
			assertEquals(e.getMessage(),"One of the Players is not in the Standings Generator list of players");//TODO failing
		}

		try
		{
			assertFalse(gen.recordScore(new UUID(0,-1), new UUID(0,57), 1, 3, 0,1));
		}
		catch(PlayerNotExistantException e)
		{
			assertEquals(e.getMessage(),"One of the Players is not in the Standings Generator list of players");//TODO failing
		}

	}

	/**
	 * Tests that the setting scores out of round order works
	 * @throws PlayerNotExistantException shouldn't throw
	 * @since 1/10/2013
	 */
	public void testOutOfOrderScore() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);

		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//Check that round is correctly updated after going to next round
		gen.resetScores();

		//add half of a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,2);
		//	gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,3);
		//	gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,4);

		//check for correct wins
		assertEquals(gen.getPlayerWins(new UUID(0,0), 1), 3d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 1), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 1), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 1), 0d);


		//add round two before rest of round 1 finishes
		assertTrue(gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 2, 5));

		//check for correct wins
		assertEquals(gen.getPlayerWins(new UUID(0,0), 2), 6d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 2), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 2), 4d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 2), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 1), 0d);

		//check for correct standings
		assertEquals(gen.getPlayerStanding(new UUID(0,0)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,1)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,2)), 3);
		assertEquals(gen.getPlayerStanding(new UUID(0,3)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,4)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,5)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,6)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,7)), 1);
	}

	/**
	 * Tests that the get player standings
	 * @throws PlayerNotExistantException should not throw
	 * @since 10/13/2012
	 */
	public void testGetPlayerStandings() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//reset scores
		gen.resetScores();




		//Check that the standings returned are correct for each round
		//pre round 1
		assertEquals(gen.getPlayerStanding(new UUID(0,0)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,1)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,2)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,3)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,4)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,5)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,6)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,7)), 1);

		//round 1
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		assertEquals(gen.getPlayerStanding(new UUID(0,0)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,1)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,2)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,3)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,4)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,5)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,6)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,7)), 1);

		//round 2
		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);

		assertEquals(gen.getPlayerStanding(new UUID(0,0)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,1)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,2)), 3);
		assertEquals(gen.getPlayerStanding(new UUID(0,3)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,4)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,5)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,6)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,7)), 3);

		//round 3
		gen.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);

		assertEquals(gen.getPlayerStanding(new UUID(0,0)), 1);
		assertEquals(gen.getPlayerStanding(new UUID(0,1)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,2)), 3);
		assertEquals(gen.getPlayerStanding(new UUID(0,3)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,4)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,5)), 2);
		assertEquals(gen.getPlayerStanding(new UUID(0,6)), 4);
		assertEquals(gen.getPlayerStanding(new UUID(0,7)), 3);

		//Check that it returns -1 if the player does not exist
		assertEquals(gen.getPlayerStanding(new UUID(0,50)), -1);

	}

	/**
	 * Tests that the test player wins works
	 * @throws PlayerNotExistantException should not throw
	 * @since 10/13/2012
	 */
	public void testGetPlayerWins() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//reset scores
		gen.resetScores();

		//Check that the wins returned are correct for each round
		//pre round 1
		assertEquals(gen.getPlayerWins(new UUID(0,0), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 1), 0d);

		//add a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		assertEquals(gen.getPlayerWins(new UUID(0,0), 1), 3d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 1), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 1), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 1), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 1), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 1), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 1), 3d);

		//round 2
		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);

		assertEquals(gen.getPlayerWins(new UUID(0,0), 2), 6d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 2), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 2), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 2), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 2), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 2), 4d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 2), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 2), 4d);

		//round 3
		gen.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);

		assertEquals(gen.getPlayerWins(new UUID(0,0), 3), 9d);
		assertEquals(gen.getPlayerWins(new UUID(0,1), 3), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,2), 3), 2d);
		assertEquals(gen.getPlayerWins(new UUID(0,3), 3), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,4), 3), 1d);
		assertEquals(gen.getPlayerWins(new UUID(0,5), 3), 4d);
		assertEquals(gen.getPlayerWins(new UUID(0,6), 3), 0d);
		assertEquals(gen.getPlayerWins(new UUID(0,7), 3), 4d);

		//Check that it returns -1 if the player does not exist
		assertEquals(gen.getPlayerWins(new UUID(0,50), 1), -1d);

		//Check that it returns -1 if the round is in the future or less than 1
		assertEquals(gen.getPlayerWins(new UUID(0,0), -1), -1d);
		assertEquals(gen.getPlayerWins(new UUID(0,0), 0), -1d);
	}

	/**
	 * Tests that the test player losses works
	 * @throws PlayerNotExistantException should not throw
	 * @since 10/13/2012
	 */
	public void testGetPlayerLosses() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//reset scores
		gen.resetScores();

		//Check that the losses returned are correct for each round
		//pre round 1
		assertEquals(gen.getPlayerLosses(new UUID(0,0), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,1), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,2), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,3), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,4), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,5), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,6), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,7), 1), 0d);

		//round 1
		//add a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		assertEquals(gen.getPlayerLosses(new UUID(0,0), 1), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,1), 1), 3d);
		assertEquals(gen.getPlayerLosses(new UUID(0,2), 1), 1d);
		assertEquals(gen.getPlayerLosses(new UUID(0,3), 1), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,4), 1), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,5), 1), 1d);
		assertEquals(gen.getPlayerLosses(new UUID(0,6), 1), 3d);
		assertEquals(gen.getPlayerLosses(new UUID(0,7), 1), 0d);

		//round 2
		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);

		assertEquals(gen.getPlayerLosses(new UUID(0,0), 2), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,1), 2), 3d);
		assertEquals(gen.getPlayerLosses(new UUID(0,2), 2), 4d);
		assertEquals(gen.getPlayerLosses(new UUID(0,3), 2), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,4), 2), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,5), 2), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,6), 2), 3d);
		assertEquals(gen.getPlayerLosses(new UUID(0,7), 2), 2d);

		//round 3
		gen.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);

		assertEquals(gen.getPlayerLosses(new UUID(0,0), 3), 0d);
		assertEquals(gen.getPlayerLosses(new UUID(0,1), 3), 3d);
		assertEquals(gen.getPlayerLosses(new UUID(0,2), 3), 4d);
		assertEquals(gen.getPlayerLosses(new UUID(0,3), 3), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,4), 3), 2d);
		assertEquals(gen.getPlayerLosses(new UUID(0,5), 3), 5d);
		assertEquals(gen.getPlayerLosses(new UUID(0,6), 3), 3d);
		assertEquals(gen.getPlayerLosses(new UUID(0,7), 3), 2d);

		//Check that it returns -1 if the player does not exist
		assertEquals(gen.getPlayerLosses(new UUID(0,50), 1), -1d);

		//Check that it returns -1 if the round is less than 1
		assertEquals(gen.getPlayerLosses(new UUID(0,0), -1), -1d);
		assertEquals(gen.getPlayerLosses(new UUID(0,0), 0), -1d);
	}

	/**
	 * Tests that the test player round wins works
	 * @throws PlayerNotExistantException should not throw
	 * @since 10/13/2012 
	 */
	public void testGetPlayerRoundWins() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//reset scores
		gen.resetScores();

		//Check that the losses returned are correct for each round
		//pre round 1
		assertEquals(gen.getPlayerRoundWins(new UUID(0,0), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,1), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,2), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,3), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,4), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,5), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,6), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,7), 1), 0);

		//round 1
		//add a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		assertEquals(gen.getPlayerRoundWins(new UUID(0,0), 1), 1);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,1), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,2), 1), 1);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,3), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,4), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,5), 1), 1);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,6), 1), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,7), 1), 1);

		//round 2
		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);

		assertEquals(gen.getPlayerRoundWins(new UUID(0,0), 2), 2);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,1), 2), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,2), 2), 1);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,3), 2), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,4), 2), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,5), 2), 2);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,6), 2), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,7), 2), 1);

		//round 3
		gen.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);

		assertEquals(gen.getPlayerRoundWins(new UUID(0,0), 3), 3);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,1), 3), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,2), 3), 1);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,3), 3), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,4), 3), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,5), 3), 2);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,6), 3), 0);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,7), 3), 1);

		//Check that it returns -1 if the player does not exist
		assertEquals(gen.getPlayerRoundWins(new UUID(0,50), 1), -1);

		//Check that it returns -1 if the round is in the future or less than 1
		assertEquals(gen.getPlayerRoundWins(new UUID(0,0), -1), -1);
		assertEquals(gen.getPlayerRoundWins(new UUID(0,0), 0), -1);
	}

	/**
	 * Tests that the test player round losses works
	 * @throws PlayerNotExistantException should not throw
	 * @since 10/13/2012
	 */
	public void testGetPlayerRoundLosses() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//reset scores
		gen.resetScores();

		//Check that the losses returned are correct for each round
		//pre round 1
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,0), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,1), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,2), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,3), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,4), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,5), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,6), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,7), 1), 0);

		//round 1
		//add a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		assertEquals(gen.getPlayerRoundLosses(new UUID(0,0), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,1), 1), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,2), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,3), 1), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,4), 1), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,5), 1), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,6), 1), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,7), 1), 0);

		//round 2
		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);

		assertEquals(gen.getPlayerRoundLosses(new UUID(0,0), 2), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,1), 2), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,2), 2), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,3), 2), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,4), 2), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,5), 2), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,6), 2), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,7), 2), 1);

		//round 3
		gen.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);

		assertEquals(gen.getPlayerRoundLosses(new UUID(0,0), 3), 0);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,1), 3), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,2), 3), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,3), 3), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,4), 3), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,5), 3), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,6), 3), 1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,7), 3), 1);

		//Check that it returns -1 if the player does not exist
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,50), 1), -1);

		//Check that it returns -1 if the round is less than 1
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,0), -1), -1);
		assertEquals(gen.getPlayerRoundLosses(new UUID(0,0), 0), -1);


	}

	/**
	 * Tests that the reset scores works
	 * @throws PlayerNotExistantException should not throw
	 * @since 10/13/2012
	 */
	public void testResetScores() throws PlayerNotExistantException 
	{	
		//make a list of players
		ArrayList<Player> plyrs = new ArrayList<Player>();
		plyrs.add(new Player(new UUID(0,0),"bob"));
		plyrs.add(new Player(new UUID(0,1),"Tim"));
		plyrs.add(new Player(new UUID(0,2),"John"));
		plyrs.add(new Player(new UUID(0,3),"Tom"));
		plyrs.add(new Player(new UUID(0,4),"Ned"));
		plyrs.add(new Player(new UUID(0,5),"Alfred"));
		plyrs.add(new Player(new UUID(0,6),"Thomas"));
		plyrs.add(new Player(new UUID(0,7), "Shaun"));

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
		StandingsGeneratorSE gen = t.getStandingsGenerator();

		//reset scores
		gen.resetScores();
		//add a round 
		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);

		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);

		//check that round and scores reset
		gen.resetScores();
		assertEquals(gen.getPlayerStanding(new UUID(0,0)),1);
		assertEquals(gen.getPlayerStanding(new UUID(0,1)),1);
	}

	/**
	 * Tests that send final standings only sends if the round is complete
	 * @throws PlayerNotExistantException should not throw
	 */
	public void testSendFinalStandings() throws PlayerNotExistantException
	{
		ArrayList<Player> p = new ArrayList<Player>();
		Player p1 = new Player("Tim");
		Player p2 = new Player("Mike");
		p.add(p1);
		p.add(p2);

		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, p, null, Player.HOST);


		StandingsGeneratorSE gen = t.getStandingsGenerator();

		gen.recordScore(p1.getUUID(), p2.getUUID(), 1, 3, 2, 1);

		//test to make sure returns true since tournament is done
		assertTrue(gen.sendFinalStandings());


	}


}