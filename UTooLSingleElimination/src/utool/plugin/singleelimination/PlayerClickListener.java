package utool.plugin.singleelimination;

import utool.plugin.Player;
import android.view.View;
import android.view.View.OnClickListener;

public class PlayerClickListener implements OnClickListener {
	
	private Player player;
	private Matchup matchup;
	
	public PlayerClickListener(Player p, Matchup m){
		this.player = p;
		this.matchup = m;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public Matchup getMatchup(){
		return matchup;
	}

	@Override
	public void onClick(View v) {
		//Expects to be overridden inline
	}

}
