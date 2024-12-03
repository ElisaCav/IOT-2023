package com.example.progetto;

import static com.example.progetto.DetectAccident.mlDetect;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.progetto.Database.AppDatabase;
import com.example.progetto.Database.DatabaseContract;
import com.example.progetto.Database.SensorDataDao;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.IOException;


public class SensorService extends Service implements SensorEventListener {

    private static final String TAG = "SensorService";
    private static final String CHANNEL_ID = "channel1";

    // frequenza di esecuzione del thread che esegue i calcoli sulle finestre in millisecondi
    private static final int FREQUENCY = 300;
    public static int NOTIFICATION_ID = 10;

    private SensorManager sensorManager;
    private Sensor accelerometer;
  //  private Sensor gyroscope;
    float currentTime;


    protected static Queue<Float[]> accValues= new ConcurrentLinkedQueue<>();

  //  protected static Queue<Float[]> gyroValues= new ConcurrentLinkedQueue<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService executorServiceSensorData = Executors.newScheduledThreadPool(1);

    // Costante per la dimensione della finestra
    static final int MAX_ACC_VALUES = 15;

    private SensorDataDao dataDao;
    final DatabaseContract.SensorDataEntry newRow = new DatabaseContract.SensorDataEntry();

    @Override
    public void onCreate() {
        super.onCreate();

        Float [] init={Float.valueOf(0),Float.valueOf(0),Float.valueOf(0),Float.valueOf(0)};
        accValues.offer(init);

        createNotificationChannel();

        // Inizializza il sensore
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        AppDatabase database = AppDatabase.getInstance(this);
        dataDao = database.sensorDataDao();

    }

    //Tramite questo metodo il servizio resterà in esecuzione e rimarrà in tale stato
    //fino a quando non sarà richiamata la stopservice
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        /*
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        */
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        startForeground(NOTIFICATION_ID, buildNotification("Service", "Servizio in esecuzione"));

        //ogni 300 millisecondi eseguiamo i calcoli sulle finestre con delay iniziale 0
        executorServiceSensorData.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Context mContext = getApplicationContext().getApplicationContext();
                List<Float[]> accWindow = new ArrayList<Float[]>();
              //  List<Float[]> gyroWindow = new ArrayList<Float[]>();

                synchronized (SensorService.accValues){
                    // otteniamo una copia della finestra di valori dell'accelerometro da usare per i calcoli
                    accWindow.addAll(SensorService.accValues);
                }
                /*
                synchronized (SensorService.gyroValues){
                    // otteniamo una copia della finestra di valori del giroscopio da usare per i calcoli
                    gyroWindow.addAll(SensorService.gyroValues);
                }
                */
                if(accWindow.size()>=MAX_ACC_VALUES-1) {
                    float avgAcceleration = DetectAccident.calculateAverageAcceleration(accWindow);
                    mlDetect (accWindow);
                    DetectAccident.detectAccident(mContext, avgAcceleration);
                    Log.d("THREAD", "avg acc = " + avgAcceleration + ", accWindow" + accWindow.size());
                }
            }
        }, 0, FREQUENCY, TimeUnit.MILLISECONDS);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.i(TAG, "Service destroyed");
        if(sensorManager!=null) {
            sensorManager.unregisterListener(this);
        }
        // Arresta l'executorService
        executorServiceSensorData.shutdown();
        executorService.shutdown();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Crea un canale per le notifiche
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    //Costruisce la notifica
    private Notification buildNotification(String title, String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_emergency_24)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder.build();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged");
    }

    //Il sensore viene richiamato quando cambia il timestamp o l'accelerometro
    @Override
    public void onSensorChanged(SensorEvent event) {
        //tempo corrente in millisecondi
        currentTime = System.currentTimeMillis();

      //  if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float _x = event.values[0];
            float _y = event.values[1];
            float _z = event.values[2];

            Log.d("ACC", "X: " + _x + ", Y: " +  _y +", Z " + _z);

            Float[] values = {_x, _y, _z, currentTime};

            synchronized (SensorService.accValues) {
                if (accValues.size() >= MAX_ACC_VALUES) {
                    accValues.poll();
                }
                accValues.add(values);
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    newRow.time = String.valueOf(System.currentTimeMillis());
                    newRow.x = String.valueOf(_x);
                    newRow.y = String.valueOf(_y);
                    newRow.z = String.valueOf(_z);
                    dataDao.insertSensorData(newRow);
                }
            });
     //   }
    /*    else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d("Gyroscope", "X: " + x + ", Y: " + y + ", Z: " + z);
        }
    */

    }

    private void showNotification(String title, String contentText) {
        Notification notification = buildNotification(title, contentText);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}