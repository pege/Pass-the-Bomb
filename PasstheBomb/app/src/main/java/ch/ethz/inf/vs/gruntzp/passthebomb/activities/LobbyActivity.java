package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageFactory;
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
    private SharedPreferences preferences;


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


        // initialize global variables from intent and shared preferences
        preferences = getSharedPreferences("Pref", Context.MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();


        game = Game.createFromJSON(extras.getString("message"));
        thisPlayer = game.getPlayerByID(preferences.getString("userID", ""));
        isCreator = game.getCreatorName().equals(thisPlayer.getName());


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
        //Server checks creator status/player number on it's own, just say I left.
        controller.sendMessage(MessageFactory.leaveGame());

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
            controller.sendMessage(MessageFactory.startGame());
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
        // destroy intent with CreateActivity/JoinActivity
        getParent().finish();
        //destroy myself
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        controller.bind(this);
    }

    @Override
    public void onMessage(int type, JSONObject body) {
        switch(type) {
            case 0:
                Toast toast = Toast.makeText(this, "Message receipt parsing error", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case MessageFactory.SC_GAME_UPDATE:
                game = Game.createFromJSON(body);
                if(game.hasStarted()) {//hasStarted implies that the game has a bomb owner and a bomb
                    try {
                        game.setBomb(body.getInt("bomb"));
                        game.setBombOwner(game.getPlayerByID(body.getString("bombOwner")));
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    startGame();
                }
                updateTable();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.unbind(this);
    }
}
