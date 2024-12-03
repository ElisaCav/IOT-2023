package com.example.progetto;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.Database.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;


/* EmergencyHandlerActivity gestisce la schermata di gestione delle emergenze, inclusa
   la visualizzazione di un timer e l'invio automatico di SMS in caso di timeout.*/

public class EmergencyHandlerActivity extends AppCompatActivity {

    private static final String TAG = "EmergencyHandlerActivity";
    private static final long TIMEOUT_MILLIS = 60000; // 1 minute in milliseconds
    Button bttSendSms;
    private CountDownTimer emergencyTimer;
    private TextView tvTimer;
    private Boolean isSetContactPreference;
    private Boolean isSetCallEmergency;
    private OnBackPressedCallback callback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ermergency_handler);

        bttSendSms = findViewById(R.id.bttSendSms);
        Button closeButton = findViewById(R.id.closeButton);
        tvTimer = findViewById(R.id.tvTimer);

        // Recupera le preferenze di contatto ed emergenza
        isSetContactPreference = getCallContactsPreference();
        isSetCallEmergency = getCallEmergencyPreference();
        // Crea e avvia il timer di emergenza
        emergencyTimer = createEmergencyTimer();
        emergencyTimer.start();
        // Carica i contatti dal database
        List<String> phoneNumbers = loadPhoneNumbers();

        // Verifica le condizioni per gestire correttamente l'emergenza
        if( (isSetContactPreference == false && isSetCallEmergency == false) || (isSetContactPreference == true && (phoneNumbers == null || phoneNumbers.isEmpty()) && isSetCallEmergency==false)){

            emergencyTimer.cancel();
            tvTimer.setText("nessuna preferenza selezionata");
        }


        // Creazione del callback per gestire il pulsante "Indietro"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                emergencyTimer.cancel();
                finish();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        // Bottone per attivare l'emergenza senza attendere la terminazione del timer
        bttSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergencyTimer.cancel();

                performTimeoutAction(isSetContactPreference,isSetCallEmergency);
                Log.d(TAG, "bttSendSms clicked!");
            }
        });


        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergencyTimer.cancel();
                finish();
            }
        });
    }


    //crea un timer con durata di 60 secondi
    private CountDownTimer createEmergencyTimer() {
        return new CountDownTimer(TIMEOUT_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                // update tvTimer
                String timerText = String.format("Timer: %02d:%02d", secondsRemaining / 60, secondsRemaining % 60);
                Log.d(TAG, timerText);
                tvTimer.setText(timerText);
            }

            @Override
            public void onFinish() {
                // when the timer reaches zero
                Log.d(TAG, "Emergency timer finished. Sending automatic SMS.");
                performTimeoutAction(isSetContactPreference,isSetCallEmergency);
            }

        };
    }


    //Ottiene le preferenze salvate negli switch delle impostazioni
    private boolean getCallEmergencyPreference() {
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        return sharedPreferencesManager.getSwitchState("switchEmergencyNumber", false); // false valore di default, resituito se non si trova key
    }

    private boolean getCallContactsPreference() {
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        return sharedPreferencesManager.getSwitchState("switchContact", false); // false valore di default, resituito se non si trova key
    }



        //Gestisce l'evento allo scadere del timer
    private void performTimeoutAction(boolean contactPreference, boolean emergencynumberPreference ) {

        Context context = getApplicationContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (contactPreference) {
                // Carica i contatti dal database
                List<String> phoneNumbers = loadPhoneNumbers();
                if ((phoneNumbers == null || phoneNumbers.isEmpty()) && !emergencynumberPreference) {
                    tvTimer.setText("Nessun destinatario disponibile");

                } else {
                    String msg = "SMS di emergenza";
                    // send an SMS to each number in the list
                    for (String phoneNumber : phoneNumbers) {
                        SmsSender.sendSms(this, phoneNumber, msg, lastKnownLocation);

                        tvTimer.setText("SMS inviato");

                    }
                }
            }
            if (emergencynumberPreference) {

                Toast.makeText(this, "Simulo invio sms al 112", Toast.LENGTH_LONG).show();
                tvTimer.setText("SMS inviato");

            }
        }
    }

    // Metodo per caricare i numeri di telefono dal database in un thread separato (per non bloccare l'interfaccia grafica)
    private List<String> loadPhoneNumbers () {
        List<String> phoneNumbers = new ArrayList<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            // get the phone numbers from the database
            phoneNumbers.addAll(AppDatabase.getInstance(this).contactsDao().getEmergencyPhoneNumbers());
            Log.d(TAG, "Emergency phone numbers from database: " + phoneNumbers);
        });

        // Wait for the end of the execution of the thread
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                Log.e(TAG, "Thread did not terminate within the expected time.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return phoneNumbers;
    }
}
