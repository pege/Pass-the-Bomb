package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class CreateActivity extends AppCompatActivity {

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
                Intent myIntent = new Intent(this, LobbyActivity.class);
                myIntent.putExtra("creator", true);
                Bundle extras = getIntent().getExtras();
                myIntent.putExtra("creator_name", extras.getString("creator_name"));
                myIntent.putExtra("game_name", gameName.getText().toString());
                myIntent.putExtra("passwordChecked", passwordSwitch.isChecked());
                if (passwordSwitch.isChecked()) {
                    myIntent.putExtra("password", passwordField.getText().toString());
                }
                this.startActivity(myIntent);
            }
        }
    }
}
