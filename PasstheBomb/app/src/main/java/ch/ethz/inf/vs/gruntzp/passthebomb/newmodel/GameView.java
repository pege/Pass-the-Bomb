package ch.ethz.inf.vs.gruntzp.passthebomb.newmodel;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import ch.ethz.inf.vs.gruntzp.passthebomb.Communication.ServiceConnector;
import ch.ethz.inf.vs.gruntzp.passthebomb.activities.GameActivity;
import ch.ethz.inf.vs.gruntzp.passthebomb.activities.R;
import ch.ethz.inf.vs.gruntzp.passthebomb.gameModel.AudioService;

/**
 * Created by Neptun on 27.12.2016.
 */

public class GameView {
    private GameActivity gameActivity;


    public GameView(GameActivity gameActivity)
    {
        this.gameActivity = gameActivity;
    }




    //region -- Sound Region --
    private Sound sound = new Sound();
    public Sound Sound() { return sound; }
    public static class Sound {

        private AudioService audioService;
        //boolean to check whether bound to audioservice or not
        private boolean soundServiceBound;

        public boolean isSoundServiceBound() { return soundServiceBound; }
        public void unboundSoundService() { soundServiceBound = false; }
        private ServiceConnection soundServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                AudioService.LocalBinder binder = (AudioService.LocalBinder) service;
                audioService = binder.getService();
                soundServiceBound = true;

            }
            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                soundServiceBound = false;
            }
        };
        public ServiceConnection getSoundServiceConnection() { return soundServiceConnection; }


        // use R.raw.filename for arguments
        public void playSound(int soundfile){
            audioService.playSound(soundfile);
        }


        public void playTapSound(){
            audioService.playTap();
        }


        //use R.raw.filename for arguments
        private void changeBGM(int musicfile){
            //TODO: shitfix. audioService can be null, shortly after creation of gameActivity
            if (audioService!= null)
                audioService.playAudio(musicfile);
        }

        private int[] musicArray = new int[] {R.raw.bomb_stage1, R.raw.bomb_stage2, R.raw.bomb_stage3, R.raw.bomb_stage4, R.raw.bomb_stage5};
        public void setBackgroundMusicByBombLevel(int bombLevel)
        {
            changeBGM(musicArray[bombLevel - 1]);
        }
    }

    //endregion
}
