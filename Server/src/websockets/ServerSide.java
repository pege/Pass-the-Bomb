package websockets;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;

public class ServerSide {

	public static CountDownLatch serverRunning = new CountDownLatch(1);

	public static void main(String[] args) throws DeploymentException, InterruptedException, IOException {

		System.out.println("Server started at: " + InetAddress.getLocalHost());
		Server server = new Server("localhost", 8080, "/websockets", null, Connection.class);
		server.start();

		serverRunning.await();

	}

}
