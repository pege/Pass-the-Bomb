package websockets;

import javax.websocket.Session;

public class Player {
	
	private long uuid;
	private String name;
	private int score;
	private boolean hasBomb;
	private boolean isConnected; //if connected or not
	private Session session;
	private boolean inGame; //if in a game or not
	
	
	public Player(String name, long uuid, Session session){
		this.name = name;
		this.uuid = uuid;
		this.session = session;
	}
	
	public String getName(){
		return name;
	}
	
	public long getUuid(){
		return uuid;
	}
	
	public int getScore(){
		return score;
	}
	
	public boolean hasBomb(){
		return hasBomb;
	}
	
	public void changeScore(int amount){
		score = score +amount;
	}
	
	public void setConnection(boolean status){
		isConnected = status;
	}
	
	public boolean isConnected(){
		return isConnected;
	}
	
	public Session getSession(){
		return session;
	}
	
	public void setGameStatus(boolean status){
		inGame = status;
	}
	
	public boolean getGameStatus(){
		return inGame;
	}


}
