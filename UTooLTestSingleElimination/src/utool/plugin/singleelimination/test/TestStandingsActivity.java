
/**
 * This tests a class that has since been deprecated. The tests therefore have not been updated to 
 * work with the new standings generator or tournament, thus it is commented out. 
 * Before release this class should be deleted.
 * 
 * @author waltzm
 * @version 12/29/2012
 */


//package utool.plugin.singleelimination.test;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import utool.plugin.Player;
//import utool.plugin.singleelimination.SingleEliminationStandingsActivity;
//import utool.plugin.singleelimination.SingleEliminationTournament;
//import utool.plugin.singleelimination.StandingsGeneratorSE;
//import utool.plugin.singleelimination.TournamentLogic;
//import android.content.Intent;
//import android.test.ActivityUnitTestCase;
//import android.view.View;
//import android.widget.Button;
//import android.widget.GridView;
//import android.widget.Spinner;
//import android.widget.TextView;
//import utool.plugin.singleelimination.R;
//
///**
// * This test class is meant to fully test the functionality of the SingleEliminationStandingsActivity.java class
// * @author Maria
// * @version 10/11/2012
// */
//public class TestStandingsActivity extends ActivityUnitTestCase<SingleEliminationStandingsActivity>{
//
//	/**
//	 * holds the tournament id
//	 */
//	private long tournamentId = 0;
//	
//	/**
//	 * Required constructor for Activity Tests
//	 * @since 10/11/2012
//	 */
//	public TestStandingsActivity() {
//		super(SingleEliminationStandingsActivity.class);
//	}
//
//	/**
//	 * The activity we are testing
//	 * @since 10/11/2012
//	 */
//	private SingleEliminationStandingsActivity mActivity;
//
//	//This method is invoked before every test
//	@Override
//	protected void setUp() throws Exception
//	{
//		super.setUp();
//		SingleEliminationTournament.clearInstance(tournamentId);
//		mActivity = getActivity();
//
//	}
//
//	/**
//	 * Tests that the various components are initialized properly
//	 * @since 10/11/2012
//	 */
//	public void testInitialization() 
//	{		
//		//make a list of players
//		ArrayList<Player> plyrs = new ArrayList<Player>();
//		plyrs.add(new Player(new UUID(0,0),"bob"));
//		plyrs.add(new Player(new UUID(0,1),"Tim"));
//		plyrs.add(new Player(new UUID(0,2),"John"));
//		plyrs.add(new Player(new UUID(0,3),"Tom"));
//		plyrs.add(new Player(new UUID(0,4),"Ned"));
//		plyrs.add(new Player(new UUID(0,5),"Alfred"));
//		plyrs.add(new Player(new UUID(0,6),"Thomas"));
//		plyrs.add(new Player(new UUID(0,7), "Shaun"));
//
//		TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
//
//		Intent i = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		i.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(i, null, null);
//
//		//Check that the Activity is not null
//		assertNotNull(mActivity);
//
//		//Check that instance variables were initialized
//		assertNotNull(mActivity.getItems());
//
//		//Check that Views were initialized
//		GridView g = (GridView)mActivity.findViewById(R.id.grid);
//		assertNotNull(g);
//
//		Spinner s = (Spinner)mActivity.findViewById(R.id.roundSpinner);
//		assertNotNull(s);
//
//		TextView text = (TextView)mActivity.findViewById(R.id.title);
//		assertNotNull(text);
//
//	}
//
//	/**
//	 * Tests that the displayed list of players is correctly formatted
//	 * @since 10/11/2012
//	 */
//	public void testDisplayedList() 
//	{
//		//make a list of players
//		ArrayList<Player> plyrs = new ArrayList<Player>();
//		plyrs.add(new Player(new UUID(0,0),"bob"));
//		plyrs.add(new Player(new UUID(0,1),"Tim"));
//		plyrs.add(new Player(new UUID(0,2),"John"));
//		plyrs.add(new Player(new UUID(0,3),"Tom"));
//		plyrs.add(new Player(new UUID(0,4),"Ned"));
//		plyrs.add(new Player(new UUID(0,5),"Alfred"));
//		plyrs.add(new Player(new UUID(0,6),"Thomas"));
//		plyrs.add(new Player(new UUID(0,7), "Shaun"));
//
//		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
//		//StandingsGeneratorSE.getInstance(plyrs);
//		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		intent.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(intent, null, null);
//
//		//Check that the number of grid view spaces is correct for the number of players
//		//gridspaces should be 6 + players*6
//		GridView g = (GridView)mActivity.findViewById(R.id.grid);
//		StandingsGeneratorSE gen = t.getStandingsGenerator();
//		assertEquals((gen.getPlayers().size()+1)*6, g.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(g.getItemAtPosition(0),"Player");
//		assertEquals(g.getItemAtPosition(1),"W");
//		assertEquals(g.getItemAtPosition(2),"L");
//		assertEquals(g.getItemAtPosition(3),"RW");
//		assertEquals(g.getItemAtPosition(4),"RL");
//		assertEquals(g.getItemAtPosition(5),"S");
//		int index = 6;
//		int curRound=gen.getRound();
//		List<Player> players = gen.getPlayers();
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(g.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(g.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(g.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(g.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(g.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(g.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//	}
//
//	/**
//	 * Tests that the round selector works as intended
//	 * @since 10/13/2012
//	 */
//	public void testRoundSelector() 
//	{
//		//make a list of players
//		ArrayList<Player> plyrs = new ArrayList<Player>();
//		plyrs.add(new Player(new UUID(0,0),"bob"));
//		plyrs.add(new Player(new UUID(0,1),"Tim"));
//		plyrs.add(new Player(new UUID(0,2),"John"));
//		plyrs.add(new Player(new UUID(0,3),"Tom"));
//		plyrs.add(new Player(new UUID(0,4),"Ned"));
//		plyrs.add(new Player(new UUID(0,5),"Alfred"));
//		plyrs.add(new Player(new UUID(0,6),"Thomas"));
//		plyrs.add(new Player(new UUID(0,7), "Shaun"));
//		
//		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
//
//		StandingsGeneratorSE g = t.getStandingsGenerator();
//
//		//add a round 
//		g.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
//		g.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
//		g.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
//		g.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);
//
//		g.progressToNextRound(2);
//
//		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		intent.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(intent, null, null);
//
//		//Check that the round selector is displaying the right choices for rounds
//		Spinner s = (Spinner)mActivity.findViewById(R.id.roundSpinner);
//
//		int curRound = g.getRound();
//		for(int i=0;i<curRound;i++)
//		{
//			assertEquals(s.getItemAtPosition(i), i+1+"");
//		}
//
//		//check again
//		curRound = g.getRound();
//		for(int i=0;i<curRound;i++)
//		{
//			assertEquals(s.getItemAtPosition(i), i+1+"");
//		}
//
//		//Check that the round switching correctly changes the values
//		s.getOnItemSelectedListener().onItemSelected(null, null, 1, 0);
//
//		GridView grid = (GridView)mActivity.findViewById(R.id.grid);
//		StandingsGeneratorSE gen = t.getStandingsGenerator();
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		int index = 6;
//		curRound=2;
//		List<Player> players = gen.getPlayers();
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//	}
//
//	/**
//	 * This test tests that the rearranging orderBy method works for standings and seed
//	 * @since 10/13/2012
//	 */
//	public void testOrderBy()
//	{
//		//make a list of players
//		ArrayList<Player> plyrs = new ArrayList<Player>();
//		plyrs.add(new Player(new UUID(0,0),"bob"));
//		plyrs.add(new Player(new UUID(0,1),"Tim"));
//		plyrs.add(new Player(new UUID(0,2),"John"));
//		plyrs.add(new Player(new UUID(0,3),"Tom"));
//		plyrs.add(new Player(new UUID(0,4),"Ned"));
//		plyrs.add(new Player(new UUID(0,5),"Alfred"));
//		plyrs.add(new Player(new UUID(0,6),"Thomas"));
//		plyrs.add(new Player(new UUID(0,7), "Shaun"));
//		
//		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
//
//		//record full game
//		StandingsGeneratorSE g = t.getStandingsGenerator();
//		g.resetScores();
//		//add a round 
//		g.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
//		g.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
//		g.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
//		g.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);
//
//		g.progressToNextRound(2);
//
//		g.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
//		g.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);
//		g.progressToNextRound(3);
//		g.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);
//
//		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		intent.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(intent, null, null);
//
//		GridView grid = (GridView)mActivity.findViewById(R.id.grid);
//
//		//default arrangement is Asc seed
//		//check that everything in the seed column > one before
//		String previous=(String) grid.getItemAtPosition(6);
//		for(int i=12;i<grid.getCount();i+=6)
//		{
//			if(((String)grid.getItemAtPosition(i)).compareTo(previous)<0)
//			{
//				fail("Item in list not in ascending order: "+grid.getItemAtPosition(i)+", "+previous);
//			}
//			previous = (String) grid.getItemAtPosition(i);
//		}
//
//		//check desc by seed
//
//		grid.getOnItemClickListener().onItemClick(null, null, 0, 0);
//		//check that everything in the seed column < one before
//		previous=(String) grid.getItemAtPosition(6);
//		for(int i=12;i<grid.getCount();i+=6)
//		{
//			if(((String)grid.getItemAtPosition(i)).compareTo(previous)>0)
//			{
//				fail("Item in list not in decending order: "+grid.getItemAtPosition(i)+", "+previous);
//			}
//			previous = (String) grid.getItemAtPosition(i);
//		}
//
//		//check clicking again goes back to asc
//		grid.getOnItemClickListener().onItemClick(null, null, 0, 0);
//		previous=(String) grid.getItemAtPosition(6);
//		for(int i=12;i<grid.getCount();i+=6)
//		{
//			if(((String)grid.getItemAtPosition(i)).compareTo(previous)<0)
//			{
//				fail("Item in list not in ascending order: "+grid.getItemAtPosition(i)+", "+previous);
//			}
//			previous = (String) grid.getItemAtPosition(i);
//		}
//
//		//check asc by standing	
//		grid.getOnItemClickListener().onItemClick(null, null, 5, 0);
//		//check that everything in the seed column > one before
//		previous=(String) grid.getItemAtPosition(11);
//		for(int i=17;i<grid.getCount();i+=6)
//		{
//			if(((String)grid.getItemAtPosition(i)).compareTo(previous)<0)
//			{
//				fail("Item in list not in ascending order: "+grid.getItemAtPosition(i)+", "+previous);
//			}
//			previous = (String) grid.getItemAtPosition(i);
//		}
//
//		//check desc by players
//		grid.getOnItemClickListener().onItemClick(null, null, 5, 0);
//		//check that everything in the seed column < one before
//		previous=(String) grid.getItemAtPosition(11);
//		for(int i=17;i<grid.getCount();i+=6)
//		{
//			if(((String)grid.getItemAtPosition(i)).compareTo(previous)>0)
//			{
//				fail("Item in list not in decending order: "+grid.getItemAtPosition(i)+", "+previous);
//			}
//			previous = (String) grid.getItemAtPosition(i);
//		}
//	}
//
//	/**
//	 * This test tests that the positions can be selected
//	 * @since 10/13/2012
//	 */
//	public void testPositionSelection()
//	{
//		//make a list of players
//		ArrayList<Player> plyrs = new ArrayList<Player>();
//		plyrs.add(new Player(new UUID(0,0),"bob"));
//		plyrs.add(new Player(new UUID(0,1),"Tim"));
//		plyrs.add(new Player(new UUID(0,2),"John"));
//		plyrs.add(new Player(new UUID(0,3),"Tom"));
//		plyrs.add(new Player(new UUID(0,4),"Ned"));
//		plyrs.add(new Player(new UUID(0,5),"Alfred"));
//		plyrs.add(new Player(new UUID(0,6),"Thomas"));
//		plyrs.add(new Player(new UUID(0,7), "Shaun"));
//		
//		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
//
//		//record full game
//		StandingsGeneratorSE gen = t.getStandingsGenerator();
//		gen.resetScores();
//		//add a round 
//		gen.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
//		gen.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
//		gen.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
//		gen.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);
//
//		gen.progressToNextRound(2);
//
//		gen.recordScore(new UUID(0,0), new UUID(0,2), 2, 3, 0,1);
//		gen.recordScore(new UUID(0,5), new UUID(0,7), 2, 2, 1,1);
//		gen.progressToNextRound(3);
//		gen.recordScore(new UUID(0,0), new UUID(0,5), 3, 3, 0,1);
//		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		intent.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(intent, null, null);
//
//		GridView grid = (GridView)mActivity.findViewById(R.id.grid);
//
//		//checking other positions and making sure they have no effect
//		grid.getOnItemClickListener().onItemClick(null, null, -1, 0);
//
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		int index = 6;
//		int curRound=3;
//		List<Player> players = gen.getPlayers();
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//		//checking other positions and making sure they have no effect
//		grid.getOnItemClickListener().onItemClick(null, null, 6, 0);
//
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		index = 6;
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//		//checking other positions and making sure they have no effect
//		grid.getOnItemClickListener().onItemClick(null, null, 1, 0);
//
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		index = 6;
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//		//checking other positions and making sure they have no effect
//		grid.getOnItemClickListener().onItemClick(null, null, 2, 0);
//
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		index = 6;
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//		//checking other positions and making sure they have no effect
//		grid.getOnItemClickListener().onItemClick(null, null, 3, 0);
//
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		index = 6;
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//
//		//checking other positions and making sure they have no effect
//		grid.getOnItemClickListener().onItemClick(null, null, 4, 0);
//
//		assertEquals((gen.getPlayers().size()+1)*6, grid.getCount());	
//
//		//Check that all the spaces have the expected value for the round
//		assertEquals(grid.getItemAtPosition(0),"Player");
//		assertEquals(grid.getItemAtPosition(1),"W");
//		assertEquals(grid.getItemAtPosition(2),"L");
//		assertEquals(grid.getItemAtPosition(3),"RW");
//		assertEquals(grid.getItemAtPosition(4),"RL");
//		assertEquals(grid.getItemAtPosition(5),"S");
//		index = 6;
//		for(int i = 0;i<players.size();i++)
//		{
//			assertEquals(grid.getItemAtPosition(index), (i+1)+". "+players.get(i).getName());
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundWins(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerRoundLosses(players.get(i).getUUID(),curRound)+"");
//			index++;
//			assertEquals(grid.getItemAtPosition(index), gen.getPlayerStanding(players.get(i).getUUID(), curRound)+"");
//			index++;
//		}
//	}
//	/**
//	 * Tests null players initially
//	 */
//	public void testAaaaaNull()
//	{
//		TournamentLogic.getInstance(tournamentId, null);
//		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		intent.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(intent, null, null);
//
//		assertNotNull(mActivity.getItems());
//		assertEquals(mActivity.getItems().length,6);
//
//		//check null passed to  order by
//		GridView grid = (GridView)mActivity.findViewById(R.id.grid);
//		grid.getOnItemClickListener().onItemClick(null, null, 0, 0);
//		grid.getOnItemClickListener().onItemClick(null, null, 0, 0);
//
//		assertNotNull(mActivity.getItems());
//		assertEquals(mActivity.getItems().length,6);
//
//		SingleEliminationTournament.clearInstance(tournamentId);
//	}
//
//	/**
//	 * This test tests that the spinner can be deselected
//	 * @since 10/13/2012
//	 */
//	public void testDeselection()
//	{
//		//make a list of players
//		ArrayList<Player> plyrs = new ArrayList<Player>();
//		plyrs.add(new Player(new UUID(0,0),"bob"));
//		plyrs.add(new Player(new UUID(0,1),"Tim"));
//		plyrs.add(new Player(new UUID(0,2),"John"));
//		plyrs.add(new Player(new UUID(0,3),"Tom"));
//		plyrs.add(new Player(new UUID(0,4),"Ned"));
//		plyrs.add(new Player(new UUID(0,5),"Alfred"));
//		plyrs.add(new Player(new UUID(0,6),"Thomas"));
//		plyrs.add(new Player(new UUID(0,7), "Shaun"));
//
//		SingleEliminationTournament t = (SingleEliminationTournament)TournamentLogic.getInstance(tournamentId, plyrs, null, Player.HOST);
//
//		StandingsGeneratorSE g = t.getStandingsGenerator();
//
//		//add a round 
//		g.recordScore(new UUID(0,0), new UUID(0,1), 1, 3, 0,1);
//		g.recordScore(new UUID(0,2), new UUID(0,3), 1, 2, 1,1);
//		g.recordScore(new UUID(0,4), new UUID(0,5), 1, 1, 2,1);
//		g.recordScore(new UUID(0,6), new UUID(0,7), 1, 0, 3,1);
//
//		g.progressToNextRound(2);
//
//		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		intent.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(intent, null, null);
//
//		//Check that the round selector is displaying the right choices for rounds
//		Spinner s = (Spinner)mActivity.findViewById(R.id.roundSpinner);
//
//		s.getOnItemSelectedListener().onNothingSelected(null);
//
//	}
//
//	/**
//	 * Tests the final tournament standings button initialization with a true Final_Standings intent on the SingleEliminationStandingsActivity
//	 * @since 10/13/2012
//	 * @author Justin Kreier
//	 */
//	public void testDoneButtonInitialization()
//	{
//		TournamentLogic.getInstance(tournamentId, null);
//		//true extra
//		Intent start = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		start.putExtra("Final_Standings", true);
//		mActivity = this.startActivity(start, null, null);
//
//		Button b = (Button)mActivity.findViewById(R.id.doneButton);
//		assertEquals(View.VISIBLE, b.getVisibility());
//
//
//		//listener not null
//		assertTrue(b.performClick()); //returns false if no click was set
//		assertTrue(isFinishCalled());
//		assertTrue(SingleEliminationStandingsActivity.isPluginClosing);
//
//	}
//
//	/**
//	 * Tests the final tournament standings button initialization with a false Final_Standings intent on the SingleEliminationStandingsActivity
//	 * @since 10/13/2012
//	 * @author Justin Kreier
//	 */
//	public void testDoneButtonInitialization2()
//	{
//		TournamentLogic.getInstance(tournamentId, null);
//		//false extra
//		Intent start = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		start.putExtra("Final_Standings", false);
//		mActivity = this.startActivity(start, null, null);
//
//		Button b = (Button)mActivity.findViewById(R.id.doneButton);
//		assertEquals(View.GONE, b.getVisibility());
//
//
//
//	}
//
//	/**
//	 * Tests the final tournament standings button initialization with no Final_Standings intent on the SingleEliminationStandingsActivity
//	 * @since 10/13/2012
//	 * @author Justin Kreier
//	 */
//	public void testDoneButtonInitialization3()
//	{
//		TournamentLogic.getInstance(tournamentId, null);
//		//no extra
//		Intent start = new Intent(this.getInstrumentation().getTargetContext(), SingleEliminationStandingsActivity.class);
//		start.putExtra("tournamentId", tournamentId);
//		mActivity = this.startActivity(start, null, null);
//
//		Button b = (Button)mActivity.findViewById(R.id.doneButton);
//		assertEquals(View.GONE, b.getVisibility());
//	}
//
//
//}
