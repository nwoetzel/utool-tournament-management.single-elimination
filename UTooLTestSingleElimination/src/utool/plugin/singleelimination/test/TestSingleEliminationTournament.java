package utool.plugin.singleelimination.test;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;
import junit.framework.TestCase;

/**
 * Test cases for the SingleEliminationTournament class
 * @author hoguet
 * 
 * 10-28-12
 *
 */
public class TestSingleEliminationTournament extends TestCase {

	/**
	 * unique tournament id to use
	 */
	private long tournamentId = 0;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		SingleEliminationTournament.clearInstance(tournamentId);
	}
	
	public void testExpandBracket(){
		
		ArrayList<Player> players = createNewPlayerList(4);
		SingleEliminationTournament set = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, null);
		ArrayList<Matchup> matchups = SingleEliminationTournament.generateRandomMatchups(players, set);
		set.setPermissionLevel(Player.HOST);
		set.setMatchups(matchups);
		set.startTournament();
	
		
		assertEquals(set.getRound(), 1);
		
		for(Matchup m : set.getCurrentRound()){
			if(!m.containsPlayer(new Player(Player.BYE, "BYE"))){
				m.setScores(2,0);
			}
		}
		
		assertEquals(set.getRound(), 2);
		
		set.expandBracket(new Player("new1"));
		
		for(Matchup m : set.getCurrentRound()){
			if(m.getPlayerOne() != null && m.getPlayerTwo() != null && !m.containsPlayer(new Player(Player.BYE, "BYE"))){
				m.setScores(2,0);
			}
		}
		
		assertEquals(set.getRound(), 3);
		
		set.expandBracket(new Player("new2"));
		
		for(Matchup m : set.getCurrentRound()){
			if(!m.containsPlayer(new Player(Player.BYE, "BYE"))){
				m.setScores(2,0);
			}
		}
		
		assertEquals(set.getRound(), 4);
		
		
		
		
	}
	
	private ArrayList<Player> createNewPlayerList(int size){
		ArrayList<Player> toReturn = new ArrayList<Player>();
		for(int i = 1; i <= size; i++){
			toReturn.add(new Player("Player "+i));
		}
		return toReturn;
	}

	/**
	 * Tests the clearInstance and getInstance methods for SingleEliminationTournament
	 */
	public void testInstance(){

		//No arg getInstance should return null if tournament hasn't already been created
		assertEquals(SingleEliminationTournament.getInstance(tournamentId), null);

		ArrayList<Player> players = new ArrayList<Player>();

		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		Player four = new Player("four");
		Player five = new Player("five");

		players.add(one);
		players.add(two);
		players.add(three);
		players.add(four);
		players.add(five);

		//test matchups-only ctor		
		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		ArrayList<Matchup> theMatchups = SingleEliminationTournament.generateRandomMatchups(players, set);
		set.setMatchups(theMatchups);

		//		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, theMatchups);

		//The players that were in the matchups should have gotten initialized
		List<Player> tournamentPlayers = set.getPlayers();
		assertEquals(tournamentPlayers.size(), 5);
		assertTrue(tournamentPlayers.contains(one));
		assertTrue(tournamentPlayers.contains(two));
		assertTrue(tournamentPlayers.contains(three));
		assertTrue(tournamentPlayers.contains(four));
		assertTrue(tournamentPlayers.contains(five));

		//Make sure matchups were set and that tournament was set for each matchup
		assertEquals(set.getMatchups(), theMatchups);
		for(Matchup m : set.getMatchups()){
			assertEquals(m.getTournament(), set);
		}

		//add a player to the player list but use the same matchups and re-create with the 2 arg ctor
		Player six = new Player("six");
		players.add(six);

		SingleEliminationTournament.clearInstance(tournamentId);

		set = (SingleEliminationTournament) SingleEliminationTournament.getInstance(tournamentId, players, theMatchups, Player.HOST);

		//all players should be in the players list
		tournamentPlayers = set.getPlayers();
		assertEquals(tournamentPlayers.size(), 6);
		assertTrue(tournamentPlayers.contains(one));
		assertTrue(tournamentPlayers.contains(two));
		assertTrue(tournamentPlayers.contains(three));
		assertTrue(tournamentPlayers.contains(four));
		assertTrue(tournamentPlayers.contains(five));
		assertTrue(tournamentPlayers.contains(six));

		assertEquals(set.getMatchups(), theMatchups);
		for(Matchup m : set.getMatchups()){
			assertEquals(m.getTournament(), set);
		}

	}

	/**
	 * Tests the addPlayer(Player p) method.
	 */
	public void testAddPlayer(){

		ArrayList<Player> players = new ArrayList<Player>();
		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		Player four = new Player("four");
		Player five = new Player("five");

		players.add(one);
		players.add(two);
		players.add(three);

		assertEquals(players.size(), 3);

		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		set.setMatchups(SingleEliminationTournament.generateRandomMatchups(players, set));

		assertEquals(set.getPlayers().size(), 3);

		//Add two more players
		set.addPlayer(four);
		set.addPlayer(five);

		//Ensure that all added players made it into the tournament's player list
		//		assertEquals(set.getPlayers().size(), 5);
		assertTrue(set.getPlayers().contains(one));
		assertTrue(set.getPlayers().contains(two));
		assertTrue(set.getPlayers().contains(three));
		assertTrue(set.getPlayers().contains(four));
		assertTrue(set.getPlayers().contains(five));

		//Ensure that correct matchups were added to accomodate
		assertEquals(set.getMatchups().size(), 7);
		assertEquals(set.getBottomRound().size(), 4);



	}

	/**
	 * Test the getBottomRound and getCurrentRound methods
	 */
	public void testGetBottomAndCurrentRound(){

		//Create matchups for a 3 round tournament
		ArrayList<Matchup> matchups = new ArrayList<Matchup>();

		Matchup finalM = new Matchup(null, null);
		Matchup semifinalOne = new Matchup(finalM, null);
		Matchup semifinalTwo = new Matchup(finalM, null);		
		Matchup quarterfinalOne = new Matchup(new Player("one"), new Player("eight"), semifinalOne, null);
		Matchup quarterfinalTwo = new Matchup(new Player("two"), new Player("five"), semifinalOne, null);
		Matchup quarterfinalThree = new Matchup(new Player("three"), new Player("six"), semifinalTwo, null);
		Matchup quarterfinalFour = new Matchup(new Player("four"), new Player("seven"), semifinalTwo, null);

		matchups.add(finalM);
		matchups.add(semifinalOne);
		matchups.add(semifinalTwo);
		matchups.add(quarterfinalOne);
		matchups.add(quarterfinalTwo);
		matchups.add(quarterfinalThree);
		matchups.add(quarterfinalFour);

		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, matchups);

		//Bottom round should contain the 4 quarterfinals
		ArrayList<Matchup> bottomRound = set.getBottomRound();
		assertEquals(bottomRound.size(), 4);
		assertTrue(bottomRound.contains(quarterfinalOne));
		assertTrue(bottomRound.contains(quarterfinalTwo));
		assertTrue(bottomRound.contains(quarterfinalThree));
		assertTrue(bottomRound.contains(quarterfinalFour));

		//At this point, current round should be the same as bottom round
		ArrayList<Matchup> curRound = set.getCurrentRound();
		assertEquals(curRound.size(), 4);
		assertTrue(curRound.contains(quarterfinalOne));
		assertTrue(curRound.contains(quarterfinalTwo));
		assertTrue(curRound.contains(quarterfinalThree));
		assertTrue(curRound.contains(quarterfinalFour));

		set.startTournament();
		set.advanceRound();

		//Bottom round should still contain the 4 quarterfinals
		bottomRound = set.getBottomRound();
		assertEquals(bottomRound.size(), 4);
		assertTrue(bottomRound.contains(quarterfinalOne));
		assertTrue(bottomRound.contains(quarterfinalTwo));
		assertTrue(bottomRound.contains(quarterfinalThree));
		assertTrue(bottomRound.contains(quarterfinalFour));

		//At this point, current round should be the 2 semifinals
		curRound = set.getCurrentRound();
		assertEquals(curRound.size(), 2);
		assertTrue(curRound.contains(semifinalOne));
		assertTrue(curRound.contains(semifinalTwo));	


	}


	/**
	 * Tests the initializeMatchups(ArrayList<Matchup> matchups) method
	 */
	public void testInitializeMatchups(){

		ArrayList<Matchup> matchups = new ArrayList<Matchup>();
		Matchup parent = new Matchup(null, null);
		matchups.add(new Matchup(new Player("one"), new Player("two"), parent, null));
		matchups.add(new Matchup(new Player("three"), new Player("four"), parent, null));
		matchups.add(parent);

		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, matchups);
		set.setMatchups(matchups);

		ArrayList<Matchup> initializedMatchups = set.getMatchups();

		//Ensure that tournament has the 3 matchups that were passed in
		assertEquals(initializedMatchups.size(), 3);

		for(Matchup m : matchups){
			assertTrue(initializedMatchups.contains(m));
		}

		ArrayList<Matchup> newMatchups = new ArrayList<Matchup>();
		Matchup newParent = new Matchup(null, null);
		newMatchups.add(new Matchup(new Player("five"), new Player("six"), newParent, null));
		newMatchups.add(new Matchup(new Player("seven"), new Player("eight"), newParent, null));
		newMatchups.add(newParent);

		set.setMatchups(newMatchups);

		initializedMatchups = set.getMatchups();

		//Ensure that the tournament replaced the old matchups with the new initializations
		assertEquals(initializedMatchups.size(), 3);

		for(Matchup m : newMatchups){
			assertTrue(initializedMatchups.contains(m));
		}

		//TODO not critical for current use but consider enforcing that matchups only contain players in the tournament's player list	

	}

	/**
	 * Tests the initializePlayers(ArrayList<Player> players) method
	 */
	public void testInitializePlayers(){

		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player("one"));
		players.add(new Player("two"));
		players.add(new Player("three"));
		players.add(new Player("four"));
		players.add(new Player("five"));

		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		set.setMatchups(SingleEliminationTournament.generateRandomMatchups(players, null));
		set.setPlayers(players);

		List<Player> initializedPlayers = set.getPlayers();

		//Ensure that the tournament's player list is the same as the input list by checking that they have the same size,
		//and that each player has made it into the tournament's player list
		assertTrue(initializedPlayers.size() == players.size());

		for(Player p : players){
			assertTrue(initializedPlayers.contains(p));
		}

		//Ensure that initializing players with a new list replaces the old
		ArrayList<Player> newPlayers = new ArrayList<Player>();
		newPlayers.add(new Player("six"));
		newPlayers.add(new Player("seven"));
		newPlayers.add(new Player("eight"));
		newPlayers.add(new Player("nine"));
		newPlayers.add(new Player("ten"));
		newPlayers.add(new Player("eleven"));

		set.setPlayers(newPlayers);

		initializedPlayers = set.getPlayers();

		assertTrue(initializedPlayers.size() == newPlayers.size());

		for(Player p : newPlayers){
			assertTrue(initializedPlayers.contains(p));
		}


	}


	/**
	 * Test remove player method
	 */
	public void testRemovePlayer()
	{
		//long tournamentId = 434345345;
		ArrayList<Player> players = new ArrayList<Player>();
		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		Player four = new Player("four");
		Player five = new Player("five");
		Player six = new Player("six");

		players.add(one);
		players.add(two);
		players.add(three);
		players.add(four);
		players.add(five);
		players.add(six);
		TournamentLogic.clearInstance(tournamentId);
		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, players,null, Player.HOST);
		set.setMatchups(SingleEliminationTournament.generateRandomMatchups(players, set));

		ArrayList<Matchup> matchupsCopy = new ArrayList<Matchup>();
		for(Matchup m : set.getMatchups()){
			matchupsCopy.add(m);
		}


		//Remove sixth player
		set.removePlayer(six);
		assertFalse(set.getPlayers().contains(six));

		//Ensure that matchups were not added or removed
		assertEquals(matchupsCopy.size(), set.getMatchups().size());
		for(Matchup m : matchupsCopy){
			assertTrue(set.getMatchups().contains(m));
		}

		//Remove two more players
		set.removePlayer(two);
		set.removePlayer(four);
		assertFalse(set.getPlayers().contains(two));
		assertFalse(set.getPlayers().contains(four));

		//Ensure that matchups Were removed.  Should only be three remaining. (1 vs 2, 3 vs null, and null vs null)
		assertEquals(set.getMatchups().size(), 3);	


	}

	/**
	 * Test start tournament method
	 */
	public void testStartTournament(){		
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player("one"));
		players.add(new Player("two"));
		players.add(new Player("three"));
		players.add(new Player("four"));
		players.add(new Player("five"));
		players.add(new Player("six"));


		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		set.setMatchups(SingleEliminationTournament.generateRandomMatchups(players, null));

		assertEquals(set.getMatchups().size(), 7);

		ArrayList<Matchup> bottomRound = set.getBottomRound();

		//Check bottom round for null player slots
		int nullCounter = 0;
		for(Matchup m : bottomRound){
			if(m.getPlayerOne() == null||m.getPlayerOne().getUUID().equals(Player.BYE)){
				nullCounter++;
			}
			if(m.getPlayerTwo() == null||m.getPlayerTwo().getUUID().equals(Player.BYE)){
				nullCounter++;
			}
		}

		assertEquals(nullCounter, 2);

		set.startTournament();

		bottomRound = set.getBottomRound();

		for(Matchup m : bottomRound){

			//After starting, winners should be set to opposite of nulls in bottom round
			if(m.getPlayerOne() == null){				
				assertEquals(m.getWinner(), m.getPlayerTwo());				
			}
			if(m.getPlayerTwo() == null){
				assertEquals(m.getWinner(), m.getPlayerOne());
			}




		}


	}

	/**
	 * Tests the generateRandomMatchups() method
	 * Depends on correct behavior of initializePlayers, initializeMatchups, Matchup.getPlayerOne and Matchup.getPlayerTwo
	 */
	public void testGenerateRandomMatchups(){

		//Test handling of null argument
		ArrayList<Matchup> generatedMatchups = SingleEliminationTournament.generateRandomMatchups(null, null);		
		assertEquals(generatedMatchups, null);

		//Test "ideal" case of 8 players
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player("one"));
		players.add(new Player("two"));
		players.add(new Player("three"));
		players.add(new Player("four"));
		players.add(new Player("five"));
		players.add(new Player("six"));
		players.add(new Player("seven"));
		players.add(new Player("eight"));

		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);		
		generatedMatchups = SingleEliminationTournament.generateRandomMatchups(players, set);

		//There should be 7 matchups
		assertEquals(generatedMatchups.size(), 7);

		//All players should be in a matchup
		ArrayList<Player> matchedPlayers = new ArrayList<Player>();
		for(Matchup m : generatedMatchups){

			if(m.getPlayerOne() != null){
				matchedPlayers.add(m.getPlayerOne());
			}

			if(m.getPlayerTwo() != null){
				matchedPlayers.add(m.getPlayerTwo());
			}						
		}

		for(Player p : players){
			assertTrue(matchedPlayers.contains(p));
		}

		//Instantiate a tournament object to get bottom round
		//		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, generatedMatchups);
		set.setMatchups(generatedMatchups);

		for(Matchup m : set.getBottomRound()){
			assertTrue(m.getPlayerOne() != null);
			assertTrue(m.getPlayerTwo() != null);
		}



		//Test odd number of players
		players.add(new Player("nine"));
		players.add(new Player("ten"));

		generatedMatchups = SingleEliminationTournament.generateRandomMatchups(players, null);

		//There should be 15 matchups
		assertEquals(generatedMatchups.size(), 15);

		//All players should be in a matchup
		matchedPlayers = new ArrayList<Player>();
		for(Matchup m : generatedMatchups){

			if(m.getPlayerOne() != null){
				matchedPlayers.add(m.getPlayerOne());
			}

			if(m.getPlayerTwo() != null){
				matchedPlayers.add(m.getPlayerTwo());
			}						
		}

		for(Player p : players){
			assertTrue(matchedPlayers.contains(p));
		}

		//Bottom round should contain 2 full matchups and 6 single player matchups.

		//Initialize tournament to get bottom round
		set.setPlayers(players);
		set.setMatchups(generatedMatchups);

		int nullCounter = 0;
		for(Matchup m : set.getBottomRound())
		{
			if(m.getPlayerOne() == null||(m.getPlayerOne().getUUID().equals(Player.BYE)))
			{
				nullCounter++;
			}
			if(m.getPlayerTwo() == null||(m.getPlayerTwo().getUUID().equals(Player.BYE)))
			{
				nullCounter++;
			}
		}

		assertEquals(nullCounter, 6);		

	}

	public void testValidateMatchups(){

		ArrayList<Player> players = new ArrayList<Player>();
		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		Player four = new Player("four");
		Player five = new Player("five");
		Player six = new Player("six");

		players.add(one);
		players.add(two);
		players.add(three);
		players.add(four);
		players.add(five);
		players.add(six);		

		ArrayList<Matchup> matchups = new ArrayList<Matchup>();
		Matchup finalM = new Matchup(null, null, null, null);

		Matchup semifinalOne = new Matchup(null, null, finalM, null);
		Matchup semifinalTwo = new Matchup(null, null, finalM, null);

		Matchup quarterfinalOne = new Matchup(one, two, semifinalOne, null);
		Matchup quarterfinalTwo = new Matchup(three, four, semifinalOne, null);
		Matchup quarterfinalThree = new Matchup(five, six, semifinalTwo, null);
		Matchup quarterfinalFour = new Matchup(null, null, semifinalTwo, null);

//		only add Qfinals to matchups list to check validity of
		matchups.add(quarterfinalOne);
		matchups.add(quarterfinalTwo);
		matchups.add(quarterfinalThree);
		matchups.add(quarterfinalFour);

		//these matchups should be invalid; there is a null vs null
		assertFalse(SingleEliminationTournament.validateMatchups(matchups));

		assertTrue(quarterfinalFour.addPlayer(six));

		//matchups should still be false because player six is in two matchups
		assertFalse(SingleEliminationTournament.validateMatchups(matchups));

		assertTrue(quarterfinalThree.removePlayer(six));

		//Should be valid now
		assertTrue(SingleEliminationTournament.validateMatchups(matchups));		

	}

	public void testRoundTimer(){
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player("one"));
		players.add(new Player("two"));
		players.add(new Player("three"));
		players.add(new Player("four"));
		players.add(new Player("five"));
		players.add(new Player("six"));

		SingleEliminationTournament set = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		ArrayList<Matchup> generatedMatchups = SingleEliminationTournament.generateRandomMatchups(players, set);
		set.setMatchups(generatedMatchups);

		//verify that no timer is set at creation
		assertEquals(set.getRoundDuration(), SingleEliminationTournament.DEFAULT_NO_TIMER);

		Handler h = new Handler();
		set.setRoundTimer(65, h);

		//verify correct timer parameters set
		assertEquals(set.getRoundDuration(), 65);
		assertEquals(set.getMinutes(), 1);
		assertEquals(set.getSeconds(), 5);

		set.startTournament();

		assertFalse(set.getSeconds() == 5); //verify that timer has started counting down.  if it is, that means the round timer is firing

		set.pauseTimer(true); //pause the timer

		//grab the sec and min
		int seconds = set.getSeconds();
		int minutes = set.getMinutes();

		//assert that they are still true; eg, the timer has stopped counting down
		assertEquals(set.getSeconds(), seconds);
		assertEquals(set.getMinutes(), minutes);

		set.pauseTimer(false); //unpause the timer

		try{
			Thread.sleep(2000);
		}catch(Exception e){
		}
		
		//verify that timer has started counting down again
		assertFalse(set.getSeconds() == seconds);

		//end tournament to stop timer thread
		set.endTournament();

	}



}
