package utool.plugin.singleelimination;

import java.util.ArrayList;
import utool.plugin.singleelimination.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class populates the ExpandableList of rounds with the correct child elements 
 * and defines behavior of buttons on the child elements.
 *  
 * 
 * @author hoguet
 * 10-14-12
 *
 */
public class ExpandableRoundsListAdapter extends BaseExpandableListAdapter {

	/**
	 * The activity this adapter is instantiated from
	 */
	private MatchupsActivity activity;
	
	/**
	 * The context to expand the list on
	 */
	private Context context;

	/**
	 * The groups of the list, which correspond to rounds in the tournament
	 */
	private ArrayList<ExpandableRoundsListGroup> groups;

	/**
	 * 
	 * @param context the context to expand the list on
	 * @param groups the groups of the expandable list
	 * @param activity the matchups activity
	 */
	public ExpandableRoundsListAdapter(Context context, ArrayList<ExpandableRoundsListGroup> groups, MatchupsActivity activity) {
		this.context = context;
		this.groups = groups;
		this.activity = activity;
	}

	/**
	 * Add item to group
	 * @param item child element to add to group
	 * @param group group to add child to
	 */
	public void addItem(ExpandableRoundsListChild item, ExpandableRoundsListGroup group) {

		if (!groups.contains(group)) {
			groups.add(group);
		}
		int index = groups.indexOf(group);
		ArrayList<ExpandableRoundsListChild> ch = groups.get(index).getItems();
		ch.add(item);
		groups.get(index).setItems(ch);
	}

	/**
	 * @return Object child
	 */
	public Object getChild(int groupPosition, int childPosition) {
		ArrayList<ExpandableRoundsListChild> chList = groups.get(groupPosition).getItems();
		return chList.get(childPosition);
	}

	/**
	 * @return long child id
	 */
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	/**
	 * 
	 * @return child view
	 */
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {

		ExpandableRoundsListChild child = (ExpandableRoundsListChild) getChild(groupPosition, childPosition);
		if (view == null) 
		{
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = infalInflater.inflate(R.layout.expandlist_child_item, null);

		}

		TextView tv = (TextView) view.findViewById(R.id.tvChild);
		tv.setText(child.getName().toString());
		tv.setTag(child.getTag());


		//Add "Set Scores" button to child element that opens set score dialog
		Button scoresBtn = (Button) view.findViewById(R.id.scoresBtn);
		scoresBtn.setOnClickListener(new MatchupClickListener(child.getMatchup(), view, null){

			@Override
			public void onClick(View v){
				
				activity.goToSetScores(getMatchup());
				
			}
			
		});
		
		return view;
	}
		
