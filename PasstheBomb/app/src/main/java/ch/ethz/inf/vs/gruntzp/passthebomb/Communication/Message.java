package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by niederbm on 11/25/16.
 */

public class Message {
    public enum MessageType {}
    public static final int CREATE_GAME = 1;
    public static final int LIST_GAMES = 2;
    public static final int REGISTER = 3;
    public static final int JOIN_GAME = 4;
    public static final int LEAVE_GAME = 5;
    public static final int PLAYER_LIST = 6;
    public static final int PLAYER_LIST = 7;
    public static final int PLAYER_LIST = 8;
    public static final int PLAYER_LIST = 9;
    public static final int PLAYER_LIST = 10;
    public static final int PLAYER_LIST = 11;
    public static final int PLAYER_LIST = 12;
    public static final int PLAYER_LIST = 13;

    public static String createGame(String game_id, String password)
    {
        try {
            JSONObject header = new JSONObject();
            header.put("type", CREATE_GAME);

            JSONObject body = new JSONObject();
            body.put("game_id", game_id);
            body.put("password", password);

            return compose(header, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String listGames()
    {
        try {

            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();
            header.put("type", LIST_GAMES);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String register(String user_id, String username) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", REGISTER);
            body.put("user_id", user_id);
            body.put("username", username);

            return compose(header,body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String joinGame(String game_id) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", JOIN_GAME);
            body.put("game_id", game_id);

            return compose(header,body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String leaveGame() {
        try {
            JSONObject header = new JSONObject();
            //JSONObject body = new JSONObject();

            header.put("type", LEAVE_GAME);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String playerList(String[] player_names) {
        try {
            JSONObject header = new JSONObject();
            JSONObject body = new JSONObject();

            header.put("type", PLAYER_LIST);

            return compose(header);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String compose(JSONObject header, JSONObject body) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("header", header);
        o.put("body", body);
        return o.toString();
    }

    public static String compose(JSONObject header) throws JSONException {
        return compose(header, new JSONObject());
    }
}
