package com.example.progetto;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.progetto.databinding.ActivityMainBinding;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.ArrayList;


/*
   MainActivity rappresenta l'activity principale dell'applicazione.
   Questa activity gestisce la mappa, l'aggiornamento della posizione GPS dell'utente
   e le richieste di permessi.*/

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "MainActivity";
    private GoogleMap myMap;
    private Marker currentLocationMarker;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int MIN_DIST = 50;
    private static final int MIN_PERIOD = 50;
    private String providerId = LocationManager.GPS_PROVIDER;
    private ActivityMainBinding binding;
    private static final float DEFAULT_ZOOM = 15.0f;
    private LatLng markerPosition;
    private boolean isFirstLocationUpdate;

    private static final int MY_PERMISSIONS_REQUEST_CODE = 123;

    public static int NOTIFICATION_ID = 10;
    private static final String CHANNEL_ID = "channel1";

    private ArrayList<Float[]> accValues = new ArrayList<>();

    private final static long ACC_CKECK_INTERVAL=1000;
    private long lastAccCheck=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Context context = getApplicationContext();

        binding.bottomNavigationView.setBackground(null);

        checkPermissions(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
        }, MY_PERMISSIONS_REQUEST_CODE);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Menu di navigazione che gestisce anche il riposizione del marker
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.marker) {
                onClickMarker();
                return true;
            }

            if (itemId == R.id.settings) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            }

            return false;
        });

        //Bottone che avvia l'attività di emergenza
        FloatingActionButton sosB = (FloatingActionButton) findViewById(R.id.sosB);
        sosB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EmergencyHandlerActivity.class);
                startActivity(i);
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        checkGpsStatus(locationManager);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateGUI(location);
            }



            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, locationListener);

        //myStartService(context);
    }

    private Notification buildNotification(String title, String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_emergency_24)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder.build();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //Ricontrolla i permessi ed richiede la posizione attuale
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, locationListener);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        //attivo service
        Context context = getApplicationContext();
        Intent intent = new Intent(this, SensorService.class); // Build the intent for the service
        intent.putExtra("inputExtra", "Foreground Service Example in Android");
        context.startForegroundService(intent);

        myMap = googleMap;

        // Verifica se la mappa è pronta prima di iniziare gli aggiornamenti sulla posizione
        if (myMap != null) {
            startLocationUpdates();
        } else {
            Log.e(TAG, "onMapReady: myMap is null");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        Intent serviceIntent = new Intent(this, SensorService.class);
        stopService(serviceIntent);
    }


    //Riposizionamento del marker
    private void onClickMarker() {
        if (myMap != null && markerPosition != null) {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, DEFAULT_ZOOM));
        }
    }

    // Aggiornamento dell'interfaccia utente con la posizione corrente sulla mappa
    private void updateGUI(Location location) {
        Log.d("LocationUpdate", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Aggiorna la posizione nella mappa
        if (myMap != null) {
            LatLng currentLatLng = new LatLng(latitude, longitude);

            // Rimuove il marker vecchio se presente
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }

            // Aggiunge il nuovo marker
            float zoomLevel = 15.0f;
            currentLocationMarker = myMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
            Log.d(TAG, "current coordinates: " + currentLatLng);
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel));

            // Aggiorna la posizione del marker
            markerPosition = currentLatLng;
        }
    }
    // Avvio degli aggiornamenti sulla posizione
    private void startLocationUpdates() {
        Log.d("LocationUpdate","Ask for permission");

        if (myMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocationUpdate", "requirnig the gps location ");
                if (isFirstLocationUpdate) {
                    locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, locationListener);
                    isFirstLocationUpdate=false;
                }
                else  locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(providerId);
                Log.d("LocationUpdate", "last konwn location: " + lastKnownLocation);
                if (lastKnownLocation != null) {
                    updateGUI(lastKnownLocation);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


    //Chiede l'attivazione della posizione
    private void checkGpsStatus(LocationManager locationManager) {
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }


    //Controllo dei permessi
    private void checkPermissions(String[] permissions, int requestCode) {
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Log.d(TAG, "Permission rationale needed for: " + permission);
                } else {
                    Log.d(TAG, "Permission can be requested directly: " + permission);
                }
            } else {
                Log.d(TAG, "Permission already granted: " + permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            requestPermission(permissionsArray, requestCode);
        } else {
            Toast.makeText(this, "Tutti i permessi già concessi", Toast.LENGTH_SHORT).show();
        }
    }


    // Mostra una spiegazione sull'uso dei permessi e richiede il permesso all'utente
    private void showExplanationAndRequestPermission(String title,
                                                     String message,
                                                     final String permission,
                                                     final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(new String[]{permission}, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    // Richiede il permesso all'utente
    private void requestPermission(String[] permissions, int permissionRequestCode) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, permissionRequestCode);
    }

    // Verifica se tutti i permessi sono stati concessi
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    // Feedback della richiesta di permessi dell'utente
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE:
                if (hasAllPermissionsGranted(grantResults)) {
                    // Tutti i permessi sono stati concessi
                    Toast.makeText(this, "Tutti i permessi sono stati abilitati", Toast.LENGTH_SHORT).show();
                } else {
                    // Almeno uno dei permessi non è stato concesso
                    Toast.makeText(this, "Almeno uno dei permessi non è stato abilitato", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




}