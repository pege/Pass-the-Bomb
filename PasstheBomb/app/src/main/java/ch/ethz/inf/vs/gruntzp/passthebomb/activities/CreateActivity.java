package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Message;
import android.support.annotation.BoolRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

//import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageFactory;
import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageFactory;
import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;
import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.ServiceConnector;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

public class CreateActivity extends AppCompatActivity implements MessageListener{

    private Switch passwordSwitch;
    private EditText passwordField;
    private EditText gameName;
    private String lastRequestedGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        gameName = (EditText) findViewById(R.id.edit_name);

        managePasswordField();

        //button stuff
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/sensei_medium.otf");
        Button create = (Button) findViewById(R.id.create_game2);
        create.setTypeface(font);
        if(Build.VERSION.SDK_INT >= 23) {
            create.getBackground().setColorFilter(getColor(R.color.orange), PorterDuff.Mode.OVERLAY);

        } else {
            //noinspection deprecation
            create.getBackground().setColorFilter(getResources().getColor(R.color.orange), PorterDuff.Mode.OVERLAY);
        }
    }


    /* Display password text field if switch is checked,
    ** else make it invisible
     */
    private void managePasswordField(){
        passwordField = (EditText) findViewById(R.id.edit_password);
        passwordSwitch = (Switch) findViewById(R.id.switch_password);
        passwordSwitch.setChecked(false);
        passwordSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passwordField.setVisibility(View.VISIBLE);
                }else{
                    passwordField.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    public void onClickCreate(View view) {
        if(gameName.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, R.string.game_name_required, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            //require password
            if(passwordSwitch.isChecked() && passwordField.getText().toString().isEmpty()){
                Toast toast = Toast.makeText(this, R.string.password_required, Toast.LENGTH_SHORT);
                toast.show();
            }else {
                String password = passwordField.getText().toString();
                lastRequestedGame = gameName.getText().toString();

                Bundle extras = getIntent().getExtras();
                String creatorName = extras.getString("creator_name");

                controller.sendMessage(MessageFactory.createGame(lastRequestedGame, password, creatorName));

                //TODO give the server the game information
                //create the game



                Boolean locked = passwordSwitch.isChecked();
            }
        }
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
            case MessageFactory.SC_GAME_CREATED:
                Intent myIntent = new Intent(this, LobbyActivity.class);
                try {
                    String newname = body.getJSONObject("game").getString("name");
                    boolean renamed = !lastRequestedGame.equals(newname);
                    myIntent.putExtra("message", body.toString());
                    myIntent.putExtra("renamed", renamed);
                    this.startActivity(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
}
