package com.andsomore.sosinfosante.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.andsomore.sosinfosante.entite.Intervention;
import com.andsomore.sosinfosante.idao.IIntervention;
import com.andsomore.sosinfosante.idao.IResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;

import static android.content.ContentValues.TAG;

public class TraitementIntervention implements IIntervention<Intervention> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));

    @Override
    public void demarrerIntervention(Intervention intervention, IResult result) {

        db.collection("INTERVENTION")
                .document(intervention.getIdIntervention())
                .set(intervention)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        geo.setLocation(intervention.getIdIntervention(), new GeoPoint(intervention.getLatitude(), intervention.getLongitude()), null, new GeoFirestore.CompletionListener() {
                            @Override
                            public void onComplete(Exception e) {
                                if (e == null) {
                                    result.getResult(1);
                                } else Log.e("Erreur", e.getMessage().toString());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Erreur: ", e);
                result.getResult(0);
            }
        });
    }

}

