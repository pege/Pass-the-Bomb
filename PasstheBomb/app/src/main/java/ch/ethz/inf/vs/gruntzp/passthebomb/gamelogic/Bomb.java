package ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by niederbm on 12/12/16.
 */

public class Bomb implements Parcelable{ //Used because Java integers are immutable

    private final int init_value;
    private int counter;
    public static final int blank_initializer = 10;

    public Bomb(int value, int init) {
        counter = value;
        init_value = init;
    }

    public Bomb(Parcel in) {
        this.counter = in.readInt();
        this.init_value = in.readInt();
    }

    public void decrease() {
        --counter;
    }

    public int valueOf() {
        return counter;
    }

    public int initValue() {return init_value;}

    public void setCounter(int c){this.counter = c;}

    public int getLevel() {return ((int) Math.ceil(5.0*(((double)counter)/((double)init_value))));}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(counter);
        dest.writeInt(init_value);
    }

    public static final Parcelable.Creator<Bomb> CREATOR = new Parcelable.Creator<Bomb>()
    {
        public Bomb createFromParcel(Parcel in)
        {
            return new Bomb(in);
        }
        public Bomb[] newArray(int size)
        {
            return new Bomb[size];
        }
    };
}
