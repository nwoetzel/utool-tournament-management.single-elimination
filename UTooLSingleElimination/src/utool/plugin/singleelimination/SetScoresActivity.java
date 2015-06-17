package utool.plugin.singleelimination;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;

/**
 * 
 * Activity for the set scores screen.
 * 
 * @author hoguet
 * 1-7-12 
 *
 */
public class SetScoresActivity extends AbstractPluginCommonActivity{

	/**
	 * The tournament associated with this activity; the one that contains the matchup whose scores are being set
	 */
	private SingleEliminationTournament tournament;

	/**
	 * The matchup associated with this activity; the one whose scores are being set
	 */
	private Matchup matchup;

	/**
	 * The label that displays time remaining
	 */
	private TextView timerLabel;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_set_scores);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

		TextView titleLabel = (TextView) findViewById(R.id.title);
		titleLabel.setText("Single Elimination");

		//Initialize the associated tournament and matchup of this scores dialog
		tournament = (SingleEliminationTournament)TournamentLogic.getInstance(getTournamentId());
		long mid = getIntent().getExtras().getLong("matchup_id");
		matchup = Matchup.getMatchupById(tournament.getMatchups(), mid);

		//Set round/match label
		TextView roundMatchLabel = (TextView) findViewById(R.id.ss_roundMatchLabel);
		roundMatchLabel.setText("Round "+tournament.getRound()+" : Match "+matchup.getId());

		//Set timer label
		timerLabel = (TextView) findViewById(R.id.timerLabel);
		timerLabel.setText(""); //TODO get infinity symbol
		tournament.addTimerDisplay(timerLabel);

		//Set player one label and portrait
		TextView playerOneLabel = (TextView) findViewById(R.id.playerOneLabel);

		String playerOneString = "";
		if(matchup.getPlayerOne() != null){
			playerOneString = matchup.getPlayerOne().getName();

			if(matchup.getPlayerOne().getPortrait() != null){
				ImageView playerOneImage = (ImageView) findViewById(R.id.playerOneImage);
				playerOneImage.setImageBitmap(matchup.getPlayerOne().getPortrait());
			}
		}

		playerOneLabel.setText(playerOneString);

		//Set player two label and portrait
		TextView playerTwoLabel = (TextView) findViewById(R.id.playerTwoLabel);

		String playerTwoString = "";
		if(matchup.getPlayerTwo() != null){
			playerTwoString = matchup.getPlayerTwo().getName();

			if(matchup.getPlayerTwo().getPortrait() != null){
				ImageView playerTwoImage = (ImageView) findViewById(R.id.playerTwoImage);
				playerTwoImage.setImageBitmap(matchup.getPlayerTwo().getPortrait());
			}
		}

		playerTwoLabel.setText(playerTwoString);

		//Initialize score fields to current score or zeros if null		
		double scoreOne = 0;
		double scoreTwo = 0;
		if(matchup.getScores() != null){
			scoreOne = matchup.getScores()[0];
			scoreTwo = matchup.getScores()[1];
		}

		EditText playerOneEdit = (EditText) findViewById(R.id.playerOneScoreField);
		playerOneEdit.setText(""+scoreOne);

		EditText playerTwoEdit = (EditText) findViewById(R.id.playerTwoScoreField);
		playerTwoEdit.setText(""+scoreTwo);


		//Define Player 1 +/- functions
		Button playerOnePlus = (Button) findViewById(R.id.playerOnePlusBtn);
		playerOnePlus.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				EditText playerOneEdit = (EditText) findViewById(R.id.playerOneScoreField);

				String newText;
				try{
					newText = ""+(Double.parseDouble(playerOneEdit.getText().toString()) + 1);
				}catch(NumberFormatException e){ //occurs if player one score field was cleared, does not contain double
					newText = "0";
				}

				playerOneEdit.setText(newText);			
			}			

		});

		Button playerOneMinus = (Button) findViewById(R.id.playerOneMinusBtn);
		playerOneMinus.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				EditText playerOneEdit = (EditText) findViewById(R.id.playerOneScoreField);

				String newText;
				try{
					newText = ""+(Double.parseDouble(playerOneEdit.getText().toString()) - 1);
				}catch(NumberFormatException e){ //occurs if player one score field was cleared, does not contain double
					newText = "0";
				}

				playerOneEdit.setText(newText);	
			}			

		});

		//Define Player 2 +/- functions
		Button playerTwoPlus = (Button) findViewById(R.id.playerTwoPlusBtn);
		playerTwoPlus.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				EditText playerTwoEdit = (EditText) findViewById(R.id.playerTwoScoreField);

				String newText;
				try{
					newText = ""+(Double.parseDouble(playerTwoEdit.getText().toString()) + 1);
				}catch(NumberFormatException e){ //occurs if player two score field was cleared, does not contain double
					newText = "0";
				}

				playerTwoEdit.setText(newText);	
			}			

		});

		Button playerTwoMinus = (Button) findViewById(R.id.playerTwoMinusBtn);
		playerTwoMinus.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				EditText playerTwoEdit = (EditText) findViewById(R.id.playerTwoScoreField);

				String newText;
				try{
					newText = ""+(Double.parseDouble(playerTwoEdit.getText().toString()) - 1);
				}catch(NumberFormatException e){ //occurs if player two score field was cleared, does not contain double
					newText = "0";
				}

				playerTwoEdit.setText(newText);	
			}			

		});

		//Define save button
		Button saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v){

				try{
					//Don't let scores be set in a finished tournament
					if(!tournament.isFinished()){

						EditText playerOneEdit = (EditText) findViewById(R.id.playerOneScoreField);
						double scoreOne = Double.parseDouble(playerOneEdit.getText().toString());	

						EditText playerTwoEdit = (EditText) findViewById(R.id.playerTwoScoreField);
						double scoreTwo = Double.parseDouble(playerTwoEdit.getText().toString());	

						if(tournament.getPermissionLevel() == Player.HOST){
							matchup.setScores(scoreOne, scoreTwo);
						}else if(tournament.getPermissionLevel() == Player.MODERATOR){
							//A moderator who set scores should send a message to the host; the host will update the tournament
							tournament.getOutgoingCommandHandler().handleSendScore(tournament.getTournamentId(), matchup.getId(), matchup.getPlayerOne().getUUID().toString(), matchup.getPlayerTwo().getUUID().toString(), scoreOne, scoreTwo, matchup.getRoundParticipant());
						}
					}

					tournament.removeTimerDisplay(timerLabel);
					finish();
				}catch(NumberFormatException e){
					//If scores are not valid (not numbers), do not allow user to save the scores.  Remain in activity until fixed or cancelled
				}
			}

		});

		//Define cancel button
		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v){
				tournament.removeTimerDisplay(timerLabel);
				finish();
			}
		});


	}

	/**
	 * Displays the help screen
	 */
	public void showHelp(){
		final Dialog dialog = new Dialog(SetScoresActivity.this);
		dialog.setContentView(R.layout.set_scores_help);
		dialog.setTitle("Set Scores Help");
		dialog.setCancelable(true);
		Button closeButton = (Button) dialog.findViewById(R.id.home_help_close_button);
		closeButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				dialog.dismiss();     
			}
		});
		dialog.show();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.overall_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			showHelp();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
