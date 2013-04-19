package utool.plugin.singleelimination.test;

import java.util.ArrayList;

import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.SEAdvancedEmailOptions;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

/**
 * Class for testing advanced email options
 * @author waltzm
 * @version 1/20/2012
 */
public class TestAdvancedSEOptions extends ActivityUnitTestCase<SEAdvancedEmailOptions>
{

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
	 * Player 5
	 */
	private Player p5 = new Player("Ned");


	/**
	 * holds the tournament id
	 */
	private long tournamentId = 235;

	/**
	 * Holds the tournament
	 */
	private SingleEliminationTournament t;


	/**
	 * The activity we are testing
	 */
	private SEAdvancedEmailOptions mActivity;


	/**
	 * Required constructor for Activity Tests
	 */
	public TestAdvancedSEOptions() {
		super(SEAdvancedEmailOptions.class);
	}


	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		//add matches to tournament
		ArrayList<Player> playerz = new ArrayList<Player>();
		playerz.add(p1);
		playerz.add(p2);
		playerz.add(p3);
		playerz.add(p4);
		playerz.add(p5);

		TournamentLogic.clearInstance(tournamentId);

	}

	/**
	 * Tests the adding of a possible subscriber
	 */
	public void testAddPossibleSubscriber() 
	{			
		//add matches to tournament
		ArrayList<Player> playerz = new ArrayList<Player>();
		playerz.add(p1);
		playerz.add(p2);
		playerz.add(p3);
		playerz.add(p4);

		t = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		ArrayList<Matchup> theMatchups = SingleEliminationTournament.generateRandomMatchups(playerz, t);
		t.setMatchups(theMatchups);

		//make sure tournament is created
		assertNotNull(TournamentLogic.getInstance(tournamentId));
		assertTrue(TournamentLogic.getInstance(tournamentId) instanceof SingleEliminationTournament);

		Intent in = new Intent(this.getInstrumentation().getTargetContext(), SEAdvancedEmailOptions.class);
		in.putExtra("tournamentId", tournamentId);
		mActivity = this.startActivity(in, null, null);
		assertNotNull(mActivity);
		
		//Test adding one not in list
		ArrayList<String> em = new ArrayList<String>();
		em.add("emailaddress1");
		em.add("emailaddress2");
		em.add("emailaddress3");
		mActivity.addPossibleSubscriber(em, "emailaddress4");
		
		assertEquals(em.size(), 4);
		assertEquals(em.get(3),"emailaddress4");
		
		
		//Test adding one in list
		mActivity.addPossibleSubscriber(em, "emailaddress4");	
		assertEquals(em.size(), 4);
		assertEquals(em.get(3),"emailaddress4");
	}

}
