package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

public class ScoreboardActivity extends AppCompatActivity implements MessageListener {

    private int currentApiVersion;
    private Game game;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        hideNavigationBar();

        displayScores();
    }

    public void displayScores(){
        Bundle extras = getIntent().getExtras();
        Game game = (Game)extras.get("game");

        //game=new Game("herp derp", "theBest", false, ""); // only for testing purposes

        //sort players by their scores
        LinkedList<Player> sortedPlayers = new LinkedList<>(game.getPlayers());
        Collections.sort(sortedPlayers);

        TableLayout tableLayout = (TableLayout) findViewById(R.id.scores_table);

        // display player and their score
        for (int i = 0; i < sortedPlayers.size(); i++){
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i+1);

            // display player's name
            TextView playerName = (TextView) tableRow.getChildAt(0);
            playerName.setText(sortedPlayers.get(i).getName());


            // display player's score
            TextView playerScore = (TextView) tableRow.getChildAt(1);
            playerScore.setText(sortedPlayers.get(i).getScore() + "");

        }

        // remove redundant rows
        for(int i = sortedPlayers.size(); i<5 ; i++){
            tableLayout.removeViewAt(sortedPlayers.size()+1);

        }
    }

    // This method makes sure navigation bar doesn't appear again
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // Suppress navigation bar
    private void hideNavigationBar(){
        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed(){
        /* Just in case the user somehow manages to get the navigation bar back up.
        ** This should stay empty,
        *+ because the user should not be able to go back to the game when it's done.
         */
    }

    public void onClickBackToMainMenu(View view) {
        Intent myIntent = new Intent(this, MainActivity.class);
        this.startActivity(myIntent);
        finish();
    }

    @Override
    public void onMessage(String message) {
        //TODO
    }
}
