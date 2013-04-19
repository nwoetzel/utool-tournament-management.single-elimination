package utool.plugin.singleelimination;

import java.util.ArrayList;

import utool.plugin.Player;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * List Adapter for the Modifying of Matchups
 * @author hoguet
 *
 */
public class ModifyMatchupsListAdapter extends ArrayAdapter<Matchup>{

	/**
	 * Holds the application context
	 */
	private Context context;

	/**
	 * Holds the text view resource id number
	 */
	private int textViewResourceId;

	/**
	 * holds the list of matchups
	 */
	private ArrayList<Matchup> matchups;

	/**
	 * holds the list of players
	 */
	private ArrayList<Player> players;

	/**
	 * Holds a reference to the listview
	 */
	private ListView listView;

	/**
	 * Constructor that creates the custom adapter
	 * @param context the application context
	 * @param textViewResourceId the resource number fo the text view
	 * @param matchups the list of matchups
	 * @param players the list of players
	 * @param listView the listview used
	 */
	public ModifyMatchupsListAdapter(Context context, int textViewResourceId, ArrayList<Matchup> matchups, ArrayList<Player> players, ListView listView) {
		super(context, textViewResourceId, matchups);
		this.context = context;
		this.textViewResourceId = textViewResourceId;
		this.matchups = matchups;
		this.players = players;
		this.listView = listView;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		View v = view;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.modify_matchups_child_item, null);
		}

		TextView tvOne = (TextView) v.findViewById(R.id.tvPlayerOne);
		Player one = matchups.get(position).getPlayerOne();
		String oneText = "null";
		if(one != null){
			oneText = one.getName();
		}
		tvOne.setText(oneText);     


		tvOne.setOnClickListener(new MatchupClickListener(matchups.get(position), v, null){
			public void onClick(View v) {

				int popupWidth = 600;
				int popupHeight = 400;

				// Inflate the scores_popup_layout.xml
				RelativeLayout viewGroup = (RelativeLayout) getView().findViewById(R.id.popup);
				LayoutInflater layoutInflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = layoutInflater.inflate(R.layout.select_player_popup_layout, viewGroup);

				// Creating the PopupWindow
				final PopupWindow popup = new PopupWindow(getContext());
				popup.setContentView(layout);
				popup.setWidth(popupWidth);
				popup.setHeight(popupHeight);
				popup.setFocusable(true);

				// Clear the default translucent background
				popup.setBackgroundDrawable(new BitmapDrawable());

				// Displaying the popup at the specified location, + offsets.
				popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);

				ListView lv = (ListView) layout.findViewById(R.id.playersListView);
				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);			

				ArrayList<Player> playersToAdapt = new ArrayList<Player>();
				for(Player p : players){
					playersToAdapt.add(p);
				}
				playersToAdapt.add(new Player(Player.BYE, "Bye"));

				ArrayAdapter<Player> lvAdapter = new ArrayAdapter<Player>(layout.getContext(), R.layout.players_list_item, playersToAdapt);
				lv.setAdapter(lvAdapter);

				lv.setOnItemClickListener(new MatchupOnItemClickListener(getMatchup(), layout, lv){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {

						ListView playerLv = (ListView) getLayout().findViewById(R.id.playersListView);    						
						Player newPlayer = (Player) playerLv.getItemAtPosition(arg2);
						if(newPlayer.getUUID().equals(Player.BYE)){
							newPlayer = null;
						}

						getMatchup().swapPlayerOne(newPlayer);


						ModifyMatchupsListAdapter lvAdapter = new ModifyMatchupsListAdapter(context, textViewResourceId, matchups, players, listView);
						listView.setAdapter(lvAdapter);

						popup.dismiss();
					}

				});

			}
		});

		TextView tvTwo = (TextView) v.findViewById(R.id.tvPlayerTwo);
		Player two = matchups.get(position).getPlayerTwo();
		String twoText = "null";
		if(two != null){
			twoText = two.getName();
		}
		tvTwo.setText(twoText);

		tvTwo.setOnClickListener(new MatchupClickListener(matchups.get(position), v, null){
			public void onClick(View v) {

				int popupWidth = 600;
				int popupHeight = 400;

				// Inflate the scores_popup_layout.xml
				RelativeLayout viewGroup = (RelativeLayout) getView().findViewById(R.id.popup);
				LayoutInflater layoutInflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = layoutInflater.inflate(R.layout.select_player_popup_layout, viewGroup);

				// Creating the PopupWindow
				final PopupWindow popup = new PopupWindow(getContext());
				popup.setContentView(layout);
				popup.setWidth(popupWidth);
				popup.setHeight(popupHeight);
				popup.setFocusable(true);

				// Clear the default translucent background
				popup.setBackgroundDrawable(new BitmapDrawable());

				// Displaying the popup at the specified location, + offsets.
				popup.showAtLocation(layout, Gravity.NO_GRAVITY, 50, 50);

				ListView lv = (ListView) layout.findViewById(R.id.playersListView);
				lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);			

				ArrayList<Player> playersToAdapt = new ArrayList<Player>();
				for(Player p : players){
					playersToAdapt.add(p);
				}
				playersToAdapt.add(new Player(Player.BYE, "Bye"));

				ArrayAdapter<Player> lvAdapter = new ArrayAdapter<Player>(layout.getContext(), R.layout.players_list_item, playersToAdapt);
				lv.setAdapter(lvAdapter);

				lv.setOnItemClickListener(new MatchupOnItemClickListener(getMatchup(), layout, lv){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {

						ListView playerLv = (ListView) getLayout().findViewById(R.id.playersListView);    						
						Player newPlayer = (Player) playerLv.getItemAtPosition(arg2);
						if(newPlayer.getUUID().equals(Player.BYE)){
							newPlayer = null;
						}

						getMatchup().swapPlayerTwo(newPlayer);

						ModifyMatchupsListAdapter lvAdapter = new ModifyMatchupsListAdapter(context, textViewResourceId, matchups, players, listView);
						listView.setAdapter(lvAdapter);

						popup.dismiss();

					}

				});

			}
		});

		return v;
	}

	/**
	 * On item click listener for a Matchup
	 * @author hoguet
	 *
	 */
	private class MatchupOnItemClickListener implements OnItemClickListener{

		/**
		 * The matchup this on click listner is attached to
		 */
		private Matchup matchup;
		
		/**
		 * The layout
		 */
		private View layout;
		
		/**
		 * the listview
		 */
		private ListView matchupListView;

		/**
		 * Constructor for the onClickListener
		 * @param m the matchup to connect to
		 * @param layout the layout
		 * @param lv the listview
		 */
		public MatchupOnItemClickListener(Matchup m, View layout, ListView lv){
			this.matchup = m;
			this.matchupListView = lv;
			this.layout = layout;
		}


		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub

		}

		/**
		 * Getter for the matchup
		 * @return the matchup
		 */
		public Matchup getMatchup(){
			return matchup;
		}
		
		/**
		 * Getter for the matchup list view
		 * @return the matchup listview
		 */
		public ListView getMatchupListView(){
			return matchupListView;
		}
		
		/**
		 * Getter for the layout
		 * @return the layout
		 */
		public View getLayout(){
			return layout;
		}

	}

}
