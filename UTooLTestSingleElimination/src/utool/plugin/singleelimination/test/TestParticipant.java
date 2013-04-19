package utool.plugin.singleelimination.test;

import java.util.UUID;
import junit.framework.TestCase;
import utool.plugin.singleelimination.Participant;

/**
 * Tests to determine correct operation of the participant class
 * @author waltzm
 * @version 12/28/2012
 */
public class TestParticipant extends TestCase
{

	/**
	 * Tests that the getStanding method in Participant works as expected
	 */
	public void testGetStanding() 
	{
		Participant p = new Participant(new UUID(0, 1), "Bob");

		//Make sure initial standing is 1
		assertEquals(p.getStanding(8), 1);

		p.addScoresForRound(3, 1, 1);
		//make sure still at 1
		assertEquals(p.getStanding(8), 1);

		p.addScoresForRound(1, 3, 2);
		//make sure standing is 3
		assertEquals(p.getStanding(8), 3);
	}

	/**
	 * Tests that RoundEliminatedIn works as intended
	 */
	public void testRoundEliminatedIn()
	{
		Participant p = new Participant(new UUID(0, 1), "Bob");

		assertEquals(p.getRoundEliminatedIn(),-1);
		p.addScoresForRound(3, 1, 1);
		assertEquals(p.getRoundEliminatedIn(),-1);
		p.addScoresForRound(1, 3, 2);

		assertEquals(p.getRoundEliminatedIn(),2);
	}

	/**
	 * Tests that  scores can be recorded correctly
	 */
	public void testScores()
	{
		Participant p = new Participant(new UUID(0, 1), "Bob"); 
		
		p.addScoresForRound(3, 1, 1);
		p.addScoresForRound(3, 2, 2);
		assertEquals(p.getTotalWins(), 6d);
		assertEquals(p.getStanding(8),1);
		
		
	}

}
