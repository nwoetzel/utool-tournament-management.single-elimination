package utool.plugin.singleelimination.test;

import utool.plugin.singleelimination.TournamentLogic;

/**
 * This class is for testing the tournament logic class
 * This instance of tournament logic will allow for full testing of the base class
 * @author waltzm
 * @version 12/6/2012
 */
public class TournamentLogicForTests extends TournamentLogic
{

	/**
	 * Constructor for a TournamentLogicForTests class
	 * @param tournamentId unique id fo the tournament
	 */
	public TournamentLogicForTests(long tournamentId) 
	{
		this.tournamentId = tournamentId;  

	}
}
