package utool.plugin.singleelimination.test;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.RoundStandingsActivity;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;
import utool.plugin.singleelimination.participant.Match;
import android.content.Intent;
import android.test.ActivityUnitTestCase;


/**
 * Tests the testable parts of round standings
 * @author waltzm
 * @version 1/20/2012
 */
public class TestRoundStandings extends ActivityUnitTestCase<RoundStandingsActivity>
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
	private long tournamentId = 42342;

	/**
	 * Holds the tournament
	 */
	private SingleEliminationTournament t;


	/**
	 * The activity we are testing
	 */
	private RoundStandingsActivity mActivity;


	/**
	 * Required constructor for Activity Tests
	 */
	public TestRoundStandings() {
		super(RoundStandingsActivity.class);
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

		//	t = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, playerz, null, Player.HOST);

		TournamentLogic.clearInstance(tournamentId);

	}

	/**
	 * Tests the method get matchups for round
	 */
	public void testGetMatchupsForRound() 
	{			
		//add matches to tournament
		ArrayList<Player> playerz = new ArrayList<Player>();
		playerz.add(p1);
		playerz.add(p2);
		playerz.add(p3);
		playerz.add(p4);
		playerz.add(p5);

		t = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		ArrayList<Matchup> theMatchups = SingleEliminationTournament.generateRandomMatchups(playerz, t);
		t.setMatchups(theMatchups);

		//make sure tournament is created
		assertNotNull(TournamentLogic.getInstance(tournamentId));
		assertTrue(TournamentLogic.getInstance(tournamentId) instanceof SingleEliminationTournament);

		Intent in = new Intent(this.getInstrumentation().getTargetContext(), RoundStandingsActivity.class);
		in.putExtra("tournamentId", tournamentId);
		in.putExtra("Round", 1);
		mActivity = this.startActivity(in, null, null);
		assertNotNull(mActivity);

		ArrayList<Match> m = mActivity.getMatchupsForRound(1);

		//determine correct round 1
		assertEquals(4, m.size());

		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		players.add(p5);
		int bye = 0;

		//make sure players are all in once and 3 BYES
		for(int i=0;i<m.size();i++)
		{
			Player a = m.get(i).getPlayerOne();
			Player b = m.get(i).getPlayerTwo();

			if(a.equals(Player.BYE)||a  ==null || a.getName().equals("BYE"))
			{
				bye++;
			}

			if(b.equals(Player.BYE)||b == null|| b.getName().equals("BYE"))
			{
				bye++;
			}
		}

		//now should only have null players
		assertEquals(bye,3);

		//determine correct round 2
		m = mActivity.getMatchupsForRound(2);
		assertEquals(2, m.size());


		bye = 0;

		//make sure players are all in once and 3 BYES
		for(int i=0;i<m.size();i++)
		{
			Player a = m.get(i).getPlayerOne();
			Player b = m.get(i).getPlayerTwo();

			if( a.getName().equals("UNDECIDED"))
			{
				bye++;
			}

			if(b.getName().equals("UNDECIDED"))
			{
				bye++;
			}
		}

		//now should only have null players
		assertEquals(bye,4);


		//determine correct round 3
		m = mActivity.getMatchupsForRound(3);
		assertEquals(1, m.size());


		bye = 0;

		//make sure players are all in once and 3 BYES
		for(int i=0;i<m.size();i++)
		{
			Player a = m.get(i).getPlayerOne();
			Player b = m.get(i).getPlayerTwo();

			if( a.getName().equals("UNDECIDED"))
			{
				bye++;
			}

			if(b.getName().equals("UNDECIDED"))
			{
				bye++;
			}
		}

		//now should only have null players
		assertEquals(bye,2);

	}


	/**
	 * Tests the round methods
	 */
	public void testRounding() 
	{			
		//add matches to tournament
		ArrayList<Player> playerz = new ArrayList<Player>();
		playerz.add(p1);
		playerz.add(p2);
		playerz.add(p3);
		playerz.add(p4);
		playerz.add(p5);

		t = (SingleEliminationTournament) TournamentLogic.getInstance(tournamentId, null);
		ArrayList<Matchup> theMatchups = SingleEliminationTournament.generateRandomMatchups(playerz, t);
		t.setMatchups(theMatchups);

		//make sure tournament is created
		assertNotNull(TournamentLogic.getInstance(tournamentId));
		assertTrue(TournamentLogic.getInstance(tournamentId) instanceof SingleEliminationTournament);

		Intent in = new Intent(this.getInstrumentation().getTargetContext(), RoundStandingsActivity.class);
		in.putExtra("tournamentId", tournamentId);
		in.putExtra("Round", 1);
		mActivity = this.startActivity(in, null, null);
		assertNotNull(mActivity);


		//TEST ROUND ONE DECIMAL
		double a = 1.9999;
		assertEquals(""+RoundStandingsActivity.roundOneDecimal(a), ""+2.0);
		a = 1.3333333;
		assertEquals(""+RoundStandingsActivity.roundOneDecimal(a)+"", ""+1.3);
		a = 2342;
		assertEquals(""+RoundStandingsActivity.roundOneDecimal(a)+"", ""+2342.0+"");

		//TEST ROUND TWO DECIMAL
		a = 1.9999;
		assertEquals(""+RoundStandingsActivity.roundTwoDecimal(a)+"", ""+2.00+"");
		a = 1.3333333;
		assertEquals(""+RoundStandingsActivity.roundTwoDecimal(a)+"", ""+1.33+"");
		a = 2342;
		assertEquals(""+RoundStandingsActivity.roundTwoDecimal(a)+"", ""+2342.00+"");

	}
	


}
