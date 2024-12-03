package com.example.progetto;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/* BrSend è un BroadcastReceiver che gestisce gli esiti di broadcast relativi all'invio di SMS. */
public class BrSend extends BroadcastReceiver {
    private static final String TAG = "brSend";
    @Override
    public void onReceive(Context context, Intent intent) { //Viene chiamato quando il BroadcastReceiver riceve un intento di broadcast
        String action = intent.getAction();
        Log.d(TAG, "br");

        if(SmsSender.SMS_SENT_ACTION.equals(action)) {
            // L'azione è relativa all'invio di SMS
            Log.d(TAG, "sent");
            handleSmsResult(context, intent);
        }
    }

    private void handleSmsResult(Context context, Intent intent) { //Gestisce il risultato dell'invio
        if (getResultCode() == Activity.RESULT_OK) {
            Log.d(TAG, "SMS sent successfully");
            Toast.makeText(context, "SMS inviato correttamente", Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Error sending SMS");
            Toast.makeText(context, "Errore invio SMS", Toast.LENGTH_LONG).show();
        }
    }
}
