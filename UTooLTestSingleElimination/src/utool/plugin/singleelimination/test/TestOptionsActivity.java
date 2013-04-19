package utool.plugin.singleelimination.test;

import java.util.ArrayList;

import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.SingleEliminationOptionsActivity;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

/**
 * Tests the options Screen
 * @author waltzm
 * @version 1/20/2013
 */
public class TestOptionsActivity extends ActivityUnitTestCase<SingleEliminationOptionsActivity>
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
	private SingleEliminationOptionsActivity mActivity;


	/**
	 * Required constructor for Activity Tests
	 */
	public TestOptionsActivity() {
		super(SingleEliminationOptionsActivity.class);
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
	 * Tests the getting of tournament data
	 */
	public void testGetTournamentData() 
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
		
		ArrayList<Matchup> r1 = t.getBottomRound();

		//make sure tournament is created
		assertNotNull(TournamentLogic.getInstance(tournamentId));
		assertTrue(TournamentLogic.getInstance(tournamentId) instanceof SingleEliminationTournament);

		Intent in = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationOptionsActivity.class);
		in.putExtra("tournamentId", tournamentId);
		mActivity = this.startActivity(in, null, null);
		assertNotNull(mActivity);
		
		String data = SingleEliminationOptionsActivity.getTournamentData(t);

		assertEquals(data,"<h2>Round 1: </h2>"+r1.get(0).getPlayerOne().getName()+" vs. "+r1.get(0).getPlayerTwo().getName()+"<br>"+r1.get(1).getPlayerOne().getName()+" vs. "+r1.get(1).getPlayerTwo().getName()+"<br><br>");

	}
}