package utool.plugin.singleelimination.test;

import java.util.ArrayList;
import java.util.UUID;
import junit.framework.TestCase;
import utool.plugin.Player;
import utool.plugin.singleelimination.SingleEliminationTournament;
import utool.plugin.singleelimination.TournamentLogic;
import utool.plugin.singleelimination.communications.SaxFeedParser;

/**
 * This test is to fully test the parsing of xml through the saxfeedparser, feedhandler and IncomingCommandHandler
 * @author waltzm
 * @version 10/25/2012
 */
public class TestFromXML extends TestCase{

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
	 * the sax feed parser being used
	 */
	private SaxFeedParser s;
	
	/**
	 * holds the tournament object used in the tests
	 */
	private SingleEliminationTournament tournament;

	/**
	 * holds the number of the current test
	 */
	private int currentTest=0;

	/**
	 * Holds whether or not each test has passed
	 */
	private Boolean[] tests = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		s= new SaxFeedParser(new IncomingCommandHandlerForTests(this));
		tournament = (SingleEliminationTournament) TournamentLogic.getInstance(0, null);
		tournament.setPermissionLevel(Player.HOST);
	}

	/**
	 * Tests that the change Matchup xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testChangeMatchup() 
	{	
		//check change matchup with 1 person teams
		tournament.getOutgoingCommandHandler().handleChangeMatchup(tid, mid, n1, n2, t1, t2, round, table);
		String xml = tournament.bridge.getLastXML(); 
		this.currentTest = 0;
		s.parse(xml);
		assertTrue(tests[0]);

		//check change matchup with 2 person teams
		tournament.getOutgoingCommandHandler().handleChangeMatchup(tid, mid, n3, n4, t3, t4, round, table);
		xml = tournament.bridge.getLastXML(); 
		this.currentTest = 1;
		s.parse(xml);
		assertTrue(tests[1]);

		//test if table being null works as intended
		tournament.getOutgoingCommandHandler().handleChangeMatchup(tid, mid, n1, n2, t1, t2, round, null);
		xml = tournament.bridge.getLastXML(); 
		this.currentTest = 2;
		s.parse(xml);
		assertTrue(tests[2]);

	}

	/**
	 * Tests that the send Matchup xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testSendMatchup() 
	{	
		//check send matchup with 1 person teams
		tournament.getOutgoingCommandHandler().handleSendMatchup(tid, mid,n1, n2, t1, t2, round, table);
		String xml = tournament.bridge.getLastXML();  
		this.currentTest = 3;
		s.parse(xml);
		assertTrue(tests[3]);

		//check send matchup with 2 person teams
		tournament.getOutgoingCommandHandler().handleSendMatchup(tid, mid, n3, n4, t3, t4, round, table);
		xml = tournament.bridge.getLastXML();  
		this.currentTest = 4;
		s.parse(xml);
		assertTrue(tests[4]);

		//test if table being null works as intended
		tournament.getOutgoingCommandHandler().handleSendMatchup(tid, mid, n1, n2, t1, t2, round, null);
		xml = tournament.bridge.getLastXML();  
		this.currentTest = 5;
		s.parse(xml);
		assertTrue(tests[5]);

	}

	/**
	 * Tests that the send Score xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testSendScore() 
	{	
		//check send matchup with 1 person teams
		tournament.getOutgoingCommandHandler().handleSendScore(tid, mid, p1.getUUID().toString(), p2.getUUID().toString(), 3d, 0d, round);
		String xml = tournament.bridge.getLastXML();  
		xml =tournament.bridge.getLastXML();  
		this.currentTest = 6;
		s.parse(xml);
		assertTrue(tests[6]);

		//check send score with 2 person teams
		tournament.getOutgoingCommandHandler().handleSendScore(tid, mid, n3, n4, 2d, 1d, round);
		xml = tournament.bridge.getLastXML();  
		this.currentTest = 7;
		s.parse(xml);
		assertTrue(tests[7]);
	}

	/**
	 * Tests that the send Players xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testSendPlayers() 
	{	
		//test for 4 players, no ghosts and no seeds
		tournament.getOutgoingCommandHandler().handleSendPlayers(tid, players);
		String xml = tournament.bridge.getLastXML();  
		this.currentTest = 8;
		s.parse(xml);
		assertTrue(tests[8]);

		//test seeds
		Player s1 = new Player(new UUID(0,45), "Bob",false, 1);
		Player s2 = new Player(new UUID(0,56), "Tim",false, 2);
		Player s3 = new Player(new UUID(0,23), "Bob",false, 4);
		Player s4 = new Player(new UUID(0,53), "Mike",false, 3);
		Player[] seeds = {s1,s2,s3,s4};
		tournament.getOutgoingCommandHandler().handleSendPlayers(tid,seeds);
		xml = tournament.bridge.getLastXML(); 
		this.currentTest = 9;
		s.parse(xml);
		assertTrue(tests[9]);

		//test ghosts
		s1 = new Player(new UUID(0,45), "Bob",true, 1);
		s2 = new Player(new UUID(0,56), "Tim",false, 2);
		s3 = new Player(new UUID(0,23), "Bob",true, 4);
		s4 = new Player(new UUID(0,53), "Mike",false, 3);
		Player[] ghost = {s1,s2,s3,s4};
		tournament.getOutgoingCommandHandler().handleSendPlayers(tid,ghost);
		xml = tournament.bridge.getLastXML();  
		this.currentTest = 10;
		s.parse(xml);
		assertTrue(tests[10]);
	}

	/**
	 * Tests that the send final standing xml is being parsed correctly
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
		this.currentTest = 11;
		s.parse(xml);
		assertTrue(tests[11]);
	}

	/**
	 * Tests that the send round timer amount xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testSendRoundTimerAmount() 
	{
		//Test sending time through single time string
		tournament.getOutgoingCommandHandler().handleSendRoundTimerAmount(tid, "10:23:45");
		String xml = tournament.bridge.getLastXML();
		this.currentTest = 12;
		s.parse(xml);
		assertTrue(tests[12]);
	}

	/**
	 * Tests that the send tournament name xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testSendTournamentName() 
	{
		//Test sending the tournament name
		tournament.getOutgoingCommandHandler().handleSendTournamentName(tid, "Tournament1");
		String xml = tournament.bridge.getLastXML(); 
		this.currentTest = 13;
		s.parse(xml);
		assertTrue(tests[13]);
	}

	/**
	 * Tests that the send error xml is being parsed correctly
	 * @since 10/25/2012
	 */
	public void testSendError() 
	{
		//Test sending an error
		tournament.getOutgoingCommandHandler().handleSendError(tid, p1.getUUID().toString(), "NullPointer", "r is null");
		String xml = tournament.bridge.getLastXML();  
		this.currentTest = 14;
		s.parse(xml);
		assertTrue(tests[14]);
	}

	/**
	 * Tests that the send begin new round xml is being generated correctly
	 * @since 10/25/2012
	 */
	public void testSendBeginNewRound() 
	{
		tournament.getOutgoingCommandHandler().handleSendBeginNewRound(tid, 1);
		String xml = tournament.bridge.getLastXML();  
		this.currentTest = 15;
		s.parse(xml);
		assertTrue(tests[15]);
	}

	/**
	 * Tests the change matchup was called with the correct parameters
	 * used in test 0,1,2
	 * @param id of the tournament
	 * @param matchid of the match
	 * @param team1name name of the first team
	 * @param team2name name of the second team
	 * @param team1 of the matchup being sent
	 * @param team2 of the matchup being sent
	 * @param roundNum of the matchup
	 * @param tableNum of the matchup
	 */
	public void changeMatchupTest(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int roundNum, String tableNum) 
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 0)
		{
			//check change matchup with 1 person teams
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n1);
			assertEquals(team2name, n2);
			assertEquals(roundNum, round);
			//team 1 
			assertEquals(t1.length, team1.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t1[i], team1.get(i));
			}
			//team 2
			assertEquals(t2.length, team2.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t2[i], team2.get(i));
			}

			assertEquals(tableNum, table);
			tests[0]=true;

		}
		else if(this.currentTest == 1)
		{
			//check change matchup with 2 person teams
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n3);
			assertEquals(team2name, n4);
			assertEquals(roundNum, round);
			//team 3 
			assertEquals(t3.length, team1.size());
			for(int i=0;i<this.t3.length;i++)
			{
				assertEquals(t3[i], team1.get(team1.size()-1-i));
			}
			//team 4
			assertEquals(t4.length, team2.size());
			for(int i=0;i<this.t4.length;i++)
			{
				assertEquals(t4[i], team2.get(team1.size()-1-i));
			}

			assertEquals(tableNum, table);
			tests[1]=true;
		}
		else if(this.currentTest == 2)
		{
			//test if table being null works as intended
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n1);
			assertEquals(team2name, n2);
			assertEquals(roundNum, round);
			//team 1 
			assertEquals(t1.length, team1.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t1[i], team1.get(i));
			}
			//team 2
			assertEquals(t2.length, team2.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t2[i], team2.get(i));
			}

			assertEquals(tableNum, null);

			tests[2]=true;
		}
		else
		{
			fail("Entered ChangeMatchups without it being a changeMatchup test. Test number: "+this.currentTest);
		}

	}

	/**
	 * Tests the send matchup was called with the correct parameters
	 * used in test 3,4,5
	 * @param id of the tournament
	 * @param matchid of the match
	 * @param team1name name of the first team
	 * @param team2name name of the second team
	 * @param team1 of the matchup being sent
	 * @param team2 of the matchup being sent
	 * @param roundNum of the matchup
	 * @param tableNum of the matchup
	 */
	public void sendMatchupTest(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int roundNum, String tableNum) 
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 3)
		{
			//check send matchup with 1 person teams
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n1);
			assertEquals(team2name, n2);
			assertEquals(roundNum, round);
			//team 1 
			assertEquals(t1.length, team1.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t1[i], team1.get(i));
			}
			//team 2
			assertEquals(t2.length, team2.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t2[i], team2.get(i));
			}

			assertEquals(tableNum, table);
			tests[3]=true;

		}
		else if(this.currentTest == 4)
		{
			//check change matchup with 2 person teams
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n3);
			assertEquals(team2name, n4);
			assertEquals(roundNum, round);
			//team 3 
			assertEquals(t3.length, team1.size());
			for(int i=0;i<this.t3.length;i++)
			{
				assertEquals(t3[i], team1.get(team1.size()-1-i));
			}
			//team 4
			assertEquals(t4.length, team2.size());
			for(int i=0;i<this.t4.length;i++)
			{
				assertEquals(t4[i], team2.get(team1.size()-1-i));
			}

			assertEquals(tableNum, table);
			tests[4]=true;
		}
		else if(this.currentTest == 5)
		{
			//test if table being null works as intended
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n1);
			assertEquals(team2name, n2);
			assertEquals(roundNum, round);
			//team 1 
			assertEquals(t1.length, team1.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t1[i], team1.get(i));
			}
			//team 2
			assertEquals(t2.length, team2.size());
			for(int i=0;i<this.t1.length;i++)
			{
				assertEquals(t2[i], team2.get(i));
			}

			assertEquals(tableNum, null);

			tests[5]=true;
		}
		else
		{
			fail("Entered SendMatchups without it being a sendMatchup test. Test number: "+this.currentTest);
		}

	}


	/**
	 * Tests the send scores was called with the correct parameters
	 * Used in test 6,7
	 * @param id of the tournament
	 * @param matchid of the match
	 * @param team1name name of the first team/id of the player
	 * @param team2name name of the first team/id of the player
	 * @param score1 score of the first team
	 * @param score2 score of the second team
	 * @param round of the match
	 */
	public void sendScoreTest(long id, long matchid, String team1name, String team2name, Double score1, Double score2, int round) 
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 6)
		{
			//check scores with 1 person teams
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, p1.getUUID().toString());
			assertEquals(team2name, p2.getUUID().toString());
			assertEquals(score1, 3d);
			assertEquals(score2, 0d);
			assertEquals(round, this.round);

			tests[6]=true;

		}
		else if(this.currentTest == 7)
		{
			//check scores with 1 person teams
			assertEquals(id, tid);
			assertEquals(matchid, mid);
			assertEquals(team1name, n3);
			assertEquals(team2name, n4);
			assertEquals(score1, 2d);
			assertEquals(score2, 1d);
			assertEquals(round, this.round);

			tests[7]=true;
		}
		else
		{
			fail("Entered SendScore without it being a sendScore test. Test number: "+this.currentTest);
		}

	}

	/**
	 * Tests the send players was called with the correct parameters
	 * Used in test 8,9, 10
	 * @param id of the tournament
	 * @param players in the tournament
	 */
	public void sendPlayersTest(String id, ArrayList<Player> players)
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 8)
		{
			//test no ghosts no seeds
			assertEquals(Long.parseLong(id), tid);
			for(int i=0;i<players.size();i++)
			{
				//determine player
				Player p=null;
				if(i==0)
				{
					p = p4;
				}
				else if(i==1)
				{
					p = p3;
				}
				if(i==2)
				{
					p = p2;
				}
				if(i==3)
				{
					p = p1;
				}

				assertEquals(p.getSeedValue(), players.get(i).getSeedValue());
				assertEquals(p.isGhost(), players.get(i).isGhost());
				assertEquals(p.getUUID(), players.get(i).getUUID());

			}
			tests[8]=true;
		}
		else if(this.currentTest == 9)
		{
			//test seeds
			assertEquals(Long.parseLong(id), tid);
			for(int i=0;i<players.size();i++)
			{
				//determine player
				Player p=null;
				if(i==0)
				{
					p = new Player(new UUID(0,53), "Mike",false, 3);
				}
				else if(i==1)
				{
					p = new Player(new UUID(0,23), "Bob",false, 4);
				}
				if(i==2)
				{
					p = new Player(new UUID(0,56), "Tim",false, 2);
				}
				if(i==3)
				{
					p = new Player(new UUID(0,45), "Bob",false, 1);
				}

				assertEquals(p.getSeedValue(), players.get(i).getSeedValue());
				assertEquals(p.isGhost(), players.get(i).isGhost());
				assertEquals(p.getUUID(), players.get(i).getUUID());

			}

			tests[9]=true;
		}
		else if(this.currentTest == 10)
		{
			//test ghosts
			assertEquals(Long.parseLong(id), tid);
			for(int i=0;i<players.size();i++)
			{
				//determine player
				Player p=null;
				if(i==0)
				{
					p = new Player(new UUID(0,53), "Mike",false, 3);
				}
				else if(i==1)
				{
					p = new Player(new UUID(0,23), "Bob",true, 4);
				}
				if(i==2)
				{
					p = new Player(new UUID(0,56), "Tim",false, 2);
				}
				if(i==3)
				{
					p = new Player(new UUID(0,45), "Bob",true, 1);
				}

				assertEquals(p.getSeedValue(), players.get(i).getSeedValue());
				assertEquals(p.isGhost(), players.get(i).isGhost());
				assertEquals(p.getUUID(), players.get(i).getUUID());

			}

			tests[10]=true;
		}
		else
		{
			fail("Entered SendPlayers without it being a sendPlayers test. Test number: "+this.currentTest);
		}

	}

	/**
	 * Tests the send final standings was called with the correct parameters
	 * Test 11
	 * @param id of the tournament
	 * @param players list of players in the tournament
	 * @param w of each player overall
	 * @param l of each player overall
	 * @param rw of each player overall
	 * @param rl of each player overall
	 * @param s of each player at the completion of the tournament
	 */
	public void sendFinalStandingsTest(String id, ArrayList<String> players, ArrayList<Double> w, ArrayList<Double> l, ArrayList<Integer> rw, ArrayList<Integer> rl, ArrayList<Integer> s)
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 11)
		{
			assertEquals(Long.parseLong(id), tid);

			String[] p = {p1.getUUID().toString(),p2.getUUID().toString(),p3.getUUID().toString(),p4.getUUID().toString()};
			Double[]  wins = {5d,6d,2d,0d};
			Double[]  losses = {0d,2d,5d,6d};
			Integer[]  roundWins = {1,2,3,0};
			Integer[]  roundLosses = {0,2,3,1};
			Integer[]  standing = {1,3,2,4};

			assertEquals(w.size(),4);
			assertEquals(l.size(),4);
			assertEquals(rw.size(),4);
			assertEquals(rl.size(),4);
			assertEquals(s.size(),4);
			assertEquals(players.size(),4);

			//go through players
			for(int i=0;i<players.size();i++)
			{
				assertEquals(players.get(i),p[3-i]);
				assertEquals(w.get(i),wins[3-i]);
				assertEquals(l.get(i),losses[3-i]);
				assertEquals(rw.get(i),roundWins[3-i]);
				assertEquals(rl.get(i),roundLosses[3-i]);
				assertEquals(s.get(i),standing[3-i]);
			}

			tests[11]=true;

		}
		else
		{
			fail("Entered SendFinalStandings without it being a sendFinalStandings test. Test number: "+this.currentTest);
		}

	}

	/**
	 * Tests the send round timer was called with the correct parameters
	 * Test 12
	 * @param id of the tournament
	 * @param time the time in hh:mm:ss
	 */
	public void sendRoundTimerTest(long id, String time)
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 12)
		{
			assertEquals(id, tid);
			assertEquals(time, "10:23:45");

			tests[12]=true;

		}
		else
		{
			fail("Entered SendFinalStandings without it being a sendFinalStandings test. Test number: "+this.currentTest);
		}

	}
	
	/**
	 * Tests the send tournament name was called with the correct parameters
	 * Test 13
	 * @param id of the tournament
	 * @param name the name of the tournament
	 */
	public void sendTournamentNameTest(long id, String name)
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 13)
		{
			assertEquals(id, tid);
			assertEquals(name, "Tournament1");
			tests[13]=true;

		}
		else
		{
			fail("Entered SendFinalStandings without it being a sendFinalStandings test. Test number: "+this.currentTest);
		}

	}
	
	/**
	 * Tests the send error was called with the correct parameters
	 * Test 14
	 * @param id of the tournament
	 * @param playerid the players id witht the error
	 * @param name of the error
	 * @param message of the error
	 */
	public void sendErrorTest(long id, String playerid, String name, String message)
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 14)
		{
			assertEquals(id, tid);
			assertEquals(playerid, p1.getUUID().toString());
			assertEquals(name, "NullPointer");
			assertEquals(message, "r is null");
			tests[14]=true;

		}
		else
		{
			fail("Entered SendFinalStandings without it being a sendFinalStandings test. Test number: "+this.currentTest);
		}

	}
	
	/**
	 * Tests the send begin new round was called with the correct parameters
	 * Test 15
	 * @param id of the tournament
	 * @param round the round to progress to
	 */
	public void sendBeginNewRoundTest(long id, int round)
	{
		//check all parameters are as expected for each test
		if(this.currentTest == 15)
		{
			assertEquals(id, tid);
			assertEquals(round, 1);
			tests[15]=true;

		}
		else
		{
			fail("Entered SendFinalStandings without it being a sendFinalStandings test. Test number: "+this.currentTest);
		}

	}
}
