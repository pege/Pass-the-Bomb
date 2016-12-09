package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

public class LobbyActivity extends AppCompatActivity implements MessageListener {
// TODO: get information from server about the players and put it into the global variable 'game'
    // TODO (cont.) at a regular interval
    // TODO (cont.) and update the table with updateTable()
    private Game game;
    private Player thisPlayer;
    private Boolean isCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        String game_update = getIntent().getExtras().getString("message");
        try {
            JSONObject body = new JSONObject(game_update);

        } catch (JSONException e) {
            return;
        }

//        Game game = new Game(name, creatorName, locked, password);
//
//        // give GameActivity extra information
//        myIntent.putExtra("isCreator", true);
//        myIntent.putExtra("game", game);
//        myIntent.putExtra("thisPlayer", game.getPlayers().get(0));
//
//        this.startActivity(myIntent);


        // initialize global variables
        Bundle extras = getIntent().getExtras();
        game = (Game) extras.get("game");
        thisPlayer = (Player) extras.get("thisPlayer");
        isCreator = extras.getBoolean("isCreator");

        setLobbyTitle();
        setStartButton();
        updateTable();

    }

    private void setLobbyTitle(){
        String gameName = game.getName();
        setTitle(gameName);
    }

    private void setStartButton(){
        Button startButton = (Button) findViewById(R.id.start_game);
        if (isCreator){
            startButton.setVisibility(View.VISIBLE);
        } else
        {
            startButton.setVisibility(View.INVISIBLE);
        }
    }

    // updates Table based on information from the global variable 'game'
    public void updateTable(){
        TableLayout tableLayout = (TableLayout) findViewById(R.id.players_table);

        // places player names
        for (int i = 1; i<=game.getPlayers().size(); i++){
            TextView text = (TextView) ((TableRow)tableLayout.getChildAt(i)).getChildAt(0);
            text.setText(game.getPlayers().get(i-1).getName());
            if(Build.VERSION.SDK_INT >= 23) {
                text.setTextColor(getColor(R.color.black));
            }else{
                //noinspection deprecation
                text.setTextColor(getResources().getColor(R.color.black));
            }
        }

        // fills out empty spots
        for (int i = game.getPlayers().size() + 1; i<=5; i++){
            TextView text = (TextView) ((TableRow)tableLayout.getChildAt(i)).getChildAt(0);
            text.setText(getString(R.string.empty_player_field));
            if(Build.VERSION.SDK_INT >= 23) {
                text.setTextColor(getColor(R.color.grey));
            }else{
                //noinspection deprecation
                text.setTextColor(getResources().getColor(R.color.grey));
            }
        }
    }

    @Override
    public void onBackPressed(){
        if (isCreator) {
            //TODO: send the server information to randomly select a new "creator"
            // TODO (cont.) if there are still people in the lobby,
            // TODO (cont.) else delete games from list of games available on server (but this is handled by the server)
        } else {
            //TODO: send server information that this player has exited the game

            //not sure if this is necessary as it seems to be only local?
            //Depends how you client people want to handle things...
            game.removePlayer(thisPlayer);
        }

        finish();
    }

    /* Starts the game if there are enough players
     * Sends a start command to the server if successful in starting
     */
    public void onClickStart(View view) {
        if(game.getPlayers().size()<2){
            Toast toast = Toast.makeText(this, R.string.too_little_players, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            //TODO send start command to server
            startGame();
        }
    }


    //TODO call this when the creator starts the game
    /* Starts the game.
    ** All other intents should be destroyed,
    ** because they won't be called though the back button anymore
    ** and thus would otherwise be stuck on the stack
    **
    ** Also, all other players should be forced to call this
    *  when the creator starts the game
     */
    public void startGame(){
        Intent myIntent = new Intent(this, GameActivity.class);

        // give the next activity extra information
        myIntent.putExtra("game", game);
        myIntent.putExtra("thisPlayer", thisPlayer);

        this.startActivity(myIntent);

        // destroy intent with MainActivity
        getParent().getParent().finish();
        // destroy intent with CreateActivity/LobbyActivity
        getParent().finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        controller.bind(this);
    }

    @Override
    public void onMessage(int type, JSONObject body) {
        //TODO
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.unbind(this);
    }
}
