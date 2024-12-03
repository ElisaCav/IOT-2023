package com.example.progetto;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.Database.DatabaseContract;
import com.example.progetto.Database.DatabaseContract.ContactsEntry;

import java.util.List;

/*
   ContactsAdapter è un adapter personalizzato per la RecyclerView che gestisce la visualizzazione
   di una lista di contatti. Implementa la selezione degli elementi e fornisce un'interfaccia per
   gestire gli eventi di clic sugli elementi e sulla rimozione di essi. */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private static final String TAG = "ContactsAdapter";
    private List<ContactsEntry> contacts;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnItemClickListener listener;

    // Interfaccia per gestire gli eventi di clic sugli elementi
    public interface OnItemClickListener{

        void onItemClick(int position);

    }

    // Metodo per impostare l'ascoltatore per gli eventi di clic sugli elementi
    public void setOnItemClickListener(OnItemClickListener clickListener){

        listener = clickListener;
    }

    // Metodo per impostare la lista di contatti
    public void setContacts(List<ContactsEntry> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }


    // Metodo chiamato quando RecyclerView ha bisogno di un nuovo ViewHolder
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(itemView, listener);
    }

    // Metodo chiamato per visualizzare i dati nella posizione specificata
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        if (contacts != null) {
            ContactsEntry currentContact = contacts.get(position);
            holder.nameTextView.setText(currentContact.getName());
            holder.phoneNumberTextView.setText(currentContact.getPhone_number());
            // Imposta il colore di sfondo in base alla selezione
            holder.itemView.setBackgroundColor(selectedPosition == position ? Color.GRAY : Color.TRANSPARENT);
            // Aggiungi un click listener per ogni item
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = holder.getAdapterPosition();
                    // Deseleziona l'elemento precedente, se presente
                    int previousSelectedPosition = selectedPosition;
                    selectedPosition = adapterPosition;
                    notifyItemChanged(previousSelectedPosition);
                    // Seleziona l'elemento corrente
                    notifyItemChanged(selectedPosition);
                }
            });
        }
    }

    // Metodo chiamato per ottenere il numero totale di elementi nella RecyclerView
    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    // ViewHolder che contiene gli elementi della vista per ogni elemento nella RecyclerView
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView phoneNumberTextView;
        private final ImageView imageView;


        // Costruttore del ViewHolder
        public ContactViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.removeimg);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Gestisce l'evento di clic sull'icona di rimozione
                    listener.onItemClick(getAdapterPosition());
                }
            });

            nameTextView = itemView.findViewById(R.id.textName);
            phoneNumberTextView = itemView.findViewById(R.id.textNumber);
        }
    }

    // Metodo per ottenere un contatto in base alla posizione
    public ContactsEntry getItem(int position) {
        return contacts.get(position);
    }

    // Metodo per aggiungere un nuovo contatto alla lista
    public void addContact(DatabaseContract.ContactsEntry newContact) {
        contacts.add(newContact);
    }

    // Metodo per notificare l'inserimento di un nuovo contatto
    public void notifyContactInserted() {
        notifyItemInserted(contacts.size() - 1);
    }

    // Metodo per rimuovere un contatto dalla lista
    public void removeContact(int position) {
        contacts.remove(position);
        notifyItemRemoved(position);
    }
}


