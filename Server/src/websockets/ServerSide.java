package websockets;

import java.io.IOException;
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


@ServerEndpoint("/echo")
public class ServerSide {
	
	private static Set<Session> sessions = new HashSet<>();
	
	public static void main(String[] args) throws DeploymentException, InterruptedException, IOException{
		Server server = new Server("localhost", 8080, "/websockets", null, ServerSide.class);
		server.start();
		
		Scanner sc = new Scanner(System.in);
		String mess="";
		
		while(!mess.equals("exit")){
			System.out.println("Something to send? (Current number of Clients: " + Integer.toString(sessions.size()) + ")");
			mess = sc.nextLine();
			
			for(Session s : sessions){
				if (s.isOpen())
					s.getBasicRemote().sendText(mess);
				else
					System.out.println("Session in Set which hasnt an openConnection");
			}
		}
		
		//latch.await();
		System.out.println("Server stopped");
		server.stop();	
	}
	
	@OnOpen
    public void onOpen(Session session) throws IOException{
        System.out.println(session.getId() + " has opened a connection");
        sessions.add(session);
        session.getBasicRemote().sendText("S: Connection Established");
    }
 
    @OnMessage
    public void onMessage(String message, Session session){
        System.out.println("Message from " + session.getId() + ": " + message);
    }
    
    @OnMessage
    public void onPong(PongMessage pongMessage, Session session) {
    	System.out.println("Pong received");
    }
 
    //If connection get closed
    @OnClose
    public void onClose(Session session){
        sessions.remove(session);
    	System.out.println("S: Session " +session.getId()+" has ended");
        //this.session=null;
        //latch.countDown();
    }
    
    @OnError
    public void onError(Session session, Throwable t){
    	System.out.println("Höu");
    	
    }
    
   
}