		//If tournament is not started or either player is null, disable the set scores button
//		if(!getMatchup().getTournament().isStarted() || getMatchup().getPlayerOne() == null || getMatchup().getPlayerTwo() == null){
//			setScores.setEnabled(false);
//		}
				
//				int popupWidth = 600;
//				int popupHeight = 400;
//
//				// Inflate the scores_popup_layout.xml
//				RelativeLayout viewGroup = (RelativeLayout) getView().findViewById(R.id.popup);
//				LayoutInflater layoutInflater = (LayoutInflater) context
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				View layout = layoutInflater.inflate(R.layout.scores_popup_layout, viewGroup);
//
//				// Creating the PopupWindow
//				final PopupWindow popup = new PopupWindow(context);
//				popup.setContentView(layout);
//				popup.setWidth(popupWidth);
//				popup.setHeight(popupHeight);
//				popup.setFocusable(true);
//
//				// Clear the default translucent background
//				popup.setBackgroundDrawable(new BitmapDrawable());
//
//				// Displaying the popup at the specified location, + offsets.
//				popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);
//
//				TextView t = (TextView) layout.findViewById(R.id.playerOneText);
//				if(getMatchup().getPlayerOne() != null ){
//					t.setText(getMatchup().getPlayerOne().getName());
//				}
//
//				t = (TextView) layout.findViewById(R.id.playerTwoText);
//				if(getMatchup().getPlayerTwo() != null ){
//					t.setText(getMatchup().getPlayerTwo().getName());
//				}
//
//				// Getting a reference to Close button, and close the popup when clicked.
//				Button close = (Button) layout.findViewById(R.id.close);
//				close.setOnClickListener(new OnClickListener() {
//
//					
//					public void onClick(View v) {
//						popup.dismiss();
//					}
//				});
//
//				//Get reference to Set Scores button on the scores_popup_layout and define its behavior
//				Button setScores = (Button) layout.findViewById(R.id.setScoresBtn);
//				setScores.setOnClickListener(new MatchupClickListener(getMatchup(), getView(), layout){
//
//					@Override
//					public void onClick(View v){
//						
//						try{
//							
//							//Grab player one's score from player one EditText
//							EditText poet = (EditText)getLayout().findViewById(R.id.playerOneScore);
//							double playerOneScore = Double.parseDouble(poet.getText().toString());
//
//							//Grab player two's score from player two EditText
//							EditText ptet = (EditText)getLayout().findViewById(R.id.playerTwoScore);
//							double playerTwoScore = Double.parseDouble(ptet.getText().toString());
//
//							//Set scores of the match
//							getMatchup().setScores(playerOneScore, playerTwoScore);
//
//							//Display new scores
//							TextView scoresText = (TextView)getView().findViewById(R.id.tvChild);
//							scoresText.setText(getMatchup().printMatchup2());
//							
//							//Refresh expandableList
//							activity.refreshRoundsList();
//							
//							//needs correct  round
////							
////							//If either player is null, instead they are set to be players with the Bye UUID
////							Player p1 = getMatchup().getPlayerOne();
////							Player p2 = getMatchup().getPlayerTwo();
////							if(p1==null)
////							{
////								p1 = new Player(Player.BYE,"Bye");
////							}
////							if(p2==null)
////							{
////								p2 = new Player(Player.BYE,"Bye");
////							}
////							//SG needs to be told when round progresses
////							StandingsGeneratorSE.getInstance(null).recordScore(p1.getUUID(), p2.getUUID(), getMatchup().getTournament().getRound(), playerOneScore, playerTwoScore, getMatchup().getId());
////							
//							
//							//Close popup
//							popup.dismiss();
//							
//							//if tournament is finished as a result of this score being set, go to standings screen
//							if(getMatchup().getTournament().isFinished()){
//								
//								activity.goToStandings();
//								
//								
//							}
//							
//
//						}catch(NumberFormatException e){ //thrown if not a double in one of the score EditTexts. 
//
//						}
//					}
//				});
//				
//				//If tournament is not started or either player is null, disable the set scores button
//				if(!getMatchup().getTournament().isStarted() || getMatchup().getPlayerOne() == null || getMatchup().getPlayerTwo() == null){
//					setScores.setEnabled(false);
//				}
//
//			}
//
//		});

//		return view;
//	}

	/**
	 * @return int children count
	 */
	public int getChildrenCount(int groupPosition) {
		ArrayList<ExpandableRoundsListChild> chList = groups.get(groupPosition).getItems();

		return chList.size();

	}

	/**
	 * @return Object group
	 */
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	/**
	 * @return int group count
	 */
	public int getGroupCount() {
		return groups.size();
	}

	/**
	 * @return long group id
	 */
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/**
	 * @return group view
	 */
	public View getGroupView(int groupPosition, boolean isLastChild, View view,	ViewGroup parent) {
		ExpandableRoundsListGroup group = (ExpandableRoundsListGroup) getGroup(groupPosition);
		if (view == null) {
			LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.expandlist_group_item, null);
		}
		TextView tv = (TextView) view.findViewById(R.id.tvGroup);
		tv.setText(group.getName());

		return view;
	}

	/**
	 * @return true if has stable Ids TODO this is always true currently?
	 */
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * @return true if child is selectable TODO this is always true currently?
	 */
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}


}


