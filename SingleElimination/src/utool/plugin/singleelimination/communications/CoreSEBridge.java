package utool.plugin.singleelimination.communications;

import utool.plugin.singleelimination.MatchupsActivity;


/**
 * This class is used to capture output of the outgoing command handler and send it to the main activity
 * if there is one registered
 * @author waltzm
 *
 */
public class CoreSEBridge 
{
//	/**
//	 * Holds the singleton reference
//	 */
//	private static CoreSEBridge server;

	/**
	 * Holds a reference to the main activity that has the connection to the core
	 */
	private MatchupsActivity mainActivity;

	/**
	 * Last XML to be sent out
	 */
	private String lastXML="";


//	/**
//	 * Returns a reference to the singleton CoreSEBridge
//	 * @return the CoreSEBridge instance
//	 */
//	public static CoreSEBridge getInstance()
//	{
//		if(server==null)
//		{
//			server= new CoreSEBridge();
//		}
//
//		return server;
//
//	}

	/**
	 * Notifies this class when a new message is to be sent out to the server
	 * This class will notify the main activity if it isn't null
	 * @param message the xml to send
	 * @return true if send is successful
	 */
	public boolean sendMessage(String message) 
	{

		//send message to log
		//Log.i("Bridge", message);
		this.lastXML = message;

		if(mainActivity!=null)
		{

			
			mainActivity.sendMessage(message);
			
			//TODO remove when connection works (next two lines make a toast to the screen to show communication being sent)
//			SaxFeedParser s = new SaxFeedParser(new IncomingCommandHandlerHost());
//			s.parse(message);
//			mainActivity.displayMessage(message);
			//TODO remove end

			
			return true;
		}


		return false;
	}


	/**
	 * Private constructor to maintain singleton
	 */
	public CoreSEBridge()
	{

	}

	/**
	 * Getter for the last xml sent
	 * @return the lastXML
	 */
	public String getLastXML() 
	{
		return lastXML;
	}

	/**
	 * Getter for the main activity with the core connection
	 * @return the mainActivity
	 */
	public MatchupsActivity getMainActivity() 
	{
		return mainActivity;
	}

	/**
	 * Setter for the main activity with the core connection
	 * @param mainActivity the mainActivity to set
	 */
	public void setMainActivity(MatchupsActivity mainActivity) 
	{
		this.mainActivity = mainActivity;
	}

	/**
	 * Sends a message to the main screen to display as a toast
	 * @param s the string to display
	 * @return true if a main activity is connected
	 */
	public boolean sendMessageToScreen(String s)
	{
		if(mainActivity!=null)
		{
			mainActivity.displayMessage(s);
			return true;
		}
		
		return false;
	}

	/**
	 * Clears the last message. Sets it to null
	 */
	public void clearLastMessage() 
	{
		this.lastXML = null;
		
	}




}
