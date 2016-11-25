package ch.ethz.inf.vs.gruntzp.passthebomb.activities;

import java.io.Serializable;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.MessageListener;

/**
 * Created by Michelle on 25.11.2016.
 */

public class Reference implements Serializable {
    public static MessageListener activity;

    public void setActivity(MessageListener activity) {
        this.activity = activity;
    }

    public MessageListener getActivity() {
        return activity;
    }
}