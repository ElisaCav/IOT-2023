package com.example.progetto;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.Database.AppDatabase;
import com.example.progetto.Database.ContactsDao;
import com.example.progetto.Database.DatabaseContract;

import java.math.BigInteger;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * L'activity SettingsActivity gestisce le impostazioni dell'applicazione,
 * consentendo all'utente di configurare i contatti di emergenza e attivare/disattivare
 * le funzionalità correlate.
 */

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "SettingsActivity";
    private SwitchCompat switchEmergencyNumber = null, switchContact = null;
    private Button bttAddContact = null, bttContactSubmit = null;
    private EditText etAddContactName = null, etAddContactNumber = null;
    private ArrayList<BigInteger> toCall;
    private ContactsDao contactsDao;
    private ContactsAdapter contactsAdapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inizializza gli elementi dell'UI
        switchEmergencyNumber = findViewById(R.id.switchEmergencyNumber);
        switchContact = findViewById(R.id.switchContact);
        etAddContactName = findViewById(R.id.etAddContactName);
        etAddContactNumber = findViewById(R.id.etAddContactNumber);
        bttAddContact = findViewById(R.id.bttAddContact);
        bttContactSubmit = findViewById(R.id.bttContactSubmit);

        // Inizializza il database e il DAO
        AppDatabase database = AppDatabase.getInstance(this);
        contactsDao = database.contactsDao();





        // Inizializza la RecyclerView e il suo adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new ContactsAdapter();
        recyclerView.setAdapter(contactsAdapter);
        contactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                DatabaseContract.ContactsEntry contact = contactsAdapter.getItem(position);
                removeSelectedContact(contact, position);
            }
        });

        // Recupera lo stato dello switch dalle SharedPreferences
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(SettingsActivity.this);
        boolean switchEmergencyState = sharedPreferencesManager.getSwitchState("switchEmergencyNumber", false);
        // Imposta lo stato dello switch
        switchEmergencyNumber.setChecked(switchEmergencyState);
        switchEmergencyNumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Gestione cambio di stato dello switchEmergencyNumber
                if (isChecked) {
                    //(ON)
                    sharedPreferencesManager.saveSwitchState("switchEmergencyNumber", true);
                    Toast.makeText(SettingsActivity.this, "Informazione salvata", Toast.LENGTH_LONG).show();
                } else {
                    // (OFF)
                    sharedPreferencesManager.saveSwitchState("switchEmergencyNumber", false);
                    Toast.makeText(SettingsActivity.this, "Informazione salvata", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Listener per il click sul pulsante per aggiungere un contatto
        bttAddContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                etAddContactName.setVisibility(View.VISIBLE);
                etAddContactNumber.setVisibility(View.VISIBLE);
                bttContactSubmit.setVisibility(View.VISIBLE);
            }
        });

        // Listener per il click sul pulsante di submit per l'aggiunta di un contatto
        bttContactSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
                etAddContactName.setVisibility(View.INVISIBLE);
                etAddContactNumber.setVisibility(View.INVISIBLE);
                bttContactSubmit.setVisibility(View.INVISIBLE);
            }
        });

        // Retrieve switch state from SharedPreferences
        boolean switchContactState = sharedPreferencesManager.getSwitchState("switchContact", false);
        // Set the switch state
        switchContact.setChecked(switchContactState);
        switchContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // Gestione cambio di stato dello switchContact
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // (ON) ->
                    sharedPreferencesManager.saveSwitchState("switchContact", true);
                    Toast.makeText(SettingsActivity.this, "Informazione salvata", Toast.LENGTH_LONG).show();
                }
                else{
                    // (OFF)
                    sharedPreferencesManager.saveSwitchState("switchContact", isChecked);
                    Toast.makeText(SettingsActivity.this, "Informazione salvata", Toast.LENGTH_LONG).show();
                }
            }
        });



        // Carica inizialmente tutti i contatti dal database
        loadContacts();
    }

    // Carica tutti i contatti dal database e li visualizza nella RecyclerView
    private void loadContacts() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final List<DatabaseContract.ContactsEntry> contacts = contactsDao.getAllContacts();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactsAdapter.setContacts(contacts);
                    }
                });
            }
        });
    }

    // Aggiunge un nuovo contatto al database e all'adapter
    private void addContact() {
        final String name = etAddContactName.getText().toString().trim();
        final String phoneNumber = etAddContactNumber.getText().toString().trim();

        if (!name.isEmpty() && !phoneNumber.isEmpty()) {
            final DatabaseContract.ContactsEntry newContact = new DatabaseContract.ContactsEntry();
            newContact.name = name;
            newContact.phone_number = phoneNumber;

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    // Inserisce il contatto nel database
                    contactsDao.insertContact(newContact);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Aggiorna la vista
                            contactsAdapter.addContact(newContact);
                            contactsAdapter.notifyContactInserted();
                        }
                    });
                }
            });

            // Pulisce gli editText dopo l'inserimento
            etAddContactName.setText("");
            etAddContactNumber.setText("");
        }
    }

    // Metodo chiamato alla distruzione dell'activity per arrestare l'executorService
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    // Rimuove il contatto selezionato dal database e dall'adapter
    private void removeSelectedContact(DatabaseContract.ContactsEntry contactToRemove, int position) {
        if (contactToRemove != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    if (contactToRemove != null) {
                        // Elimina il contatto dal database
                        contactsDao.deleteContact(contactToRemove);
                        runOnUiThread(new Runnable() {
                            @Override
                            // Aggiorna l'adapter
                            public void run() {
                                contactsAdapter.removeContact(position);
                            }
                        });
                    }
                }
            });
        }

    }

}