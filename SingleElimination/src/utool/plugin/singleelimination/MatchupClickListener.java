package utool.plugin.singleelimination;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * A custom OnClickListener class that is able to reference the matchup, view, and layout
 * @author hoguet
 *
 */
public class MatchupClickListener implements OnClickListener{

	/**
	 * The matchup that this listener is associated with
	 */
	private Matchup matchup;

	/**
	 * The view containing the element that uses this listener
	 */
	private View view;

	/**
	 * The view that contains fields that may be modified by this listener
	 */
	private View layout;

	/**
	 * 
	 * @param m the matchup that this listener is associated with
	 * @param v the view containing the element that uses this listener
	 * @param layout the view that contains fields that may be modified by this listener
	 */
	public MatchupClickListener(Matchup m, View v, View layout) {
		this.matchup = m;
		this.view = v;
		this.layout = layout;
	}


	/**
	 * Expects to be overridden and implemented in-line when this class is used
	 */
	public void onClick(View v) {

	}


	/**
	 * @return matchup
	 */
	public Matchup getMatchup(){
		return matchup;
	}

	/**
	 * @return view
	 */
	public View getView(){
		return view;
	}

	/**
	 * @return layout
	 */
	public View getLayout(){
		return layout;
	}
}