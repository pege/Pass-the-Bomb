package ch.ethz.inf.vs.gruntzp.passthebomb.newmodel;

import ch.ethz.inf.vs.gruntzp.passthebomb.activities.GameActivity;

/**
 * Created by Neptun on 27.12.2016.
 */

public class GameView {
    public static GameView instance;
    private GameActivity gameActivity;

    public GameView(GameActivity gameActivity)
    {
        instance = this;
        this.gameActivity = gameActivity;
    }

    playSound()
}
