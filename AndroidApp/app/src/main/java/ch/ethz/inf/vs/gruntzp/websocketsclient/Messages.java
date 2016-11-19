package ch.ethz.inf.vs.gruntzp.websocketsclient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by PhiSc on 11.11.2016.
 */

public class Messages {
    public static final String CREATE_GAME = "Create Game";
    public static final String LIST_GAMES = "List Games";

    public static String createGame(String game_id, String username, String user_id)
    {
        try {
            JSONObject o = new JSONObject();
            JSONObject header = new JSONObject();
            header.put("game_id", game_id);
            header.put("user_id", user_id);

            JSONObject body = new JSONObject();
            body.put("type", CREATE_GAME);

            o.put("header", header);
            o.put("body", body);

            return o.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String listGames()
    {
        try {
            JSONObject o = new JSONObject();
            JSONObject header = new JSONObject();

            JSONObject body = new JSONObject();
            body.put("type", LIST_GAMES);

            o.put("header", header);
            o.put("body", body);

            return o.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
