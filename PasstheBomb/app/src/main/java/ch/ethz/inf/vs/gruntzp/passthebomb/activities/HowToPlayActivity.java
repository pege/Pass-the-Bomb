package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import org.glassfish.tyrus.core.Utils;

import java.io.IOException;
import java.io.InputStream;

public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
        WebView browser = (WebView) findViewById(R.id.browser);
        InputStream is = getResources().openRawResource(R.raw.tutorial);

        byte[] b = new byte[0];
        try {
            b = new byte[is.available()];
            is.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String html = new String(b);
        browser.loadData(html, "text/html; charset=utf-8", "UTF-8" );
    }
}
