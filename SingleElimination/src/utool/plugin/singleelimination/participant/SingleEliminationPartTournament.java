package utool.plugin.singleelimination.participant;

import java.util.ArrayList;
import java.util.UUID;
import utool.plugin.Player;
import utool.plugin.singleelimination.ParticipantTournamentActivity;
import utool.plugin.singleelimination.StandingsGeneratorSE;
import utool.plugin.singleelimination.TournamentLogic;
import android.util.Log;


/**
 * Data class that represents a Single Elimination Tournament
 * for a participant.
 * 
 * @author waltzm
 * @version 12-3-2012
 */
public class SingleEliminationPartTournament extends TournamentLogic
{


	/**
	 * holds the list of rounds
	 */
	private ArrayList<String> groups;

	/**
	 * Holds the matches for each round
	 */
	private ArrayList<ArrayList<Match>> children;

	/**
	 * Holds a reference to the sample activity running
	 */
	private ParticipantTournamentActivity activity;

	/**
	 * Constructor that initializes the tournament informationm
	 * @param tournamentId unique id of the tournament
	 * @param players the players
	 */
	public SingleEliminationPartTournament(long tournamentId, ArrayList<Player> players) 
	{
		this.tournamentId = tournamentId;  
		this.players = players;
		groups = new ArrayList<String>();
		children = new ArrayList<ArrayList<Match>>();
		if(this.players==null)
		{
			this.players = new ArrayList<Player>();
		}
		this.setPermissionLevel(Player.PARTICIPANT);
	}

	/**
	 * Getter for the groups (names of rounds)
	 * @return groups
	 */
	public ArrayList<String> getGroups()
	{
		ArrayList<String> copy = new ArrayList<String>();
		for(int i=0;i<groups.size();i++)
		{
			copy.add(groups.get(i));
		}
		return groups;
	}

	/**
	 * Getter for the children (matches for each round)
	 * @return children
	 */
	public ArrayList<ArrayList<Match>> getChildren()
	{
		//shallow copy
		ArrayList<ArrayList<Match>> copy = new ArrayList<ArrayList<Match>>();
		for(int i=0;i<children.size();i++)
		{
			copy.add(new ArrayList<Match>());
			for(int j=0;j<children.get(i).size();j++)
			{
				copy.get(i).add(children.get(i).get(j));
			}
		}
		return children;
	}

	/**
	 * Clears the instance and the standings generator
	 */
	public void clearInstance()
	{
		groups = new ArrayList<String>();
		children = new ArrayList<ArrayList<Match>>();
		
		//reset standings gen
		synchronized (this) {
			if (standingsGenerator == null){
				standingsGenerator = new StandingsGeneratorSE(tournamentId, new ArrayList<Player>());
			}
		}
	}

	/**
	 * Setter for the score of a (already created) match
	 * @param mid unique match id  of the match to set a score for
	 * @param score1 the first players's score
	 * @param score2 the second player's score
	 * @return true if score set, false otherwise
	 */
	public boolean setScore(long mid, double score1, double score2)
	{
		Log.w("Setting score","Scores: "+score1+", "+score2);

		boolean ret = updateChildMatch(mid,score1,score2);


		//update adapter matches
		if(activity!=null)
		{
			activity.resetAdapter();
		}

		return ret;
	}

	/**
	 * Private method to internal update the children data structure for a score being set
	 * @param mid2 id of the match
	 * @param score1 player 1's score in the match
	 * @param score2 player 2's score in the match
	 * @return true if match updates, false otherwise
	 */
	private boolean updateChildMatch(long mid2, double score1, double score2)
	{
		Log.w("beginning to update","Scores: "+score1+", "+score2);
		//look through children for the match, and update it
		for(int i = 0;i<children.size();i++)
		{
			if(children.get(i)!=null)
			{
				int size = children.get(i).size();

				for(int j=0;j<size;j++)
				{
					if(children.get(i).get(j).getId() == mid2)
					{
						//found the correct match
						Log.w("Updating scores","Scores: "+score1+", "+score2);
						//update scores
						children.get(i).get(j).setScores(score1, score2);
						return true;
					}
				}
			}
		}
		return false;

	}

	/**
	 * Getter for a match
	 * @param mid unique id of the match
	 * @return the Match
	 */
	public Match getMatch(long mid)
	{
		for(int i = 0;i<children.size();i++)
		{
			if(children.get(i)!=null)
			{
				int size = children.get(i).size();

				for(int j=0;j<size;j++)
				{
					if(children.get(i).get(j).getId() == mid)
					{

						return children.get(i).get(j);
					}
				}
			}
		}

		//no match found so return null
		return null;
	}

