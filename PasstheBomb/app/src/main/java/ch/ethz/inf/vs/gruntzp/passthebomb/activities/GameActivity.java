package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

//TODO make the bomb and the layout for the players and the score
public class GameActivity extends AppCompatActivity {

    private int currentApiVersion;
    private Game game;
    private Player thisPlayer;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // initialize global variables
        Bundle extras = getIntent().getExtras();
        game = (Game) extras.get("game");
        thisPlayer = (Player) extras.get("thisPlayer");

        //GUI stuff
        hideNavigationBar();

        //for testing only
        /*
        game = new Game("herp derp", "theBest", false, "");
        game.addPlayer(new Player("Senpai"));
        game.getPlayers().get(1).setScore(9000);
        thisPlayer = game.getPlayers().get(0);
        endGame();
        */
    }

    //TODO call this when the server sends information that the game has ended
    /* finishes game; displays button to go to ScoreboardActivity
     * call this when the server sends information that the game has ended
    */
    public void endGame(){
        TextView gameOver = (TextView) findViewById(R.id.game_over);
        Button toScoreboard = (Button) findViewById(R.id.to_scoreboard);

        gameOver.setVisibility(View.VISIBLE);
        toScoreboard.setVisibility(View.VISIBLE);
    }


    // makes sure navigation bar doesn't appear again
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
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // Suppress navigation bar
    private void hideNavigationBar(){
        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
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

    /* TODO? if user somehow manages to bring back the navigation bar,
    ** should it not do anything, or
    ** should it bring them back to the main menu or something
    ** and kick him out of the game?
    */
    @Override
    public void onBackPressed(){
        super.onBackPressed(); //to change?
    }

    public void onClickContinue(View view) {
        Intent myIntent = new Intent(this, ScoreboardActivity.class);

        //give extra information to the next activity
        myIntent.putExtra("game", game);
        myIntent.putExtra("thisPlayer", thisPlayer);

        //start next activity
        this.startActivity(myIntent);

        finish();
    }
}
