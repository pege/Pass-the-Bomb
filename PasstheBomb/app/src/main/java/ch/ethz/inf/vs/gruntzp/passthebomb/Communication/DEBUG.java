package ch.ethz.inf.vs.gruntzp.passthebomb.Communication;

/**
 * Created by Neptun on 25.12.2016.
 */

public  final class DEBUG {
    public static final DEBUG_SETTINGS settings = DEBUG_SETTINGS.Local;

    public static enum DEBUG_SETTINGS {
        ReadyToShip,
        WithoutTimeout,
        Local
    }
}
