package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageFactory;
import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;
import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.ServiceConnector;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Game;
import ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic.Player;

public class CreateActivity extends AppCompatActivity implements MessageListener{

    private Switch passwordSwitch;
    private EditText passwordField;
    private EditText gameName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        gameName = (EditText) findViewById(R.id.edit_name);

        managePasswordField();
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
                String name = gameName.getText().toString();

                controller.sendMessage(MessageFactory.createGame(name, password));

                //TODO give the server the game information
                //create the game

                Bundle extras = getIntent().getExtras();
                String creatorName = extras.getString("creator_name");
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
        if (type == MessageFactory.SC_GAME_UPDATE) {
            Intent myIntent = new Intent(this, LobbyActivity.class);

            myIntent.putExtra("message", body.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.unbind(this);
    }
}
