package com.vik3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class YourBroadcastReceiver extends BroadcastReceiver {
    // Constructor is mandatory
    public YourBroadcastReceiver ()
    {
        super ();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
//        Log.i (TAG_MEDIA, intentAction.toString() + " happended");
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            Toast.makeText(context, ""+intentAction, Toast.LENGTH_SHORT).show();
//            Log.i (TAG_MEDIA, "no media button information");
            return;
        }
        KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
//            Log.i (TAG_MEDIA, "no keypress");
            return;
        }
        // other stuff you want to do
    }
}
