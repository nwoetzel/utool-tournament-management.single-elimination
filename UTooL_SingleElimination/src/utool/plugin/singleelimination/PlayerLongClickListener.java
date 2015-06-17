package utool.plugin.singleelimination;

import utool.plugin.Player;
import android.view.View;
import android.view.View.OnLongClickListener;

public class PlayerLongClickListener implements OnLongClickListener{
	
	private Player player;
	private Matchup matchup;
	
	public PlayerLongClickListener(Player p, Matchup m){
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
	public boolean onLongClick(View v) {
		//Expects to be overridden inline
		return false;
	}

}
