package com.example.progetto;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.Manifest;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;


/*
 * SmsSender è una classe che gestisce la costruzione e l'invio degli SMS.
 * Questa classe utilizza un intent per ricevere notifiche sull'esito dell'invio degli SMS.
 * Il metodo principale, sendSms, costruisce il contenuto dell'SMS, inclusa la posizione corrente,
 * e tenta di inviarlo utilizzando il servizio SMS del dispositivo, con opportuni controlli
 * sui permessi.
 */
public class SmsSender {
    public static final String SMS_SENT_ACTION = "com.example.progetto.SMS_SENT";

    //uses an intent to send SMS
    public static void sendSms(Context context, String phoneNumber, String message, Location location) {

        // Intent per la ricezione della notifica sull'invio degli SMS
        Intent intent = new Intent(context, BrSend.class);
        intent.setAction(SMS_SENT_ACTION);
        intent.putExtra("phone_number", phoneNumber);
        intent.putExtra("message", message);

        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
/*
        // Richiedo il servizio di gestione della posizione
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

 */
        if (location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            //Aggiunta della posizione corrente al messaggio
            message += "\nCurrent Location: " + currentLatLng.latitude + ", " + currentLatLng.longitude;

            SmsManager smsManager;
            // Impostazione del listener per il risultato dell'invio dell'SMS
            smsManager = SmsManager.getDefault();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
            } else {
                Toast.makeText(context, "Permesso di inviare sms non concesso", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(context, "Posizione corrente non disponibile", Toast.LENGTH_LONG).show();
        }
    }
}
