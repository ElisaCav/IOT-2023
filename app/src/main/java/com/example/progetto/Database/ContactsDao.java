package com.example.progetto.Database;
import com.example.progetto.Database.DatabaseContract.*;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Insert;

import java.util.List;

/* ContactsDao è un interfaccia che fornisce i metodi utilizzati dal resto dell'app
 per interagire con i dati nella tabella 'contacts' */
@Dao
public interface ContactsDao {
    @Query("SELECT * FROM contacts" )
   List<ContactsEntry> getAllContacts();

    @Query("SELECT phone_number from contacts")
    List<String> getEmergencyPhoneNumbers();

    @Query("SELECT * FROM contacts WHERE phone_number = :number LIMIT 1")
    ContactsEntry getContactByNumber(String number);

    @Insert
    void insertContact(ContactsEntry contact);


   @Delete
    void deleteContact(ContactsEntry contact);
}

