package utool.plugin.singleelimination.communications;

import java.util.ArrayList;
import java.util.UUID;

import android.util.Log;
import utool.plugin.Player;
import utool.plugin.singleelimination.Matchup;
import utool.plugin.singleelimination.MatchupsActivity;
import utool.plugin.singleelimination.PlayerNotExistantException;
import utool.plugin.singleelimination.SingleEliminationTournament;

/**
 * When a message is received from the host, the message will be parsed and then will call the IncomingCommandHandler? method corresponding to 
 * the message received. The IncomingCommandHandler is responsible for notifying the appropriate part of the SE plugin that a message has been received.
 * 
 * This handler is for a host's SE plugin to have the messages received be pushed to its internal state
 * The only message that matters atm is the error command received
 * @author waltzm
 * @author hoguet
 * @version 3/22/13
 */
public class IncomingCommandHandlerHost extends AbstractIncomingCommandHandler
{

	/**
	 * A reference to the tournament's matchups activity used for refreshing UI with changes
	 */
	private MatchupsActivity activity;

	/**Constructs an IncomingCommandHandlerHost and passes the t to its parent
	 * @param t the tournament to update
	 * @param a the Matchups Activity associated with this tournament
	 */
	public IncomingCommandHandlerHost(SingleEliminationTournament t, MatchupsActivity a) 
	{
		super(t);
		this.activity = a;
	}

	@Override
	public void handleChangeMatchup(long id, long matchid, String team1name, String team2Name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		super.handleChangeMatchup(id, matchid, team1name, team2Name, team1, team2, round, table);
	}

	@Override
	public void handleSendBeginNewRound(long id, int round)
	{
		super.handleSendBeginNewRound(id, round);
	}

	@Override
	public void handleSendError(long id, String playerid, String name, String message)
	{
		super.handleSendError(id, playerid, name, message);

		Log.e("ICH_Host", "tournId: "+id);
		//if error is received, send clear and then resend everything
		SingleEliminationTournament t = (SingleEliminationTournament) this.t;

		if(t == null)
		{
			Log.e("OCH_Host","ERROR, tournament doesn't exist. ");
			return;
		}
		OutgoingCommandHandler oc = t.getOutgoingCommandHandler();

		//send clear for tid
		oc.handleSendClear(id);

		//re-send players
		ArrayList<Player> p = t.getPlayers();

		Player[] players = p.toArray(new Player[p.size()]); 

		oc.handleSendPlayers(id, players);

		//re-send matches with round breaks
		//		ArrayList<Matchup> matches = t.getBottomRound();
		//		Log.e("matches", matches.toString());
		//Send all matches and set round
		sendAllMatches(id, oc);


	}

	/**
	 * Sends all matchups to participants
	 * @param id of the tournament
	 * @param oc outgoing command handler to send messages with
	 */
	private void sendAllMatches(long id, OutgoingCommandHandler oc){

		String[] team1 = new String[1];
		String[] team2 = new String[1];


		for(int i = 1; i < t.getRound(); i++){

			for(Matchup m : t.getMatchups()){

				int round = (m.getRoundParticipant() > 0) ? m.getRoundParticipant() : m.getRound();

				if(round == i){
					//send matchup
					team1[0] = "null";
					team2[0] = "null";

					if(m.getPlayerOne() != null){
						team1[0] = m.getPlayerOne().getUUID().toString();
					}
					if(m.getPlayerTwo() != null){
						team2[0] = m.getPlayerTwo().getUUID().toString();
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					oc.handleSendMatchup(id, m.getId(), null, null, team1, team2, round, null);

					//send score for match
					double[] scores =  m.getScores();

					if(scores != null){
						oc.handleSendScore(id, m.getId(), m.getPlayerOne().getUUID().toString(), m.getPlayerTwo().getUUID().toString(),scores[0], scores[1], round);
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			
			oc.handleSendBeginNewRound(id, i+1);


		}
		
		for(Matchup m : t.getMatchups()){

			int round = (m.getRoundParticipant() > 0) ? m.getRoundParticipant() : m.getRound();

			if(round >= t.getRound()){
				//send matchup
				team1[0] = "null";
				team2[0] = "null";

				if(m.getPlayerOne() != null){
					team1[0] = m.getPlayerOne().getUUID().toString();
				}
				if(m.getPlayerTwo() != null){
					team2[0] = m.getPlayerTwo().getUUID().toString();
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				oc.handleSendMatchup(id, m.getId(), null, null, team1, team2, round, null);

				//send score for match
				double[] scores =  m.getScores();

				if(scores != null){
					oc.handleSendScore(id, m.getId(), m.getPlayerOne().getUUID().toString(), m.getPlayerTwo().getUUID().toString(),scores[0], scores[1], round);
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	@Override
	public void handleSendFinalStandings(String id, ArrayList<String> players, ArrayList<Double> w, ArrayList<Double> l, ArrayList<Integer> rw, ArrayList<Integer> rl, ArrayList<Integer> s)
	{
		super.handleSendFinalStandings(id, players, w, l, rw, rl, s);
	}

	@Override
	public void handleSendMatchup(long id, long matchid, long parentId, String team1name, String team2Name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		super.handleSendMatchup(id, matchid, parentId, team1name, team2Name, team1, team2, round, table);
	}

	@Override
	public void handleSendPlayers(String id, ArrayList<Player> players)
	{
		super.handleSendPlayers(id, players);
		Log.i("INCOMING", "players");

		activity.compareAndResolvePlayers(players);		

	}

	@Override
	public void handleSendRoundTimerAmount(long id, String time)
	{
		super.handleSendRoundTimerAmount(id, time);
	}

	@Override
	public void handleSendScore(long id, long matchid, String player1, String player2, String score1, String score2, int round)
	{
		super.handleSendScore(id, matchid, player1, player2, score1, score2, round);

		//received score from moderator
		double score1d;
		double score2d;
		try{
			score1d = Double.parseDouble(score1);
			score2d = Double.parseDouble(score2);
		}catch(NumberFormatException e){
			Log.e("Incoming Command Handler", "Illegal score value received.");
			return;
		}
		
		//update the tournament matchup
		Matchup m = Matchup.getMatchupById(t.getMatchups(), matchid);
		if(m != null){
			m.setScores(score1d, score2d);
		}
		
		//report score in tournament and update self.. is done from m.setScores method
//		try 
//		{
//			t.getStandingsGenerator().recordScore(UUID.fromString(player1), UUID.fromString(player2), round,  score1d,  score2d, matchid);
//		} 
//		catch (PlayerNotExistantException e) 
//		{
//			//don't record the score since invalid
//			Log.e("Incoming Command Handler", "Moderator recorded a score with an invalid player id");
//			return;
//		}

		//send score to all players
		t.getOutgoingCommandHandler().handleSendScore(id, matchid, player1, player2, score1d,  score2d, round);

		//update GUI
		activity.refreshMatchupsList(null,  null);
		activity.updateRoundDisplay();
	}

	@Override
	public void handleSendTournamentName(long id, String name)
	{
		super.handleSendTournamentName(id, name);
	}

	@Override
	public void handleSendClear(long tid) 
	{
		super.handleSendClear(tid);		
	}

}
