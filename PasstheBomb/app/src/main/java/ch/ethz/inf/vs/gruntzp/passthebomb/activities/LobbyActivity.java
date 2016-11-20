package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;

public class LobbyActivity extends AppCompatActivity {

    private int numberOfPlayers; //TODO count players
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        startButton = (Button) findViewById(R.id.start_game);

        setLobbyTitle();
        setStartButton();

        //TODO add the list of players
    }

    private void setLobbyTitle(){
        Bundle extras = getIntent().getExtras();
        Game game = (Game) extras.get("game");
        String gameName = game.getName();
        setTitle(gameName);
    }

    private void setStartButton(){
        Bundle extras = getIntent().getExtras();
        Boolean isCreator = extras.getBoolean("isCreator");
        if (isCreator){
            startButton.setVisibility(View.VISIBLE);
        } else
        {
            startButton.setVisibility(View.INVISIBLE);
        }
    }

    /* Starts the game.
    ** All other intents should be destroyed,
    ** because they won't be called though the back button anymore
    ** and thus would be stuck on the stack
     */
    public void onClickStart(View view) {
        if(numberOfPlayers<2){
            Toast toast = Toast.makeText(this, R.string.too_little_players, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            //TODO send start command to server
            //TODO? do we need to send anything to the next activity?
            Intent myIntent = new Intent(this, GameActivity.class);
            this.startActivity(myIntent);

            // destroy intent with MainActivity
            getParent().getParent().finish();
            // destroy intent with CreateActivity/LobbyActivity
            getParent().finish();
        }
    }
}
