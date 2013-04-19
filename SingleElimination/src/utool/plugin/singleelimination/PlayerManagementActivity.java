package utool.plugin.singleelimination;

import utool.plugin.activity.AbstractPluginCommonActivity;
import android.os.Bundle;
import android.view.Menu;

/**
 * Skeleton activity for the Player Management screen
 * @author Cory
 *
 */
public class PlayerManagementActivity extends AbstractPluginCommonActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_management);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_player_management, menu);
        return true;
    }
}
