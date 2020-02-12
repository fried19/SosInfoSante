package com.andsomore.sosinfosante;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andsomore.sosinfosante.entite.Incident;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.okhttp.internal.Platform;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ListeIncidentAdapter extends FirestoreRecyclerAdapter<Incident, ListeIncidentAdapter.IncidentHolder> {
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    private final ObservableSnapshotArray<Incident> Snapshots;
    private Context myContext;
    SharedPreferences data ;

    public ListeIncidentAdapter(@NonNull FirestoreRecyclerOptions<Incident> options, Context context) {
        super(options);
        Snapshots = options.getSnapshots();
        myContext=context;

            options.getOwner().getLifecycle().addObserver(this);

    }


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DocumentSnapshot snapshot, int newIndex, int oldIndex) {
        super.onChildChanged(type, snapshot, newIndex, oldIndex);
        switch (type) {
            case ADDED:
                notifyItemInserted(newIndex);
                break;
            case CHANGED:
                notifyItemChanged(newIndex);
                break;
            case REMOVED:
                notifyItemRemoved(oldIndex);
                break;
            case MOVED:
                notifyItemMoved(oldIndex, newIndex);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }
    }

    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        super.onError(e);
        Log.e("Erreur",e.getMessage());
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    public boolean deleteItem(int position){
        if(position != RecyclerView.NO_POSITION ){
            if(getSnapshots().getSnapshot(position).get("status").toString()!="Répondu"){
                getSnapshots().getSnapshot(position).getReference().delete();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onBindViewHolder(@NonNull ListeIncidentAdapter.IncidentHolder holder, int i, @NonNull Incident incident) {
        SimpleDateFormat Dateformatter = new SimpleDateFormat("dd MMMM yyyy",new Locale("fr","FR"));
        SimpleDateFormat Houreformatter = new SimpleDateFormat("HH:mm",new Locale("fr","FR"));
        //Affectation de données au liste_article_item
        String Date,Heure;
        String status = incident.getStatus();

        try {
            Date = Dateformatter.format(incident.getDateIncidence());
            Heure = Houreformatter.format(incident.getDateIncidence());

            holder.tvDescription.setText(incident.getDescription());
            holder.tvQuartier.setText(incident.getQuartier());
            if ((status.equals("Répondu"))) {
                holder.tvStatut.setTextColor(Color.YELLOW);

            } else if((status.equals("Terminé"))){
                holder.tvStatut.setTextColor(Color.GREEN);
            }
            else {
                holder.tvStatut.setTextColor(Color.RED);
            }
            holder.tvStatut.setText(incident.getStatus());
            holder.tvDateIncident.setText(String.format("%s à %s", Date, Heure));

        } catch (Exception e) {

            Log.e("Erreur: ",e.getMessage());
        }

    }


    @NonNull
    @Override
    public IncidentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.liste_incident_item,parent,false);
        return new IncidentHolder(view);
    }

    public class IncidentHolder extends RecyclerView.ViewHolder {
        private TextView tvQuartier;
        private TextView tvDescription;
        private TextView tvDateIncident;
        private TextView tvStatut;

        public IncidentHolder(@NonNull View itemView) {
            super(itemView);
            InitViews();
        }

        private void InitViews() {
            tvQuartier= itemView.findViewById(R.id.tvQuartier);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDateIncident = itemView.findViewById(R.id.tvDateIncidence);
            tvStatut = itemView.findViewById(R.id.tvStatusIncident);

        }
    }
}
