package com.example.progetto.Database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*   DatabaseContract definisce lo schema delle tabelle per il database Room dell'applicazione.*/
public class DatabaseContract {
        public static final int DATABASE_VERSION = 3;
        public static final String DATABASE_NAME = "UserData";

        @Entity(tableName = "contacts", primaryKeys = {"phone_number"})
        public static class ContactsEntry {
                @NonNull
                public String phone_number;

                public String name;
                public String getName() {
                        return name;
                }
                @NonNull
                public String getPhone_number() {
                        return phone_number;
                }
        }

        @Entity(tableName = "sensordata")
        public static class SensorDataEntry {
                @PrimaryKey(autoGenerate = true)
                public int id;
                public String time;
                public String x;
                public String y;
                public String z;

        }

}
