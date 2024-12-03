package com.example.progetto;

import android.content.Context;
import android.content.SharedPreferences;

// SharedPreferencesManager è una classe di utilità che semplifica l'accesso alle SharedPreferences in Android.
// Queste SharedPreferences vengono utilizzate per archiviare piccoli dati di configurazione in modo persistente.
// La classe implementa il pattern singleton per garantire un'unica istanza condivisa nell'applicazione.

public class SharedPreferencesManager {
    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;

    // Costruttore privato per garantire il singleton pattern
    private SharedPreferencesManager(Context context) {
        // Inizializza le SharedPreferences con il nome "user_pref" e il modo privato
        sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
    }

    // Metodo per ottenere l'istanza singleton della classe
    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            // Crea una nuova istanza se non è già presente
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    // Metodo per salvare lo stato di uno switch nelle SharedPreferences
    public void saveSwitchState(String switchKey, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(switchKey, isChecked);
        editor.apply();
    }

    // Metodo per ottenere lo stato di uno switch dalle SharedPreferences
    public boolean getSwitchState(String switchKey, boolean defaultValue) {
        return sharedPreferences.getBoolean(switchKey, defaultValue);
    }

}