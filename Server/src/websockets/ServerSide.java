package websockets;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.glassfish.tyrus.server.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerSide {

	private static Set<Session> sessions = new HashSet<>();
	private JSONParser parser = new JSONParser();
	private static Session sessionLocal = null;

	public static CountDownLatch serverRunning = new CountDownLatch(1);

	public static void main(String[] args) throws DeploymentException, InterruptedException, IOException {

		System.out.println(InetAddress.getLocalHost());
		Server server = new Server("localhost", 8000, "/websockets", null, Connection.class);
		server.start();

		serverRunning.await();

	}

}
