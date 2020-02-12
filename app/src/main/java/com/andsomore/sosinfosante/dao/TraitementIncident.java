package com.andsomore.sosinfosante.dao;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.andsomore.sosinfosante.entite.Incident;
import com.andsomore.sosinfosante.idao.IIncident;
import com.andsomore.sosinfosante.idao.IResponse;
import com.andsomore.sosinfosante.idao.IResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import static android.content.ContentValues.TAG;

public class TraitementIncident implements IIncident<Incident> {
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    @Override
    public void creerIncident(Context context, Incident incident, IResult result) {

        db.collection("INCIDENT")
                .document(incident.getIdIncident())
                .set(incident).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                if (task.isSuccessful()) {
                    Log.d(TAG, "Utilisateur inséré avec succès");
                    result.getResult(true);

                } else {
                    Log.e(TAG, "Erreur: ", task.getException());
                    result.getResult(false);

                }
            }
        });
    }

    public void checkIfResponded(String idIncident, IResponse response){
        db.collection("INCIDENT")
                .document(idIncident)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists()) {
                            String status = task.getResult().get("status").toString();
                            if (status.equals("En attente de réponse")) {
                                response.getResult(false,null);
                            } else {
                                response.getResult(true,task.getResult().get("codeSecouriste").toString());
                            }
                        }else {
                            response.getResult(0);
                        }
                    }
                });
    }

    public void updateIncident(Incident incident,IResult result){
        db.collection("INCIDENT")
                .document(incident.getIdIncident())
                .update("codeSecouriste",incident.getCodeSecouriste(),"status",incident.getStatus())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       result.getResult(1);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Erreur: ", e);
                result.getResult(0);
            }
        });
    }

    public void endIncident(String idIncident,IResult result){
        db.collection("INCIDENT")
                .document(idIncident)
                .update("status","Terminé")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        result.getResult(1);
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
