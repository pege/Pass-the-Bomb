package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

public class JoinActivity extends AppCompatActivity implements MessageListener {

    private Typeface font;
    private TextView noGames;
    private TableLayout gamesTable;
    private TableRow headerRow;
    private String password;
    private Game[] games;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //initializing global variables
        noGames = (TextView) findViewById(R.id.no_games_message);
        gamesTable = (TableLayout) findViewById(R.id.games_table);
        headerRow = (TableRow) gamesTable.getChildAt(0);

        password = "";

        //button stuff
        font = Typeface.createFromAsset(getAssets(), "fonts/sensei_medium.otf");
        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setTypeface(font);
        if(Build.VERSION.SDK_INT >= 23) {
            refresh.getBackground().setColorFilter(getColor(R.color.orange), PorterDuff.Mode.OVERLAY);

        } else {
            //noinspection deprecation
            refresh.getBackground().setColorFilter(getResources().getColor(R.color.orange), PorterDuff.Mode.OVERLAY);
        }

        //create the table for the first time
        onClickRefresh(findViewById(R.id.refresh));
    }

    //TODO implement this
    /* Gets information on what games are available from the server.
    ** Game[] contains necessary information about the game
    ** --> see Game.java in the gamelogic package
     */
    public void getGamesInfo(Game[] newGames){
        this.games = newGames;
        int numberOfGames;
        if(games != null) {
            numberOfGames = games.length;
        } else {
            numberOfGames = 0;
        }

        // the following is for testing purposes only
        /*
        numberOfGames = 2;
        games = new Game[]{new Game("herp derp", "theBest", false, ""),
                new Game("some game", "idiot", true, "pw")};
        */

        recreateTable(numberOfGames, games);
    }


    // Refreshes the table containing the games.
    public void onClickRefresh(View view) {
        controller.sendMessage(MessageFactory.getGames());
    }

    public void recreateTable(int numberOfGames, Game[] games){
        if(numberOfGames>0){
            // reset table
            gamesTable.removeAllViews();
            gamesTable.addView(headerRow, 0);

            noGames.setVisibility(View.INVISIBLE);

            // add games into table
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int padding = (int) Math.ceil(16 * metrics.density);
            TableRow tableRow;
            for (int i=0; i<numberOfGames; i++){
                //TODO put in game data
                tableRow = new TableRow(this);

                // add row elements to the new row
                addGameName(tableRow, games[i], padding);
                addNumberOfPlayers(tableRow, games[i], padding, metrics);
                addLockIcon(tableRow, games[i], metrics);
                addJoinButton(tableRow, games[i], padding, metrics); //TODO implement this finish (it's below)

                // add row to the table
                gamesTable.addView(tableRow, i+1);

            }

            //make table visible
            gamesTable.setVisibility(View.VISIBLE);

        }else{
            //notifies the user that there are no games available
            noGames.setVisibility(View.VISIBLE);

            // makes table invisible
            gamesTable.setVisibility(View.INVISIBLE);

            //TODO? add text to suggest making a game and button to go to CreateActivity?
            //TODO (cont.) in the case that we do the above, don't forget to finish() this intent
        }
    }

    private void addGameName(TableRow tableRow, Game game, int padding){
        // put in the game's name
        TextView gameName = new TextView(this);
        String name = game.getName();
        if(name.length()>11){
            name = name.substring(0,9)+"...";
        }
        gameName.setText(name);
        gameName.setPadding(padding, padding, padding, padding);
        gameName.setBackground(getDrawable(R.drawable.cell_shape_input));
        if(Build.VERSION.SDK_INT >= 23) {
            gameName.setTextColor(getColor(R.color.black));
        }else{
            //noinspection deprecation
            gameName.setTextColor(getResources().getColor(R.color.black));
        }
        tableRow.addView(gameName, 0);
    }

    private void addNumberOfPlayers(TableRow tableRow, Game game, int padding, DisplayMetrics metrics){
        // add number of players
        TextView numberOfPlayers = new TextView(this);
        int test = game.getNoPlayers();
        String numberString = test + " / 5";
        numberOfPlayers.setText(numberString);
        numberOfPlayers.setPadding(padding, padding, padding, padding);
        numberOfPlayers.setBackground(getDrawable(R.drawable.cell_shape_input));
        numberOfPlayers.setWidth((int) Math.ceil(80 * metrics.density));
        if(Build.VERSION.SDK_INT >= 23) {
            numberOfPlayers.setTextColor(getColor(R.color.black));
        }else{
            //noinspection deprecation
            numberOfPlayers.setTextColor(getResources().getColor(R.color.black));
        }
        tableRow.addView(numberOfPlayers, 1);
    }

    private void addLockIcon(TableRow tableRow, Game game, DisplayMetrics metrics){
        ImageView lock = new ImageView(this);
        if(game.getLocked()) {
            lock.setImageResource(R.drawable.ic_lock_black_48dp);
        } else {
            lock.setImageResource(R.drawable.ic_lock_open_black_48dp);
        }
        lock.setBackground(getDrawable(R.drawable.cell_shape_input));
        lock.setMaxWidth((int) Math.ceil(35 * metrics.density));
        lock.setPadding(0, (int) Math.ceil(2 * metrics.density),0, (int) Math.ceil(1 * metrics.density));
        tableRow.addView(lock, 2);
    }

    //TODO implement this finish; add other parameters if necessary (but don't forget to add them in the call of recreateTable())
    private void addJoinButton(TableRow tableRow, Game game, int padding, DisplayMetrics metrics){
        RelativeLayout buttonCell = new RelativeLayout(this);
        final SharedPreferences preferences = getSharedPreferences("Pref", Context.MODE_PRIVATE);


        // places cell border
        TextView cell = new TextView(this);
        cell.setBackground(getDrawable(R.drawable.cell_shape_input));
        cell.setPadding(padding, padding, padding, padding);
        cell.setWidth((int) Math.ceil(90 * metrics.density));

        // places button
        Button joinButton = new Button(this);
        joinButton.setPadding(0, (int) Math.ceil(16 * metrics.density),0, (int) Math.ceil(16 * metrics.density));
        joinButton.setText(getResources().getString(R.string.join));
        joinButton.setTypeface(font);
        if(Build.VERSION.SDK_INT >= 23) {
            joinButton.getBackground().setColorFilter(getColor(R.color.orange), PorterDuff.Mode.OVERLAY);

        } else {
            //noinspection deprecation
            joinButton.getBackground().setColorFilter(getResources().getColor(R.color.orange), PorterDuff.Mode.OVERLAY);
        }

        // defines what happens when the button is clicked
        final Game thisGame = game;
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: start lobby ONLY if the game is still available,
                // TODO (cont.) else send toast (surround current code with an if-statement?)
                /* QUESTION: When do we even check if the game is still available?
                **           Before checking if the game is locked and, if it is, also
                **           after the password was put in correctly?
                */
                //Answer: we send a join request to the server and only start LobbyActivity if we get a positive response :)

                Bundle extras = getIntent().getExtras();
                final String user_name = extras.getString("player_name");


                if(thisGame.getLocked()){
                    // Initialize a new instance of LayoutInflater service
                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                    // Inflate the custom layout/view
                    View passwordPopUp = inflater.inflate(R.layout.password_request_popup,null);

                    // make pop up that asks for the password
                    final PopupWindow mPopupWindow = new PopupWindow(passwordPopUp,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    mPopupWindow.setElevation(10.0f);

                    // make the background darker (the rest of the screen)
                    final RelativeLayout blackout = (RelativeLayout) findViewById(R.id.blackout);
                    blackout.setVisibility(View.VISIBLE);

                    // Get a reference for the popup window view join button
                    Button joinButton = (Button) passwordPopUp.findViewById(R.id.join_game_button);
                    joinButton.setTypeface(font);
                    if(Build.VERSION.SDK_INT >= 23) {
                        joinButton.getBackground().setColorFilter(getColor(R.color.orange), PorterDuff.Mode.OVERLAY);

                    } else {
                        //noinspection deprecation
                        joinButton.getBackground().setColorFilter(getResources().getColor(R.color.orange), PorterDuff.Mode.OVERLAY);
                    }

                    // Get a reference for the popup window view input password field
                    final EditText inputPassword = (EditText) passwordPopUp.findViewById(R.id.input_password);

                    //font
                    inputPassword.setTypeface(font);
                    TextView askForPassword = (TextView) passwordPopUp.findViewById(R.id.ask_for_password);
                    askForPassword.setTypeface(font);



                    // Set a click listener for the popup window join button
                    joinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(inputPassword.getText().toString().isEmpty()){
                                Toast toast = Toast.makeText(view.getContext(), R.string.empty_password_field, Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                    // Dismiss the popup window
                                    mPopupWindow.dismiss();

                                    //Set password to be sent to server
                                    password = inputPassword.getText().toString();

                                    // Make the screen colour turn back to normal
                                    blackout.setVisibility(View.INVISIBLE);


                                    // create a new intent
                                    controller.sendMessage(MessageFactory.joinGame(thisGame.getName(),password, user_name));

                                    //TODO: pass information to server that the player is entering that game

                            }
                        }
                    });

                    // Get a reference for the popup window view close button
                    ImageButton closeButton = (ImageButton) passwordPopUp.findViewById(R.id.ib_close);

                    // Set a click listener for the popup window close button
                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Dismiss the popup window
                            mPopupWindow.dismiss();

                            // Make the screen colour turn back to normal
                            blackout.setVisibility(View.INVISIBLE);
                        }
                    });

                    //set focusable so that the keyboard works
                    mPopupWindow.setFocusable(true);
                    mPopupWindow.update();

                    // Show the popup window at the center location of root relative layout
                    RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.activity_join);
                    mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,-80);

                } else {

                    controller.sendMessage(MessageFactory.joinGame(thisGame.getName(),password, user_name));

                    //TODO: pass information to server that the player is entering that game
                }
            }
        });

        buttonCell.addView(cell, 0);
        buttonCell.addView(joinButton, 1);
        tableRow.addView(buttonCell, 3);
    }

    @Override
    protected void onStart() {
        super.onStart();
        controller.bind(this);
    }

    @Override
    public void onMessage(int type, JSONObject body) {
        //TODO
        Toast toast;
        switch(type) {
            case 0:
                toast = Toast.makeText(this, "Message receipt parsing error", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case MessageFactory.SC_GAME_LIST: //Read all games from array and call function to update GUI
                try {
                    JSONArray gameArr = body.getJSONArray("games");
                    Game[] games = new Game[gameArr.length()];
                    for (int i = 0; i < gameArr.length(); i++) {
                        games[i] = Game.createFromJSON0(gameArr.getJSONObject(i));
                    }
                    getGamesInfo(games);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MessageFactory.SC_PLAYER_JOINED: //I joined
                Intent myIntent = new Intent(this, LobbyActivity.class);
                myIntent.putExtra("message", body.toString());
                myIntent.putExtra("renamed", false);
                this.startActivity(myIntent);
                break;
            case MessageFactory.ALREADY_STARTED_ERROR:
                toast = Toast.makeText(this, "That game already started", Toast.LENGTH_SHORT);
                onClickRefresh(findViewById(R.id.refresh));
                try {
                    String gameName = body.getString("game_id");
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                toast.show();
                break;
            case MessageFactory.WRONG_PASSWORD_ERROR:
                toast = Toast.makeText(this, "The password you entered isn't correct", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case MessageFactory.GAME_NOT_FOUND_ERROR:
                toast = Toast.makeText(this, "The game was not found", Toast.LENGTH_SHORT);
                onClickRefresh(findViewById(R.id.refresh));
                toast.show();
                break;
            case MessageFactory.CONNECTION_FAILED:
                Toast.makeText(this.getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
                Intent retMain = new Intent(this, MainActivity.class);
                this.startActivity(retMain);
                finish();
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onClickRefresh(findViewById(R.id.refresh));
    }
}
