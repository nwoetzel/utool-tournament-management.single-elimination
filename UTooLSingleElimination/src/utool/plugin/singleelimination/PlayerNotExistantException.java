package utool.plugin.singleelimination;


/**
 * Exception for Player not being found
 * @author waltzm
 * @version 12/29/2012
 */
public class PlayerNotExistantException extends Exception 
{

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -2681657925662811972L;

	/**
	 * Player Not found, send the message
	 * @param message the messgae to display
	 */
	public PlayerNotExistantException(String message)
	{
		super(message);
	}

}
