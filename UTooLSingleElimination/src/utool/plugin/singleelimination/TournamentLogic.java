package utool.plugin.singleelimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import utool.plugin.Player;
import utool.plugin.singleelimination.communications.CoreSEBridge;
import utool.plugin.singleelimination.communications.OutgoingCommandHandler;
import utool.plugin.singleelimination.email.AutomaticMessageHandler;

/**
 * Abstract class that defines a tournament
 * All tournament logic classes for single elimination must extend this
 * @author waltzm
 * @version 12/6/2012
 */
public abstract class TournamentLogic 
{

	/**
	 * HashMap storing instances for all tournaments
	 */
	protected static HashMap<Long, TournamentLogic> tournamentInstances = new HashMap<Long, TournamentLogic>();

	/**
	 * Holds whether or not the user of the SE plugin is the host of the tournament or not.
	 * Used to determine permissions in the Commandhandlers
	 */
	private int  permissionLevel = Player.PARTICIPANT;
	
	/**
	 * Holds a reference to teh tournament's email handler
	 */
	private AutomaticMessageHandler emailHandler;

	/**
	 * Used to send messages to the core through the main activity registered
	 */
	public CoreSEBridge bridge = new CoreSEBridge();

	/**
	 * The standings generator associated with this tournament
	 */
	protected StandingsGeneratorSE standingsGenerator;

	/**
	 * The OutgoingCommandHandler for this tournament
	 */
	protected OutgoingCommandHandler outgoingCommandHandler = new OutgoingCommandHandler(this);

	/**
	 * The tournament's id
	 */
	protected long tournamentId;

	/**
	 * List of players in the tournament
	 */
	protected List<Player> players;


	/**
	 * The tournament's name
	 */
	protected String tournamentName = null;

	/**
	 * The player's uuid
	 */
	public UUID pid = new UUID(-1,-1);


	/**
	 * Get the tournament's id
	 * @return The tournament's id
	 */
	public long getTournamentId(){
		return tournamentId;
	}

	/**
	 * Get the tournament name. The host will append the tournament id, clients will take what the host gives them.
	 * @param includeIdIfHost Include the tournament id if this is a host device
	 * @return The tournament name, maybe with :id appended
	 */
	public String getTournamentName(boolean includeIdIfHost){
		return tournamentName;
	}


	/**
	 * @return ArrayList of Players in the tournament
	 */
	public List<Player> getPlayers(){
		return players;
	}

	/**
	 * Set the tournament's name
	 * @param name The tournament's name
	 */
	public void setTournamentName(String name){
		tournamentName = name;
	}

	/**
	 * Get the tournament's standings generator
	 * @return Instance of a StandingsGeneratorSE for this tournament
	 */
	public StandingsGeneratorSE getStandingsGenerator(){
		if (standingsGenerator == null){
			synchronized (this) {
				if (standingsGenerator == null){
					standingsGenerator = new StandingsGeneratorSE(tournamentId, players);
				}
			}
		}
		return standingsGenerator;
	}

	/**
	 * Get the tournament's email handler 
	 * @return Instance of an email handler
	 */
	public AutomaticMessageHandler getAutomaticMessageHandler(){
		if (emailHandler == null){
			synchronized (this) {
				if (emailHandler == null){
					emailHandler = new AutomaticMessageHandler(tournamentId);
				}
			}
		}
		return emailHandler;
	}

	
	/**
	 * Get the tournament's OutgoingCommandHandler
	 * @return OutgoingCommandHandler for this tournament
	 */
	public OutgoingCommandHandler getOutgoingCommandHandler()
	{
		if(outgoingCommandHandler == null)
		{
			synchronized (this) {
				if (outgoingCommandHandler == null){
					outgoingCommandHandler = new OutgoingCommandHandler(this);
				}
			}

		}
		return outgoingCommandHandler;
	}

	/**
	 * Set tournament players to the given list
	 * @param players The players to add
	 */
	public void setPlayers(ArrayList<Player> players){
		this.players = players;
	}

	/**
	 * Method for getting singleton instance of this SingleEliminationTournament
	 * This method does not attempt to create the instance, so if it does not exist, this will return null
	 * @param tournamentId The tournament id of the single elimination instance to retrieve
	 * @return the singleton instance of this tournament or null if it has not been created yet
	 */
	public static TournamentLogic getInstance(long tournamentId){
		return tournamentInstances.get(tournamentId);
	}

	/**
	 * Method for getting singleton instance of this SingleEliminationTournament
	 * This method will construct the tournament using the matchups given if the instance has not been created yet
	 * @param tournamentId The tournament id of the instance
	 * @param matchups the matchups of the tournament
	 * @return the singleton instance of this tournament or NULL if no matchups are provided and the instance has not been created yet.
	 */
	public static TournamentLogic getInstance(long tournamentId, ArrayList<Matchup> matchups){
		TournamentLogic tournament = tournamentInstances.get(tournamentId);

		if(tournament == null){
			tournament = new SingleEliminationTournament(tournamentId, matchups);
			tournamentInstances.put(tournamentId, tournament);
		}
		return tournament;
	}

	/**
	 * Method for getting singleton instance of this SingleEliminationTournament
	 * This method will construct the tournament using the players and matchups given if the instance has not been created yet
	 * @param tournamentId The tournament id of the instance
	 * @param players the players of the tournament
	 * @param matchups the matchups of the tournament
	 * @param permissionLevel the level of permission of the participant
	 * @return the singleton instance of this tournament or NULL if no players or matchups are provided and the instance has not been created yet.
	 */
	public static  TournamentLogic getInstance(long tournamentId, List<Player> players, ArrayList<Matchup> matchups, int permissionLevel)
	{
		TournamentLogic tournament = tournamentInstances.get(tournamentId);

		if(tournament == null){			
			tournament = new SingleEliminationTournament(tournamentId, players, matchups);
			tournament.setPermissionLevel(permissionLevel);
			
			tournament.standingsGenerator = new StandingsGeneratorSE(tournamentId, players);
			tournamentInstances.put(tournamentId, tournament);
		}
		return tournament;
	}     

	/**
	 * Clears the singleton instance of this tournament
	 * @param tournamentId The tournament id of the instance
	 */
	public static void clearInstance(long tournamentId){
		tournamentInstances.remove(tournamentId);
	}

	/**
	 * Getter for the permission level
	 * @return permission level
	 */
	public int getPermissionLevel() {
		return permissionLevel;
	}

	/**
	 * Setter for the permission level
	 * @param permissionLevel the level of permissions of the player in this tournament
	 */
	public void setPermissionLevel(int permissionLevel) {
		this.permissionLevel = permissionLevel;
	}
}
