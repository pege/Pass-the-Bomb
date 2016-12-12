package ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic;

/**
 * Created by niederbm on 12/12/16.
 */

public class Bomb { //Used because Java integers are immutable

    private int counter;

    public Bomb(int value) {
        counter = value;
    }

    public void decrease() {
        --counter;
    }

    public int valueOf() {
        return counter;
    }
}
