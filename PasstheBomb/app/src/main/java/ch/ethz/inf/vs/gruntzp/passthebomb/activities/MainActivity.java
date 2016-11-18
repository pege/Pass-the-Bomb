package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdit   = (EditText)findViewById(R.id.text_field);

        //TODO: save the username in the text field -> use preferences or something
    }

    public void onClickCreate(View view) {
        if(mEdit.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(this, R.string.username_required, Toast.LENGTH_SHORT);
            toast.show();
        }else{
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
            Intent myIntent = new Intent(this, JoinActivity.class);
            myIntent.putExtra("player_name", mEdit.getText().toString());
            this.startActivity(myIntent);
        }
    }

    public void onClickTutorial(View view) {
        Intent myIntent = new Intent(this, TutorialActivity.class);
        this.startActivity(myIntent);
    }
}
