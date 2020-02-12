package com.andsomore.sosinfosante.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andsomore.sosinfosante.entite.Utilisateur;
import com.andsomore.sosinfosante.idao.IResult;
import com.andsomore.sosinfosante.idao.IUtilisateur;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class TraitementUtilisateur implements IUtilisateur<Utilisateur> {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    @Override
    public void seConnecter(Context context, Utilisateur utilisateur, final IResult result) {

        SharedPreferences preference=   androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        auth.signInWithEmailAndPassword(utilisateur.getEmail(),utilisateur.getPassword())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        db.collection("UTILISATEUR").document(auth.getCurrentUser().getUid())
                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (e !=null){
                                            Log.e("Erreur",e.getMessage());
                                            result.getResult(0);
                                        }
                                        if(documentSnapshot.exists()){
                                            editor.clear();
                                            editor.putString("idUtilisateur",documentSnapshot.getId());
                                            editor.putString("Nom",  documentSnapshot. getString("Nom"));
                                            editor.putString("Prénom",documentSnapshot.getString("Prénom"));
                                            editor.putString("Profession",documentSnapshot.getString("Profession"));
                                            editor.putString("Téléphone",documentSnapshot.getString("Téléphone"));
                                            editor.putString("Email",documentSnapshot.getString("Email"));
                                            editor.putString("idIntervention",null);
                                            editor.apply();

                                            String email = documentSnapshot.getString("Email");
                                            String password = documentSnapshot.getString("Password");
                                            result.getResult(true);
                                        }
                                    }
                                });
                    }
                }).addOnFailureListener(e -> {
            if(e instanceof FirebaseAuthInvalidCredentialsException){
                String errorCode =  ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();
                switch (errorCode){
                    case "ERROR_INVALID_EMAIL":
                        //   notifyUser("Invalid email");
                        result.getResult(-1);
                        break;
                    case "ERROR_WRONG_PASSWORD":
                        //notifyUser("Invalid password");
                        result.getResult(-3);
                        break;
                }
            }else if(e instanceof FirebaseAuthInvalidUserException) {
                String errorCode =  ((FirebaseAuthInvalidUserException) e).getErrorCode();
                switch (errorCode) {
                    case "ERROR_USER_NOT_FOUND":
                        //   notifyUser("Invalid email");
                        result.getResult(-2);
                        break;
                    case "ERROR_USER_DISABLED":
                        //    notifyUser("User account has been disabled");
                        result.getResult(-4);
                        break;
                }

            }

        });
    }


    @Override
    public void seDeConnecter(Utilisateur utilisateur, IResult result) {

    }

    @Override
    public void modifierInfoCompte(Context context, Utilisateur utilisateur, IResult result) {

    }

    @Override
    public void supprimerCompte(Utilisateur utilisateur) {

    }

    @Override
    public void creerCompte(final Utilisateur utilisateur, final IResult result) {
        auth.createUserWithEmailAndPassword(utilisateur.getEmail(),utilisateur.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task12 -> {
                                //Autre manière pour vérifier la validation de l'email
                                //Patterns.EMAIL_ADDRESS.matcher(utilisateur.getEmail()).matches();
                                Map<String, Object> docData = new HashMap<>();
                                try {
                                    docData.put("idUtilisateur",auth.getCurrentUser().getUid());
                                    docData.put("Nom",utilisateur.getNom());
                                    docData.put("Prénom",utilisateur.getPrenom());
                                    docData.put("Téléphone",utilisateur.getContact());
                                    docData.put("Profession",utilisateur.getProfession());
                                    docData.put("Email",utilisateur.getEmail());
                                    docData.put("Password",utilisateur.getPassword());
                                    docData.put("Token", task12.getResult().getToken());
                                }catch (Exception e){
                                    Log.d("Erreur",e.getMessage());
                                    return;
                                }

                                db.collection("UTILISATEUR").document(auth.getCurrentUser().getUid())
                                        .set(docData)
                                        .addOnCompleteListener(task1 -> {
                                            if(task1.isSuccessful()){
                                                result.getResult(1);
                                            }else{
                                                Log.e("Erreur", task1.getException().getMessage());
                                                result.getResult(0);
                                            }
                                        });
                            });
                        }else{
                            if(task.getException() instanceof FirebaseAuthException){
                                 String errCode = ((FirebaseAuthException)task.getException()).getErrorCode() ;
                            switch (errCode){
                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    result.getResult(-1);
                                    break;
                                case "ERROR_INVALID_EMAIL":
                                    result.getResult(-2);
                                    break;
                                case "ERROR_WEAK_PASSWORD":
                                    result.getResult(-3);
                                    break;
                            }
                            }else{
                                result.getResult(-4);
                            }



                        }
                    }
                });
    }

    @Override
    public void isSecouriste(Context context,String email, String password, IResult result) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        db.collection("SECOURISTE")
                .whereEqualTo("Email",email)
                .whereEqualTo("Password",password)
                //.whereEqualTo("numTelephone",preferences.getString("Telephone","non defini"))
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.getResult().isEmpty()) {
                        for(QueryDocumentSnapshot documentSnapshot:task.getResult() ){
                            editor.clear();
                            editor.putString("Code",documentSnapshot. getString("CodeSecouriste"));
                            editor.apply();
                        }
                        result.getResult(true);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        result.getResult(false);

                    }
                });
    }
}
