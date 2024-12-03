package com.example.progetto.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/* AppDatabase è una classe Room che rappresenta il database principale dell'applicazione.
   Include definizioni per l'accesso ai dati e la gestione del database SQLite tramite Room.*/

@Database(entities = {DatabaseContract.ContactsEntry.class, DatabaseContract.SensorDataEntry.class}, version = DatabaseContract.DATABASE_VERSION, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase implements AutoCloseable {
    public abstract ContactsDao contactsDao();
    public abstract SensorDataDao sensorDataDao();


    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "UserData")
                    .build();
        }
        return instance;
    }

}