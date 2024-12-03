package com.example.progetto.Database;
import com.example.progetto.Database.DatabaseContract.*;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.DeleteTable;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
/* AccidentsDao fornisce i metodi utilizzati dal resto dell'app per interagire con i dati nella tabella 'accidents'*/
@Dao
public interface SensorDataDao {
    @Query("SELECT * FROM sensordata")
    List<DatabaseContract.SensorDataEntry> getAllData();

    @Query("DELETE FROM sensordata")
    void deleteAllData();

    @Insert
    void insertSensorData(DatabaseContract.SensorDataEntry sensorData);

    @Delete
    void deleteSensorData(DatabaseContract.SensorDataEntry sensorData);

}