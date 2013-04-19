package utool.plugin.singleelimination;

import utool.plugin.singleelimination.R;
import utool.plugin.singleelimination.participant.ExpandableListAdapter;
import utool.plugin.singleelimination.participant.SingleEliminationPartTournament;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

/**
 * Activity for the matchups screen for a Participant
 * Note: all the runnable stuff was for debugging purposes
 * @author waltzm
 * @version 12/3/2012
 */
public class ParticipantTournamentActivity extends Activity
{
	
	/**
	 * Holds a reference to the adapter
	 */
	private ExpandableListAdapter adapter;
	
	/**
	 * unique tournament id
	 */
	private long tid = 0;

	/**
	 * Holds a reference to this class
	 */
	private ParticipantTournamentActivity act;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matchup_main_layout);

		//pull tid out of the extras
		this.tid = getIntent().getExtras().getLong("tid");
		Toast toast = Toast.makeText(this, "Tournament id: "+tid, Toast.LENGTH_SHORT);
		toast.show();
		
		//assign tournament to talk to SampleActivity
		((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).setActivity(this);
		//((SingleEliminationPartTournament)TournamentLogic.getInstance(tid)).permissionLevel=Player.PARTICIPANT;//TODO this needs to be removed
		
		//TODO temporary button that may be removed later
		Button reload = (Button)findViewById(R.id.reload);
		reload.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				TournamentLogic.getInstance(tid).getOutgoingCommandHandler().handleSendError(tid, "bob", "Test", "Error");				
			}
			
		});
		
		

		//remake the adapter
		resetAdapter();
		
		
		//set tournament name
		TextView tn = (TextView)findViewById(R.id.tourny_name);
		tn.setText("Tournament: "+TournamentLogic.getInstance(tid).getTournamentName(false));

	}

	
	/**
	 * Forces the adapter to re-make itself (essentially a redraw method)
	 */
	public void resetAdapter() 
	{
		act=this;
		runOnUiThread(new Runnable() {
			public void run() {
				// Retrieve the ExpandableListView from the layout
				ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);

				listView.setOnChildClickListener(new OnChildClickListener()
				{

					public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4)
					{
						//Toast.makeText(getBaseContext(), "Child clicked", Toast.LENGTH_LONG).show();
						return false;
					}
				});

				listView.setOnGroupClickListener(new OnGroupClickListener()
				{

					public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2, long arg3)
					{
						//Toast.makeText(getBaseContext(), "Group clicked", Toast.LENGTH_LONG).show();
						return false;
					}
				});

				// Initialize the adapter with blank groups and children
				// We will be adding children on a thread, and then update the ListView
				adapter = new ExpandableListAdapter(act, tid);

				// Set this blank adapter to the list view
				listView.setAdapter(adapter);

			}
		});


	}

}
