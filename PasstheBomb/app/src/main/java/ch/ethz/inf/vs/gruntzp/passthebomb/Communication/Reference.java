package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

import java.io.Serializable;

/**
 * Created by Marc on 26.11.2016.
 */

class Reference implements Serializable
{
    public static MessageListener activity;

    public void setActivity(MessageListener activity)
    {
        this.activity = activity;
    }

    public MessageListener getActivity(){
        return activity;
    }
}
