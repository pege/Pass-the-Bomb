package ch.ethz.inf.vs.gruntzp.passthebomb.gamelogic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import ch.ethz.inf.vs.gruntzp.passthebomb.activities.R;

/**
 * Created by duong on 16/12/2016.
 */

public class AudioService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer bgm;
    /**
          * Class used for the client Binder.  Because we know this service always
          * runs in the same process as its clients, we don't need to deal with IPC.
          */
    public class LocalBinder extends Binder {
        AudioService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AudioService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public void startAudio(int audiofile){
        bgm = MediaPlayer.create(this, audiofile);
        bgm.setLooping(true);
        bgm.start();
    }

    public void stopAudio(){
        if (bgm != null) {
            bgm.stop();
            bgm.release();
        }
    }
}
