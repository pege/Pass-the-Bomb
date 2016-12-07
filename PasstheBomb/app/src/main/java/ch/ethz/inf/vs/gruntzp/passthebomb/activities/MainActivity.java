package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;

public class MainActivity extends AppCompatActivity implements MessageListener {

    //TODO: remember uuid when we remember username

    EditText mEdit;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdit   = (EditText)findViewById(R.id.text_field);

        //TODO: save the username in the text field -> use preferences or something
        preferences = getSharedPreferences("Pref", Context.MODE_PRIVATE);
        String username = preferences.getString("user_name", "");
        mEdit.setText(username);
    }

    public void onClickCreate(View view) {
        if(mEdit.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(this, R.string.username_required, Toast.LENGTH_SHORT);
            toast.show();
        }else{
            // save username
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_name", mEdit.getText().toString());
            editor.commit();

            //Start next Activity
            Intent myIntent = new Intent(this, CreateActivity.class);
            myIntent.putExtra("creator_name", mEdit.getText().toString());
            this.startActivity(myIntent);
        }
    }

    public void onClickJoin(View view) {
        if(mEdit.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(this, R.string.username_required, Toast.LENGTH_SHORT);
            toast.show();
        }else{
            // save username
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_name", mEdit.getText().toString());
            editor.commit();

            //Start next Activity
            Intent myIntent = new Intent(this, JoinActivity.class);
            myIntent.putExtra("player_name", mEdit.getText().toString());
            this.startActivity(myIntent);
        }
    }

    public void onClickTutorial(View view) {
        // save username
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", mEdit.getText().toString());
        editor.commit();

        //Start next Activity
        Intent myIntent = new Intent(this, TutorialActivity.class);
        this.startActivity(myIntent);
    }

    @Override
    public void onBackPressed(){
        // save username
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", mEdit.getText().toString());
        editor.commit();

        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        controller.startService(this); // Only do this in MainActivity
        controller.bind(this);
    }

    @Override
    public void onMessage(String message) {
        //TODO
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.unbind(this);
    }
}