	/**
	 * Adds a match to the tournament
	 * @param mid the id of the match
	 * @param uuid first player's uuid
	 * @param uuid2 second player's uuid
	 * @param round the round of the match
	 * @return true if added, false otherwise
	 */
	public boolean addMatchup(long mid, UUID uuid, UUID uuid2, int round)
	{
		//make sure the match id is unique
		for(int i=0;i<children.size();i++)
		{
			//look at each index of children fro the match id
			if(doesContain(children.get(i), new Match(mid, null, null, 1))!=-1)
			{
				//if match already exists, return false
				return false;
			}

		}
		//make sure the players are valid
		int i1 = -1;
		int i2 = -1;
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getUUID().equals(uuid))
			{
				i1=i;
			}
			if(players.get(i).getUUID().equals(uuid2))
			{
				i2=i;
			}
		}

		if((i1==-1&&!uuid.equals(Player.BYE))||(i2==-1&&!uuid2.equals(Player.BYE)))
		{
			//one of the players is nonexistant and thats an issue
			//TODO send error msg
			Log.e("SE_Tourn","Matchup added with invalid player");
			return false;
		}

		Player one;
		Player two;

		if(i1!=-1)
		{
			//p1 is not a bye
			if(i2!=-1)
			{
				//neither are byes
				one = players.get(i1);
				two = players.get(i2);
			}
			else
			{
				//p2 is a bye, p1 isn't
				one = players.get(i1);
				two = new Player(Player.BYE,"Bye");
			}
		}
		else
		{
			//p1 is a bye
			if(i2==-1)
			{
				//both are byes
				one = new Player(Player.BYE,"Bye");
				two = new Player(Player.BYE,"Bye");
			}
			else
			{
				//p1 is a bye
				one = new Player(Player.BYE,"Bye");
				two = players.get(i2);
			}
		}

		//update groups and children
		Match match = new Match(mid, one, two, round);
		if (!groups.contains("Round "+match.getRound())) 
		{
			groups.add("Round "+match.getRound());

		}

		int index = groups.indexOf("Round "+match.getRound());

		if (children.size() < index + 1) 
		{
			children.add(new ArrayList<Match>());	
		}

		//Log.d("ExpandableListAdapter","Children: " +children);
		//determine if child exists before adding. if it does replace it
		int i = doesContain(children.get(index),match);
		if(i!=-1)
		{
			Log.e("ExpandableListAdapter","Replacing child: " +i);
			//vehicle must be replaced
			children.get(index).set(i,match);

		}
		else
		{
			children.get(index).add(match);
		}

		//redraw listview
		if(activity!=null)
		{
			activity.resetAdapter();
		}

		return true;

	}


	/**
	 * Edits a match that was already created
	 * @param mid match id of the match to alter
	 * @param uuid the first player's uuid
	 * @param uuid2 the second player's UUID
	 * @param round the round of the match
	 * @return true if the match exists, 
	 * false if operation failed since original match didn't exist
	 */
	public boolean editMatchup(long mid, UUID uuid, UUID uuid2, int round)
	{
		int childRow = -1;
		int childCol = -1;
		//make sure the match id exists already
		for(int i=0;i<children.size();i++)
		{
			int contain = doesContain(children.get(i), new Match(mid, null, null, 1));
			
			//look at each index of children fro the match id
			if(contain!=-1)
			{
				childRow = i;
				childCol = contain;
			}

		}
		
		if(childRow<0||childCol<0)
		{
			//mid didnt exist
			return false;
		}
		
		
		//make sure the players are valid
		int i1 = -1;
		int i2 = -1;
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getUUID().equals(uuid))
			{
				i1=i;
			}
			if(players.get(i).getUUID().equals(uuid2))
			{
				i2=i;
			}
		}

		if((i1==-1&&!uuid.equals(Player.BYE))||(i2==-1&&!uuid2.equals(Player.BYE)))
		{
			//one of the players is nonexistant and thats an issue
			//TODO send error msg
			Log.e("SE_Tourn","Matchup added with invalid player");
			return false;
		}

		Player one;
		Player two;

		if(i1!=-1)
		{
			//p1 is not a bye
			if(i2!=-1)
			{
				//neither are byes
				one = players.get(i1);
				two = players.get(i2);
			}
			else
			{
				//p2 is a bye, p1 isn't
				one = players.get(i1);
				two = new Player(Player.BYE,"Bye");
			}
		}
		else
		{
			//p1 is a bye
			if(i2==-1)
			{
				//both are byes
				one = new Player(Player.BYE,"Bye");
				two = new Player(Player.BYE,"Bye");
			}
			else
			{
				//p1 is a bye
				one = new Player(Player.BYE,"Bye");
				two = players.get(i2);
			}
		}

		
		//match = new Match(mid, one, two, round);
		children.get(childRow).set(childCol,new Match(mid, one,two,round));

		Match match = children.get(childRow).get(childCol);
		
		//update groups and children
		if (!groups.contains("Round "+match.getRound())) 
		{
			groups.add("Round "+match.getRound());

		}

		int index = groups.indexOf("Round "+match.getRound());

		if (children.size() < index + 1) 
		{
			children.add(new ArrayList<Match>());	
		}

		//Log.d("ExpandableListAdapter","Children: " +children);
		//determine if child exists before adding. if it does replace it
		int i = doesContain(children.get(index),match);
		if(i!=-1)
		{
			Log.e("ExpandableListAdapter","Replacing child: " +i);
			//vehicle must be replaced
			children.get(index).set(i,match);

		}
		else
		{
			children.get(index).add(match);
		}

		//redraw listview
		if(activity!=null)
		{
			activity.resetAdapter();
		}

		return true;
	}

	/**
	 * Getter for sampleactivity that is displaying
	 * @return the activity
	 */
	public ParticipantTournamentActivity getActivity() {
		return activity;
	}

	/**
	 * Setter for the activity to connect to
	 * @param activity the activity to set
	 */
	public void setActivity(ParticipantTournamentActivity activity) {
		this.activity = activity;
	}

	/**
	 * Determines if a list contains a given match
	 * @param list the list to look for the match within
	 * @param item the item to look for
	 * @return index of the item, or -1 if not found
	 */
	private int doesContain(ArrayList<Match> list, Match item)
	{
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i).getId() == item.getId())
			{
				return i;
			}
		}
		return -1;

	}

	/**
	 * Getter for the Matches in the given round
	 * Shallow copy of the matches.
	 * @param round the round to get the matches
	 * @return the matches
	 */
	public ArrayList<Match> getMatches(int round) 
	{
		ArrayList<Match> temp = children.get(round-1);
		ArrayList<Match> copy = new ArrayList<Match>();
		
		for(int i=0;i<temp.size();i++)
		{
			copy.add(temp.get(i));
		}
		
		return copy;
	}

}
