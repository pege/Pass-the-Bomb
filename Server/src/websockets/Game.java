package websockets;

import java.util.HashSet;
import java.util.Set;

public class Game {

	private Player creator;
	private Set<Player> players = new HashSet<>();

	private String gameName;
	private String password;

	public Game(Player creator, String gamename, String password) {
		this.gameName = gamename;
		this.password = password;
		this.creator = creator;
		players.add(creator);
	}

	public String getGamename() {
		return gameName;
	}

	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}

	public void setNewCreator() {
		for (Player p : players) {
			if (!creator.equals(p)) {
				creator = p;
				break;
			}
		}
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}

	public String getPlayersName() {

		String s = "";
		for (Player p : players) {
			s = s + p.getName();
		}

		return s;
	}

	public Player getCreator() {
		return creator;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public boolean playerInGame(Player p) {
		return players.contains(p);
	}

	public int numberOfPlayers() {
		return players.size();
	}
}
