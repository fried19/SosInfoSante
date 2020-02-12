package com.andsomore.sosinfosante;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andsomore.sosinfosante.entite.Incident;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ListeIncidentFragmentActivity extends Fragment {
    private View view;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference incidentRef = db.collection("INCIDENT");
    private ListeIncidentAdapter adapter;
    SharedPreferences preference;

    public ListeIncidentFragmentActivity() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.liste_incident_fragment, container, false);
        InitView();
        setUpRecyclerView();
        return view;
    }

    private void setUpRecyclerView() {
        String idUtilisateur = preference.getString("idUtilisateur", "null");
        Query query = incidentRef
                .whereEqualTo("idUtilisateur", idUtilisateur)
                .orderBy("dateIncidence", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Incident> options = new FirestoreRecyclerOptions.Builder<Incident>()
                .setQuery(query, Incident.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new ListeIncidentAdapter(options, getActivity());
        RecyclerView recyclerView = view.findViewById(R.id.rvListeIncident);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManage(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        //Suppression d'une réservation dès qu'on glisse le RecyclerView
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                if (adapter.deleteItem(viewHolder.getAdapterPosition()))
                    Toast.makeText(getActivity(), "Sos supprimé avec succès", Toast.LENGTH_SHORT).show();
                else Toast.makeText(getActivity(), "Impossible de supprimer ce Sos.Les sappeurs pompiers sont sur l'opération", Toast.LENGTH_SHORT).show();


            }


        }).attachToRecyclerView(recyclerView);

    }

    private void InitView() {
        preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView();
    }
}
