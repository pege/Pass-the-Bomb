package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
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

import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

public class JoinActivity extends AppCompatActivity {

    private TextView noGames;
    private TableLayout gamesTable;
    private TableRow headerRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //initializing global variables
        noGames = (TextView) findViewById(R.id.no_games_message);
        gamesTable = (TableLayout) findViewById(R.id.games_table);
        headerRow = (TableRow) gamesTable.getChildAt(0);

        //create the table for the first time
        onClickRefresh(findViewById(R.id.refresh));
    }

    //TODO implement this
    /* Gets information on what games are available from the server.
    ** Game[] contains necessary information about the game
    ** --> see Game.java in the gamelogic package
     */
    public Game[] getGamesInfo(){
        return null;
    }


    // Refreshes the table containing the games.
    public void onClickRefresh(View view) {
        Game[] games = getGamesInfo();

        int numberOfGames;
        if(games != null) {
            numberOfGames = games.length;
        } else {
            numberOfGames = 0;
        }
        //TODO get rid of next two lines; (this is only for testing purposes, because games=null for now)
        numberOfGames = 2;
        games = new Game[]{new Game("herp derp", "theBest", false, ""),
                new Game("some game", "idiot", true, "pw")};

        recreateTable(numberOfGames, games);
    }

    public void recreateTable(int numberOfGames, Game[] games){
        if(numberOfGames>0){
            // reset table
            gamesTable.removeAllViews();
            gamesTable.addView(headerRow, 0);

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
        gameName.setText(game.getName());
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
        numberOfPlayers.setText(game.getPlayers().size() + " / 5");
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

        // places cell border
        TextView cell = new TextView(this);
        cell.setBackground(getDrawable(R.drawable.cell_shape_input));
        cell.setPadding(padding, padding, padding, padding);
        cell.setWidth((int) Math.ceil(90 * metrics.density));

        // places button
        Button joinButton = new Button(this);
        joinButton.setPadding(0, (int) Math.ceil(16 * metrics.density),0, (int) Math.ceil(16 * metrics.density));
        joinButton.setText(getResources().getString(R.string.join));

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

                    // Get a reference for the popup window view input password field
                    final EditText inputPassword = (EditText) passwordPopUp.findViewById(R.id.input_password);

                    // Set a click listener for the popup window join button
                    joinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(inputPassword.getText().toString().isEmpty()){
                                Toast toast = Toast.makeText(view.getContext(), R.string.empty_password_field, Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                if (inputPassword.getText().toString().equals(thisGame.getPassword())) {
                                    // Dismiss the popup window
                                    mPopupWindow.dismiss();

                                    // Make the screen colour turn back to normal
                                    blackout.setVisibility(View.INVISIBLE);

                                    // create a new intent
                                    Intent myIntent = new Intent(view.getContext(), LobbyActivity.class);
                                    // pass LobbyActivity extra information
                                    Bundle extras = getIntent().getExtras();
                                    Player thisPlayer = new Player(extras.getString("player_name"));
                                    thisGame.addPlayer(thisPlayer);
                                    myIntent.putExtra("game", thisGame);
                                    myIntent.putExtra("isCreator", false);
                                    myIntent.putExtra("thisPlayer", thisPlayer);
                                    // Start LobbyActivity
                                    view.getContext().startActivity(myIntent);
                                    //TODO: pass information to server that the player is entering that game
                                } else {
                                    Toast toast = Toast.makeText(view.getContext(), R.string.wrong_password, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
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
                    Intent myIntent = new Intent(v.getContext(), LobbyActivity.class);

                    // give LobbyActivity extra information
                    Bundle extras = getIntent().getExtras();
                    Player thisPlayer = new Player(extras.getString("player_name"));
                    thisGame.addPlayer(thisPlayer);
                    myIntent.putExtra("thisPlayer", thisPlayer);
                    myIntent.putExtra("game", thisGame);
                    myIntent.putExtra("isCreator", false);

                    // start LobbyActivity
                    v.getContext().startActivity(myIntent);
                    //TODO: pass information to server that the player is entering that game
                }
            }
        });

        buttonCell.addView(cell, 0);
        buttonCell.addView(joinButton, 1);
        tableRow.addView(buttonCell, 3);
    }
}
