package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import org.json.JSONObject;

/**
 * Created by Marc on 25.11.2016.
 */

public interface MessageListener{
    void onMessage(int type, JSONObject body);
    ServiceConnector controller = ServiceConnector.getInstance();
}
