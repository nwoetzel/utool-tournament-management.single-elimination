package utool.plugin.singleelimination.participant;

import java.util.ArrayList;

import utool.plugin.Player;
import utool.plugin.singleelimination.OverallStandingsActivity;
import utool.plugin.singleelimination.R;
import utool.plugin.singleelimination.TournamentLogic;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Used to display the rounds and matches in SampleActivity
 * Connected to an expandable list
 * @author waltzm
 * @version 12/3/2012
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

	/**
	 * Holds the context
	 */
	private Context context;
	
	/**
	 * Holds the tournament id
	 */
	private long tid;

	

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}


	/**
	 * Constructor that takes in and saves the context
	 * @param context the app context
	 * @param tid the tournament id
	 */
	public ExpandableListAdapter(Context context, long tid)
	{
		this.context = context;
		this.tid = tid;
	}

	public Object getChild(int groupPosition, int childPosition) 
	{
		ArrayList<ArrayList<Match>> children = ((SingleEliminationPartTournament)(TournamentLogic.getInstance(tid))).getChildren();

		return children.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// Return a child view. You can load your custom layout here.
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) 
	{
		Match vehicle = (Match) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.child_layout, null);
		}

		//setup individual row
		//if childPosition is even than use gray background
		ImageView bg = (ImageView) convertView.findViewById(R.id.child_background);

		if(childPosition%2!=0)
		{
			bg.setImageDrawable(parent.getResources().getDrawable(R.drawable.bracket5gray));
		}
		else
		{
			bg.setImageDrawable(parent.getResources().getDrawable(R.drawable.bracket5));
		}
		//set p1 name
		TextView p1 = (TextView) convertView.findViewById(R.id.player1name);
		p1.setText("   " + vehicle.getPlayerOne().getName());
		//set p2 name
		TextView p2 = (TextView) convertView.findViewById(R.id.player2name);
		p2.setText("   " + vehicle.getPlayerTwo().getName());

		//set score1
		EditText s1 = (EditText) convertView.findViewById(R.id.player1score);
		s1.setText("" + vehicle.getScoreP1());

		//set score2
		EditText s2 = (EditText) convertView.findViewById(R.id.player2score);
		s2.setText(""+vehicle.getScoreP2());

		//set enabled or not depending on permissions
		if(TournamentLogic.getInstance(tid).getPermissionLevel() ==Player.PARTICIPANT)
		{
			s1.setEnabled(false);
			s2.setEnabled(false);
			
			//remove checkboxes from view
			ImageButton ib2 = (ImageButton) convertView.findViewById(R.id.checkscore_two);
			ib2.setVisibility(ImageButton.INVISIBLE);
			
		}
		else
		{
			s1.setEnabled(true);
			s2.setEnabled(true);
			
			//add checkbox to view
			ImageButton ib2 = (ImageButton) convertView.findViewById(R.id.checkscore_two);
			ib2.setVisibility(ImageButton.VISIBLE);
			
			//add listener to update the score
			ib2.setOnClickListener(new SetScoresListener(vehicle, s1, s2));
			
		}

		// Depending upon the child type, set the imageTextView01
		p1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		p2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		s1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		s2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		//        if (vehicle instanceof Car) {
		//            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_launcher, 0, 0, 0);
		//        } else if (vehicle instanceof Bus) {
		//            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_launcher, 0, 0, 0);
		//        } else if (vehicle instanceof Bike) {
		//            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_launcher, 0, 0, 0);
		//        }
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		ArrayList<ArrayList<Match>> children = ((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).getChildren();

		return children.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		ArrayList<String> groups = ((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).getGroups();

		return groups.get(groupPosition);
	}

	public int getGroupCount() {
		ArrayList<String> groups = ((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).getGroups();

		return groups.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// Return a group view. You can load your custom layout here.
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,ViewGroup parent) 
	{
		String group = (String) getGroup(groupPosition);
		if (convertView == null) 
		{
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.group_layout, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		tv.setText(group);

		//setup  the goto button
		ImageView gt = (ImageView) convertView.findViewById(R.id.goto_button);
		gt.setOnClickListener(new OnClickListener(){

			public void onClick(View v) 
			{
				// transition to the standings page
				Log.d("List Adapter","goto clicked");	
				Intent i = new Intent(((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).getActivity(), OverallStandingsActivity.class);
				i.putExtra("tournamentId", ((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).getTournamentId());
				
				((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).getActivity().startActivity(i);

			}

		});



		return convertView;
	}

	public boolean hasStableIds() {
		return true;
	}


	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}
	
	
	/**
	 * Listener for attaching to the check maks that will send the score update to the host.
	 * @author waltzm
	 * @versio 12/14/2012
	 */
	private class SetScoresListener implements OnClickListener
	{
		/**
		 * Holds the match to update
		 */
		private Match m;
		
		/**
		 * Holds the player 1 edit text
		 */
		private EditText p1;
		
		/**
		 * holds the player 2 edit text
		 */
		private EditText p2;

		/**
		 * Constructs a useful listener for the checkmarks
		 * @param m the match this listener is connected to
		 * @param p1 the edit text with player 1's score
		 * @param p2 the edit text with player two's score
		 */
		public SetScoresListener(Match m, EditText p1, EditText p2)
		{
			this.m=m;
			this.p1=p1;
			this.p2=p2;
		}

		public void onClick(View v) 
		{
			//when clicked send new scores to the host
			((SingleEliminationPartTournament)(TournamentLogic.getInstance(tid))).getOutgoingCommandHandler().handleSendScore(tid, m.getId(), m.getPlayerOne().getUUID().toString(), m.getPlayerTwo().getUUID().toString(), Double.parseDouble(p1.getText().toString()), Double.parseDouble(p2.getText().toString()), m.getRound());
		}
		
	}



}