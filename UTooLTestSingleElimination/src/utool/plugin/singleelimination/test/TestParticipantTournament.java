package utool.plugin.singleelimination.test;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.singleelimination.ParticipantTournamentActivity;
import utool.plugin.singleelimination.TournamentLogic;
import utool.plugin.singleelimination.participant.Match;
import utool.plugin.singleelimination.participant.SingleEliminationPartTournament;
import junit.framework.TestCase;

/**
 * Test class for the Participant Tournament
 * @author waltzm
 * @version 12/3/2012
 */
public class TestParticipantTournament extends TestCase
{
	/**
	 * tournament id
	 */
	long tid = 3451;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		//create a tournament
		t = (SingleEliminationPartTournament) TournamentLogic.getInstance(3451, null, null, Player.PARTICIPANT);
		t.clearInstance();

	}

	/**
	 * holds the tournament object used in the tests
	 */
	SingleEliminationPartTournament t;

	/**
	 * first player
	 */
	Player p1 = new Player("John");

	/**
	 * second player
	 */
	Player p2 = new Player("Tim");

	/**
	 * third player
	 */
	Player p3 = new Player("Randy");

	/**
	 * fourth player
	 */
	Player p4 = new Player("Bob");

	/**
	 * fifth player
	 */
	Player p5 = new Player("Kitty");

	/**
	 * sixth player
	 */
	Player p6 = new Player("Invalid");



	/**
	 * Tests that the groups information is accurate
	 * @since 12/4/2012
	 */
	public void testGetGroups() 
	{
		//make sure tournament is clear initially
		assertEquals(t.getGroups().size(),0);
		assertEquals(t.getChildren().size(),0);

		//add players
		ArrayList<Player> list = new ArrayList<Player>();
		list.add(p4);
		list.add(p3);
		list.add(p2);
		list.add(p1);

		t.setPlayers(list);

		//add stuff
		t.addMatchup(1, p1.getUUID(), p2.getUUID(), 1);
		t.addMatchup(2, p3.getUUID(), p4.getUUID(), 1);

		//check that round 1 now exists
		assertEquals(1, t.getGroups().size());
		assertEquals("Round 1", t.getGroups().get(0));

		//add roudn 2 matches
		t.addMatchup(3, p1.getUUID(), p3.getUUID(), 2);

		//check that round 2 now exists
		assertEquals(2, t.getGroups().size());
		assertEquals("Round 2", t.getGroups().get(1));

		//add round 5
		t.addMatchup(4, p1.getUUID(), p3.getUUID(), 5); 

		//check that round 5 exists
		assertEquals(3, t.getGroups().size());
		assertEquals("Round 5", t.getGroups().get(2));
	}

	/**
	 * Tests that the children information is accurate
	 * @since 12/4/2012
	 */
	public void testGetChildren() 
	{
		//make sure tournament is clear initially
		assertEquals(t.getGroups().size(),0);
		assertEquals(t.getChildren().size(),0);

		//add players
		ArrayList<Player> list = new ArrayList<Player>();
		list.add(p4);
		list.add(p3);
		list.add(p2);
		list.add(p1);

		t.setPlayers(list);

		//add stuff
		t.addMatchup(21, p1.getUUID(), p2.getUUID(), 1);
		t.addMatchup(22, p3.getUUID(), p4.getUUID(), 1);

		//check that round 1 matches now exists
		assertEquals(1, t.getChildren().size());
		assertEquals(2, t.getChildren().get(0).size());
		Match m1 = t.getChildren().get(0).get(0);
		Match m2 = t.getChildren().get(0).get(1);

		assertEquals(21l, m1.getId());
		assertEquals(22l, m2.getId());

		//add round 2 matches
		t.addMatchup(23, p1.getUUID(), p3.getUUID(), 2);

		//check that round 2 now exists
		assertEquals(2, t.getChildren().size());
		assertEquals(2, t.getChildren().get(0).size());
		assertEquals(1, t.getChildren().get(1).size());

		Match m3 = t.getChildren().get(1).get(0);
		assertEquals(23l, m3.getId());

		//add round 5
		t.addMatchup(24, p1.getUUID(), p3.getUUID(), 5); 

		//check that round 5 exists
		assertEquals(3, t.getChildren().size());
		assertEquals(2, t.getChildren().get(0).size());
		assertEquals(1, t.getChildren().get(1).size());
		assertEquals(1, t.getChildren().get(2).size());
		
		Match m4 = t.getChildren().get(2).get(0);
		assertEquals(24l, m4.getId());
		
		//make sure none of the matches have been changed
		assertEquals(21l, m1.getId());
		assertEquals(22l, m2.getId());
		assertEquals(23l, m3.getId());
		assertEquals(24l, m4.getId());
		
	}
	
	/**
	 * tests the get and set activity work
	 * @since 12/4/2012
	 */
	public void testActivity()
	{
		assertNull(t.getActivity());
		
		//set activity
		ParticipantTournamentActivity s = new ParticipantTournamentActivity();
		t.setActivity(s);
		
		assertEquals(s,t.getActivity());
			
	}

	/**
	 * Tests that the clear instance works
	 * @since 12/4/2012
	 */
	public void testclearInstance() 
	{		
		//make sure tournament is clear initially
		assertEquals(t.getGroups().size(),0);
		assertEquals(t.getChildren().size(),0);

		//add players
		ArrayList<Player> list = new ArrayList<Player>();
		list.add(p4);
		list.add(p3);
		list.add(p2);
		list.add(p1);

		t.setPlayers(list);

		//add stuff
		t.addMatchup(1, p1.getUUID(), p2.getUUID(), 1);

		//clear tournament
		t.clearInstance();

		//see if clear
		assertEquals(t.getMatch(1),null);
	}

	/**
	 * Tests that the setScore works
	 * @since 12/4/2012
	 */
	public void testSetScore() 
	{

		//add players
		ArrayList<Player> list = new ArrayList<Player>();
		list.add(p5);
		list.add(p4);
		list.add(p3);
		list.add(p2);
		list.add(p1);		

		t.setPlayers(list);


		t.addMatchup(1, p1.getUUID(), p2.getUUID(), 1);
		t.addMatchup(2, p3.getUUID(),p4.getUUID(),1);

		assertEquals(t.setScore(1, 5, 3.5),true);
		assertEquals(t.setScore(2, 8, 1),true);

		assertEquals(t.getMatch(1).getScoreP1(), 5.0);
		assertEquals(t.getMatch(1).getScoreP2(), 3.5);
		assertEquals(t.getMatch(2).getScoreP1(), 8.0);
		assertEquals(t.getMatch(2).getScoreP2(), 1.0);

		//test invalid match fails
		assertEquals(t.setScore(3, 3, 4),false);

		//test byes
		t.addMatchup(3, p5.getUUID(),Player.BYE,1);	
		assertEquals(true,t.setScore(3,1,0));
		assertEquals(t.getMatch(3).getScoreP1(), 1.0);
		assertEquals(t.getMatch(3).getScoreP2(), 0.0);

		t.addMatchup(4, Player.BYE,p5.getUUID(),1);	
		assertEquals(true,t.setScore(3,1,0));
		//		assertEquals(t.getMatch(4).getScoreP1(), 1.0);//TODO
		assertEquals(t.getMatch(4).getScoreP2(), 0.0);

		//test negative and zero scores
		t.addMatchup(5, p1.getUUID(),p5.getUUID(),1);	
		assertEquals(true,t.setScore(3,-60,0));
		//		assertEquals(t.getMatch(5).getScoreP1(), -60.0);//TODO
		assertEquals(t.getMatch(5).getScoreP2(), 0.0);

		t.addMatchup(6, p2.getUUID(),p5.getUUID(),1);	
		assertEquals(true,t.setScore(3,0,0));
		assertEquals(t.getMatch(6).getScoreP1(), 0.0);
		assertEquals(t.getMatch(6).getScoreP2(), 0.0);


	}



	/**
	 * Tests that matchup are able to be added and information is kept
	 * @since 12/2/2012
	 */
	public void testAddingMatchup() 
	{	

		//add players
		ArrayList<Player> list = new ArrayList<Player>();
		list.add(p5);
		list.add(p4);
		list.add(p3);
		list.add(p2);
		list.add(p1);


		t.setPlayers(list);

		//add some matchups
		assertEquals(true,t.addMatchup(1,p1.getUUID(), p2.getUUID(), 1));
		assertEquals(true,t.addMatchup(2,p3.getUUID(), p4.getUUID(), 1));

		//verify matchup
		Match m = t.getMatch(1);
		assertEquals(m.getId(), 1);
		assertEquals(m.getRound(),1);
		assertEquals(m.getScoreP1(),0d);
		assertEquals(m.getScoreP2(),0d);
		assertEquals(m.getPlayerOne().getUUID(),p1.getUUID());
		assertEquals(m.getPlayerTwo().getUUID(),p2.getUUID());
		assertEquals(m.getPlayerOne().getName(),p1.getName());
		assertEquals(m.getPlayerTwo().getName(),p2.getName());

		//verify matchup
		Match m2 = t.getMatch(2);
		assertEquals(m2.getId(), 2);
		assertEquals(m2.getRound(),1);
		assertEquals(m2.getScoreP1(),0d);
		assertEquals(m2.getScoreP2(),0d);
		assertEquals(m2.getPlayerOne().getUUID(),p3.getUUID());
		assertEquals(m2.getPlayerTwo().getUUID(),p4.getUUID());
		assertEquals(m2.getPlayerOne().getName(),p3.getName());
		assertEquals(m2.getPlayerTwo().getName(),p4.getName());

		//test invalid player (p6 isn't in list)
		assertEquals(false,t.addMatchup(90,p6.getUUID(), p4.getUUID(), 1));
		assertEquals(false,t.addMatchup(90, p4.getUUID(),p6.getUUID(), 1));
		assertEquals(false,t.addMatchup(90, p6.getUUID(),p6.getUUID(), 1));

		//assert that the match wasn't added
		assertNull(t.getMatch(90)); 

		//test that adding with existing mid fails
		assertEquals(false, t.addMatchup(1, p4.getUUID(), p3.getUUID(), 2));


		//test byes
		t.addMatchup(30,p5.getUUID(), Player.BYE, 1);

		Match m3 = t.getMatch(30);
		assertEquals(m3.getId(), 30);
		assertEquals(m3.getRound(),1);
		assertEquals(m3.getScoreP1(),0d);
		assertEquals(m3.getScoreP2(),0d);
		assertEquals(m3.getPlayerOne().getUUID(),p5.getUUID());
		assertEquals(m3.getPlayerTwo().getUUID(),Player.BYE);
		assertEquals(m3.getPlayerOne().getName(),p5.getName());
		assertEquals(m3.getPlayerTwo().getName(),"Bye");

		t.addMatchup(31, Player.BYE, p5.getUUID(), 1);
		Match m4 = t.getMatch(31);
		assertEquals(m4.getId(), 31);
		assertEquals(m4.getRound(),1);
		assertEquals(m4.getScoreP1(),0d);
		assertEquals(m4.getScoreP2(),0d);
		assertEquals(m4.getPlayerTwo().getUUID(),p5.getUUID());
		assertEquals(m4.getPlayerOne().getUUID(),Player.BYE);
		assertEquals(m4.getPlayerTwo().getName(),p5.getName());
		assertEquals(m4.getPlayerOne().getName(),"Bye");

		t.addMatchup(32, Player.BYE, Player.BYE, 1);
		Match m5 = t.getMatch(32);
		assertEquals(m5.getId(), 32);
		assertEquals(m5.getRound(),1);
		assertEquals(m5.getScoreP1(),0d);
		assertEquals(m5.getPlayerOne().getUUID(),Player.BYE);
		assertEquals(m5.getPlayerOne().getName(),"Bye");
		assertEquals(m5.getPlayerTwo().getUUID(),Player.BYE);
		assertEquals(m5.getPlayerTwo().getName(),"Bye");
	}

	/**
	 * Test of editing a matchup
	 */
	public void testEditMatchup()
	{
		//Test that it works like add matchup except make sure it fails if the mid is not existent
		//add players
		ArrayList<Player> list = new ArrayList<Player>();
		list.add(p5);
		list.add(p4);
		list.add(p3);
		list.add(p2);
		list.add(p1);

		t.setPlayers(list);

		//add some matchups
		assertEquals(true,t.addMatchup(1,p1.getUUID(), p2.getUUID(), 4));
		assertEquals(true,t.addMatchup(2,p3.getUUID(), p4.getUUID(), 5));

		//edit the matchups 
		t.editMatchup(1, p3.getUUID(), p4.getUUID(), 1);
		t.editMatchup(2, p1.getUUID(), p2.getUUID(), 1);

		//verify matchup
		Match m = t.getMatch(1);
		assertEquals(m.getId(), 1);
		assertEquals(m.getRound(),1);
		assertEquals(m.getScoreP1(),0d);
		assertEquals(m.getScoreP2(),0d);
		assertEquals(m.getPlayerOne().getUUID(),p3.getUUID());
		assertEquals(m.getPlayerTwo().getUUID(),p4.getUUID());
		assertEquals(m.getPlayerOne().getName(),p3.getName());
		assertEquals(m.getPlayerTwo().getName(),p4.getName());

		//verify matchup
		Match m2 = t.getMatch(2);
		assertEquals(m2.getId(), 2);
		assertEquals(m2.getRound(),1);
		assertEquals(m2.getScoreP1(),0d);
		assertEquals(m2.getScoreP2(),0d);
		assertEquals(m2.getPlayerOne().getUUID(),p1.getUUID());
		assertEquals(m2.getPlayerTwo().getUUID(),p2.getUUID());
		assertEquals(m2.getPlayerOne().getName(),p1.getName());
		assertEquals(m2.getPlayerTwo().getName(),p2.getName());



		//test invalid player (p6 isn't in list)
		assertEquals(false,t.editMatchup(90,p6.getUUID(), p4.getUUID(), 1));
		assertEquals(false,t.editMatchup(90, p4.getUUID(),p6.getUUID(), 1));
		assertEquals(false,t.editMatchup(90, p6.getUUID(),p6.getUUID(), 1));

		//assert that the match wasn't added
		assertNull(t.getMatch(90)); 

		//test that editing with non-existing mid fails
		assertEquals(false, t.editMatchup(23494, p4.getUUID(), p3.getUUID(), 2));


		//test byes
		t.addMatchup(30,p1.getUUID(), p2.getUUID(), 3);
		t.editMatchup(30, p5.getUUID(), Player.BYE, 1);

		Match m3 = t.getMatch(30);
		assertEquals(m3.getId(), 30);
		assertEquals(m3.getRound(),1);
		assertEquals(m3.getScoreP1(),0d);
		assertEquals(m3.getScoreP2(),0d);
		assertEquals(m3.getPlayerOne().getUUID(),p5.getUUID());
		assertEquals(m3.getPlayerTwo().getUUID(),Player.BYE);
		assertEquals(m3.getPlayerOne().getName(),p5.getName());
		assertEquals(m3.getPlayerTwo().getName(),"Bye");

		t.addMatchup(31, p1.getUUID(), p2.getUUID(), 4);
		t.editMatchup(31, Player.BYE, p5.getUUID(), 1);
		Match m4 = t.getMatch(31);
		assertEquals(m4.getId(), 31);
		assertEquals(m4.getRound(),1);
		assertEquals(m4.getScoreP1(),0d);
		assertEquals(m4.getScoreP2(),0d);
		assertEquals(m4.getPlayerTwo().getUUID(),p5.getUUID());
		assertEquals(m4.getPlayerOne().getUUID(),Player.BYE);
		assertEquals(m4.getPlayerTwo().getName(),p5.getName());
		assertEquals(m4.getPlayerOne().getName(),"Bye");

		t.addMatchup(32, p1.getUUID(), p2.getUUID(), 4);
		t.editMatchup(32, Player.BYE, Player.BYE, 1);
		Match m5 = t.getMatch(32);
		assertEquals(m5.getId(), 32);
		assertEquals(m5.getRound(),1);
		assertEquals(m5.getScoreP1(),0d);
		assertEquals(m5.getPlayerOne().getUUID(),Player.BYE);
		assertEquals(m5.getPlayerOne().getName(),"Bye");
		assertEquals(m5.getPlayerTwo().getUUID(),Player.BYE);
		assertEquals(m5.getPlayerTwo().getName(),"Bye");
	}
}
