package ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.LinkedList;

/**
 * Created by Michelle on 19.11.2016.
 * Class that describes a game.
 */

// implements Parcelable so that it can be put in putExtra()
public class Game implements Parcelable{

    private String name;
    private Player creator;
    private LinkedList<Player> players;
    private Boolean locked;
    private String password;

    public Game(String name, Player creator, Boolean locked, String password){
        this.name = name;
        this.creator = creator;

        this.players = new LinkedList<>();
        players.addFirst(creator);

        this.locked = locked;
        this.password = password;
    }

    public Game(Parcel in){
        name = in.readString();
        creator = in.readParcelable(Player.class.getClassLoader());
        players = new LinkedList<>();
        in.readList(players, Player.class.getClassLoader());
        locked = in.readByte() != 0;
        password = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorName() {return creator.getName();}

    public void setCreator(Player creator) {
        this.creator = creator;
        players.remove(0);
        players.addFirst(creator);
    }


    public Player getCreator() {return creator;}

    public LinkedList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(LinkedList<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public void removePlayer (Player player){
        players.remove(player);
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // leave this empty please
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(creator, flags);
        dest.writeList(players);
        dest.writeByte((byte) (locked ? 1 : 0));
        dest.writeString(password);
    }

    public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>()
    {
        public Game createFromParcel(Parcel in)
        {
            return new Game(in);
        }
        public Game[] newArray(int size)
        {
            return new Game[size];
        }
    };

    public static Game createFromJSON(String jsonGame) {
        JSONObject gameInfo = null;
        JSONTokener tokener = new JSONTokener(jsonGame);
        try {
            gameInfo = new JSONObject(tokener);
            //Retrieve players from game
            JSONArray jArray = new JSONArray(gameInfo.getJSONArray("players"));
            Player p;
            Player c = null;
            String uuid = gameInfo.getString("owner");
            Game game = new Game(gameInfo.getString("name"), null,
                    gameInfo.getBoolean("hasPassword"), gameInfo.getString("password")); //This is evil, null creator should usually be avoided and is okay here because it is set just afterwards
            for(int i = 0; i < jArray.length(); i++) {
                p = new Player(jArray.getJSONObject(i).getString("name"), jArray.getJSONObject(i).getString("uuid"));
                if (uuid.equals(p.getUuid())) {
                    c = p;
                    game.setCreator(c);
                } else {
                    game.addPlayer(p);
                }
            }
            game.setCreator(c);
            return game;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
