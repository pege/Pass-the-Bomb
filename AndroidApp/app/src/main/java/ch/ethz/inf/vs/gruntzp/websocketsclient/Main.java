package ch.ethz.inf.vs.gruntzp.websocketsclient;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.util.concurrent.CountDownLatch;


public class Main extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static TextView textView;
    public static EditText editText;
    public static EditText ipText;
    public static CountDownLatch latch;
    public static CountDownLatch messageLatch;


    @Override
    protected void onStart() {
        super.onStart();

        //initialize GUI
        messageLatch = new CountDownLatch(1);
        latch = new CountDownLatch(1);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.sendText);
        ipText = (EditText) findViewById(R.id.ip);
        final String[] ip = {ipText.getText().toString()};

        final Button button = (Button) findViewById(R.id.connectButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip[0] = ipText.getText().toString();
                Connection conn = new Connection(ip[0]);
                conn.execute();
            }
        });

        final Button send = (Button) findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mess = editText.getText().toString();
                Client.messageToSend = mess;
                Client.sendMessage();
            }
        });
    }

 }
