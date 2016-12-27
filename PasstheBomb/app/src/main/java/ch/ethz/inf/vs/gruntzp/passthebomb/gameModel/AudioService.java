package ch.ethz.inf.vs.gruntzp.passthebomb.gameModel;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

import ch.ethz.inf.vs.gruntzp.passthebomb.activities.R;

/**
 * Created by duong on 16/12/2016.
 */

public class AudioService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer bgm;
    private MediaPlayer sound;
    private MediaPlayer tap;


    /**
          * Class used for the client Binder.  Because we know this service always
          * runs in the same process as its clients, we don't need to deal with IPC.
          */
    public class LocalBinder extends Binder {
        public AudioService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        bgm = MediaPlayer.create(this, R.raw.bomb_stage1);
        bgm.setLooping(true);
        bgm.start();
        tap = MediaPlayer.create(this, R.raw.bomb_tap);
    }

    @Override
    public void onDestroy() {
        if (bgm != null) {
            bgm.stop();
            bgm.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public void playAudio(int audiofile){
        if (bgm != null) {
            bgm.stop();
            bgm.reset();
            bgm.release();
        }
        bgm = MediaPlayer.create(this, audiofile);
        bgm.setLooping(true);
        bgm.start();
    }

    public void stopAudio(){
        if (bgm != null) {
            bgm.stop();
            bgm.reset();
            bgm.release();
            bgm = null;
        }
    }


    public void playSound(int soundfile){
        if (sound != null) {
            sound.stop();
            sound.reset();
            sound.release();
        }
        sound = MediaPlayer.create(this, soundfile);
        sound.start();

    }
    
    public void playTap(){
        if (tap.isPlaying()){
            tap.stop();
            try {
                tap.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tap.start();
       
    }

    public void pauseMusic(){
        if (bgm != null && bgm.isPlaying()){
            bgm.pause();
        }
    }

    public void resumeMusic(){
        if (bgm != null && !bgm.isPlaying())
            bgm.start();
    }
}
