package com.vik3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

import com.vik3.ui.dashboard.RadioFragmentNew;

public class BroadcastReceiverBluetooth extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Calling", Toast.LENGTH_SHORT).show();
        final KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event.getAction() != KeyEvent.ACTION_DOWN) return;
//
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_STOP: {
                if (RadioFragmentNew.jcplayer != null) {
                    RadioFragmentNew.jcplayer.pause();
                }
                // stop music
                break;
            }
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY:{
                if (RadioFragmentNew.jcplayer != null) {
                    RadioFragmentNew.jcplayer.continueAudio();
                }
                Toast.makeText(context, "Calling", Toast.LENGTH_SHORT).show();
            }
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                if (RadioFragmentNew.jcplayer != null) {
                    RadioFragmentNew.jcplayer.pause();
                }
                Toast.makeText(context, "Calling", Toast.LENGTH_SHORT).show();
                // pause music
                break;
            }
            case KeyEvent.KEYCODE_MEDIA_NEXT: {
                // next track
                break;
            }
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
                // previous track
                break;
            }
        }
    }

}
