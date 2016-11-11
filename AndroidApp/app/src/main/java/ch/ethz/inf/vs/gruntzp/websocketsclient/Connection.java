package ch.ethz.inf.vs.gruntzp.websocketsclient;

import android.os.AsyncTask;

import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;



public class Connection extends AsyncTask<Void, Void, Void> {

    private String ip;

    public Connection(String ip){
        this.ip=ip;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final ClientManager client = ClientManager.createClient();
        try {

            URI uri = new URI("ws://" + ip + ":8080/websockets/echo");
            //URI uri = new URI("ws://192.168.0.11:8080/websockets/echo");
            client.connectToServer(Client.class, uri);
            System.out.println("Connected");

            Main.latch.await();
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Diconnected");

        return null;
    }
}
