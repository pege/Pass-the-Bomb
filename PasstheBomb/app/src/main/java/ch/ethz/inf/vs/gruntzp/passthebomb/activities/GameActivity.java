package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

//TODO make the bomb and the layout for the players and the score
public class GameActivity extends AppCompatActivity {

    private int currentApiVersion;
    private Game game;
    private Player thisPlayer;
    private RelativeLayout gameView;
    private ImageView bomb;


    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // initialize global variables
        bomb = (ImageView) findViewById(R.id.bomb);
        Bundle extras = getIntent().getExtras();
        //game = (Game) extras.get("game");
        //thisPlayer = (Player) extras.get("thisPlayer");
        //TODO get information on who has the bomb and set that in the variable 'game'


        //for testing only
        /**/
        game = new Game("herp derp", "theBest", false, "");
        game.addPlayer(new Player("Senpai"));
        game.getPlayers().get(1).setScore(9000);
        thisPlayer = game.getPlayers().get(0);
        thisPlayer.setHasBomb(true);
        //endGame();
        /**/


        //GUI stuff
        hideNavigationBar();
        gameView = (RelativeLayout) findViewById(R.id.game);
        setUpBomb();
        setUpPlayers();


    }

    private void setUpPlayers(){
        int j = 0; //index for player field
        for(int i=0; i<game.getPlayers().size(); i++){
            if (thisPlayer != game.getPlayers().get(i)) {
                Button player_field = (Button) gameView.getChildAt(j);
                player_field.setVisibility(View.VISIBLE);
                player_field.setText(game.getPlayers().get(i).getName());
                j++;
            }
        }
    }

    private void setUpBomb(){
        enableOnTouchAndDragging();
        setBombVisibility();

    }

    private void setBombVisibility(){
        if(!thisPlayer.isHasBomb()){
            bomb.setVisibility(View.INVISIBLE);
        } else {
            bomb.setVisibility(View.VISIBLE);
        }
    }

    private void enableOnTouchAndDragging(){
        bomb.setOnTouchListener(new View.OnTouchListener() {
            private Boolean[] touch = {false, false, false, false};
            int prevX,prevY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)v.getLayoutParams();

                for(int i=0; i<game.getPlayers().size()-1; i++) {
                    Button view = (Button) gameView.getChildAt(i);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {

                            //start dragging
                            prevX=(int)event.getRawX();
                            prevY=(int)event.getRawY();
                            par.bottomMargin=-2*v.getHeight();
                            par.rightMargin=-2*v.getWidth();
                            v.setLayoutParams(par);
                            scaleIn(bomb, 4);

                            //check intersection
                            if(checkInterSection(view, i,  event.getRawX(), event.getRawY())) {
                                scaleIn(view, i);
                                touch[i] = true;
                                Log.i("down", "yes!");
                            }
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {

                            //drag
                            par.topMargin+=(int)event.getRawY()-prevY;
                            prevY=(int)event.getRawY();
                            par.leftMargin+=(int)event.getRawX()-prevX;
                            prevX=(int)event.getRawX();
                            v.setLayoutParams(par);

                            //check touch
                            if(checkInterSection(view, i, event.getRawX(), event.getRawY()) && !touch[i]) {
                                scaleIn(view, i);
                                touch[i] = true;

                                Log.i("move", "yes!");
                            } else if(!checkInterSection(view, i, event.getRawX(), event.getRawY()) && touch[i]) {
                                // run scale animation and make it smaller
                                scaleOut(view, i);
                                touch[i] = false;

                                Log.i("move", "no!");
                            }
                            break;
                        }
                        case MotionEvent.ACTION_UP: {

                            //stop dragging
                            par.topMargin+=(int)event.getRawY()-prevY;
                            par.leftMargin+=(int)event.getRawX()-prevX;
                            v.setLayoutParams(par);
                            scaleOut(bomb, 4);

                            //check if touching
                            if (touch[i]) {
                                // run scale animation and make it smaller
                                scaleOut(view, i);
                                touch[i] = false;
                                //TODO and if it was touching, then send server information to pass the bomb on
                                //TODO make bomb invisible; remember to set ishasbomb for thisplayer to false
                                Log.i("up", "no!");
                            }
                            break;
                        }

                    }
                }
                return true;
            }
        });
    }

    private void scaleIn(View v, int childID){
        Animation anim;
        switch (childID){
            case 2:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_in_green);
                break;
            }
            case 1:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_in_yellow);
                break;
            }
            default:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_in_normally);
                break;
            }
        }

        v.startAnimation(anim);
        anim.setFillAfter(true);
    }

    private void scaleOut(View v, int childID){
        Animation anim;
        switch (childID){
            case 2:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_out_green);
                break;
            }
            case 1:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_out_yellow);
                break;
            }
            default:
            {
                anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_out_normally);
                break;
            }
        }

        v.startAnimation(anim);
        anim.setFillAfter(true);
    }


    private boolean checkInterSection(View view, int childID, float rawX, float rawY) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int width = view.getWidth();
        int height = view.getHeight();
        switch (childID){
            case 1: //yellow field
            {
                y -= width ;
                width = view.getHeight();
                height = view.getWidth();
                break;
            }
            case 2: //green field
            {
                x -= height;
                width = view.getHeight();
                height = view.getWidth();
                break;
            }
            default: //case 0: red field or blue field
            {
                break;
            }
        }
        //Check the intersection of point with rectangle achieved
        return rawX > x && rawX < (x+width) && rawY>y && rawY <(y+height);
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
