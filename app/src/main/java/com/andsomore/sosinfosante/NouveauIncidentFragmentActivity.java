package com.andsomore.sosinfosante;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.andsomore.sosinfosante.dao.TraitementIncident;
import com.andsomore.sosinfosante.entite.Incident;
import com.andsomore.sosinfosante.idao.IResult;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.text.LocalGlyphRasterizer;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

import static android.content.Context.LOCATION_SERVICE;

public class NouveauIncidentFragmentActivity extends Fragment implements View.OnClickListener {
    private View view;
    private EditText etDescription;
    private EditText etQuartier;
    private Button btEnvoyer;
    private AlertDialog alertDialog;
    private RelativeLayout root_incident;

    public NouveauIncidentFragmentActivity() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.nouveau_incident_fragment, container, false);
        InitView();
        btEnvoyer.setOnClickListener(this);
        return view;
    }

    private void InitView() {
        btEnvoyer = view.findViewById(R.id.btEnvoyer);
        etDescription = view.findViewById(R.id.etDescription);
        root_incident = view.findViewById(R.id.root_incident);
        etQuartier = view.findViewById(R.id.etQuartier);
        alertDialog = new SpotsDialog(getActivity());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView();
    }

    @Override
    public void onClick(View v) {
        String date;
        String description = etDescription.getText().toString();
        String quartier = etQuartier.getText().toString();
        Date DatePoste = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", new Locale("fr", "FR"));
            date = sdf.format(new Date());
            DatePoste = sdf.parse(date);
        } catch (Exception e) {
            Log.e("Erreur", e.getMessage());
        }
        if (v == btEnvoyer) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
            DocumentReference newReservationRef = db.collection("INCIDENT").document();
            SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());


            MainActivity main = MainActivity.getInstance();
            final double Longitude = main.getPosition().get(0);
            final double Latitude = main.getPosition().get(1);

            String idIncident = newReservationRef.getId();
            Incident incident = new Incident();
            incident.setDateIncidence(DatePoste);
            incident.setDescription(description);
            incident.setIdIncident(idIncident);
            incident.setQuartier(quartier);
            incident.setLatitude(Latitude);
            incident.setLongitude(Longitude);
            incident.setCodeSecouriste("null");
            incident.setStatus("En attente de réponse");
            incident.setIdUtilisateur(preferences.getString("idUtilisateur", "null"));
            TraitementIncident Tincident = new TraitementIncident();
            alertDialog.show();
            alertDialog.setMessage("Encours d'envoie du SOS");
            Tincident.creerIncident(getActivity(), incident, new IResult() {
                @Override
                public void getResult(int val) {

                }

                @Override
                public void getResult(boolean ok) {
                    if (ok) {
                        alertDialog.dismiss();
                        etQuartier.getText().clear();
                        etQuartier.requestFocus();
                        etDescription.getText().clear();
                        geo.setLocation(idIncident, new GeoPoint(Latitude, Longitude), null, new GeoFirestore.CompletionListener() {
                            @Override
                            public void onComplete(Exception e) {
                                Snackbar.make(root_incident, "SOS envoyé.Veuillez apporter vos premiers soins avant l'intervention des secouristes.", BaseTransientBottomBar.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        alertDialog.dismiss();
                        Toast.makeText(getActivity(), "Une erreur s'est produite.Veuillez réssayer.", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }

    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Le GPS est désactivé.Voulez-vous l'activer?")
                .setCancelable(false)
                .setPositiveButton("Activer",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                Toast.makeText(getActivity(), "Veuillez renvoyer l'SOS", Toast.LENGTH_LONG).show();
                            }
                        });
        alertDialogBuilder.setNegativeButton("Annuler",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


}
