package utool.plugin.singleelimination.test;

import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import junit.framework.TestCase;

/**
 * Test case for methods of the Matchup class
 * @author hoguet
 * 
 * 10-28-12
 *
 */
public class TestMatchup extends TestCase {
	
	
	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

	}
	
	/**
	 * Tests the three constructors for Matchups
	 */
	public void testConstructors(){
		
		//test the new Matchup(parent) ctor		
		Matchup m = new Matchup(null, null);
		
		assertTrue(m != null);
		assertTrue(m.getParent() == null);
		assertTrue(m.getPlayerOne() == null);
		assertTrue(m.getPlayerTwo() == null);
		
		Matchup parent = new Matchup(null, null);
		m = new Matchup(parent, null);
		assertTrue(m.getParent().equals(parent));
		
		//test the new Matchup(playerOne, parent) ctor
		Player one = new Player("one");
		
		m = new Matchup(one, parent, null);
		assertTrue(m.getPlayerOne().equals(one));
		assertTrue(m.getPlayerTwo() == null);
		
		//test the new Matchup(playerOne, playerTwo, parent) ctor
		Player two = new Player("two");
		
		m = new Matchup(one, two, parent, null);
		assertTrue(m.getPlayerOne().equals(one));
		assertTrue(m.getPlayerTwo().equals(two));
		
	}
	
	/**
	 * Tests the addPlayer(Player p) method
	 */
	public void testAddPlayer(){
		
		Matchup m = new Matchup(null, null);
		
		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		
		//Adding a player should result in matchup player vs null
		boolean returnedBool = m.addPlayer(one);
		assertTrue(m.getPlayerOne().equals(one));
		assertTrue(m.getPlayerTwo() == null);
		assertTrue(returnedBool);
		
		//Adding a second player should result in matchup player one vs player two
		returnedBool = m.addPlayer(two);
		assertTrue(m.getPlayerOne().equals(one));
		assertTrue(m.getPlayerTwo().equals(two));
		assertTrue(returnedBool);
		
		//Adding a third player should not change the matchup and should return false
		returnedBool = m.addPlayer(three);
		assertTrue(m.getPlayerOne().equals(one));
		assertTrue(m.getPlayerTwo().equals(two));
		assertTrue(!returnedBool);
		
	}
	
	
	/**
	 * Tests the containsPlayer(Player p) method
	 */
	public void testContainsPlayer(){
		
		Player one = new Player("one");
		Player two = new Player("two");
		
		Matchup m = new Matchup(one, two, null, null);
		
		assertTrue(m.containsPlayer(one));
		assertTrue(m.containsPlayer(two));
		
		Player three = new Player("three");
		assertFalse(m.containsPlayer(three));
		
		assertFalse(m.containsPlayer(null));		
				
	
	}
	
	/**
	 * Tests the equals(Matchup m) method
	 */
	public void testEquals(){
		
		Player one = new Player("one");
		Player two = new Player("two");
		
		Matchup m = new Matchup(one, two, null, null);
		Matchup m2 = new Matchup(one, two, null, null);
		
		//these 2 matchups should not be ==; their IDs are different
		assertFalse(m.equals(m2));
		
		//only m should equal m
		assertTrue(m.equals(m));
		
		
	}
	
	/**
	 * Tests the set scores method
	 */
	public void testSetScores(){

		Player one = new Player("one");
		Player two = new Player("two");
		Matchup parentM = new Matchup(null, null);
		Matchup childM = new Matchup(one, two, parentM, null);
		
		//Set scores such that player one wins; verify that all expected behavior is done
		childM.setScores(1, 0);
		
		assertEquals(childM.getScores()[0], 1.0);
		assertEquals(childM.getScores()[1], 0.0);		
		
		assertEquals(childM.getWinner(), one);
		
		assertTrue(parentM.containsPlayer(one));
		
		//Reset matchups for new case
		parentM = new Matchup(null, null);
		childM = new Matchup(one, two, parentM, null);
		
		//Set scores as a tie; currently should randomly sets winner.  behavior subject to change
		childM.setScores(1, 1);
//		
		assertEquals(childM.getScores()[0], 1.0);
		assertEquals(childM.getScores()[1], 1.0);		
//		
		assertTrue(childM.getWinner() != null);
		
	}
	
	public void testForfeit(){
		
		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		
		Matchup finalM = new Matchup(null, null, null, null);
		
		Matchup semifinalM = new Matchup(null, three, finalM, null);
		
		Matchup quarterfinalM = new Matchup(one, two, semifinalM, null);
		
		//forfeiting player one of semifinalM should cause nothing to happen yet because player two is null
		semifinalM.playerTwoSetForfeit(true);
		
		assertEquals(semifinalM.getWinner(), null);
		
		//forfeiting this player should cause player two to go straight to final
		quarterfinalM.playerOneSetForfeit(true);
		
		assertEquals(quarterfinalM.getWinner(), two);
		
		assertEquals(semifinalM.getWinner(), two);
		
		assertTrue(finalM.containsPlayer(two));
		
		
	}
	
	public void testChildren(){
		
		Player one = new Player("one");
		Player two = new Player("two");
		Player three = new Player("three");
		Player four = new Player("four");
		
		Matchup finalM = new Matchup(null, null, null, null);
		
		assertEquals(finalM.getChildren().size(), 0);
		
		Matchup semifinalOne = new Matchup(one, two, finalM, null);
		Matchup semifinalTwo = new Matchup(three, four, finalM, null);
		
		assertEquals(finalM.getChildren().size(), 2);
		assertTrue(finalM.getChildren().contains(semifinalOne));
		assertTrue(finalM.getChildren().contains(semifinalTwo));
		
	}
	
	
	

	
	
	
}