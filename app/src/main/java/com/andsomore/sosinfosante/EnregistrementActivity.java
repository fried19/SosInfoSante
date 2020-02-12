package com.andsomore.sosinfosante;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andsomore.sosinfosante.dao.TraitementUtilisateur;
import com.andsomore.sosinfosante.entite.Utilisateur;
import com.andsomore.sosinfosante.idao.IResult;
import com.google.android.material.snackbar.Snackbar;

import dmax.dialog.SpotsDialog;

public class EnregistrementActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etNom,etPrenom,etTelephone,etEmail,etPassword,etRepassword,etProfession;
    private RadioButton rbM;
    private Button btInscription;
    private AlertDialog alertDialog;
    private RelativeLayout root;
    private View ivBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enregistrement);

        InitViews();
        btInscription.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }

    private void InitViews() {
        etNom=findViewById(R.id.etNom);
        etPrenom=findViewById(R.id.etPrenom);
        etTelephone=findViewById(R.id.etTelephone);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        etRepassword=findViewById(R.id.etRePassword);
        ivBack = findViewById(R.id.ivBack);
        etProfession=findViewById(R.id.etProfession);
        rbM = findViewById(R.id.radioMasculin);
        btInscription=findViewById(R.id.btInscription);
        alertDialog = new SpotsDialog(this);
        root = findViewById(R.id._rootregister);
    }

    @Override
    public void onClick(View v) {
        String Nom=etNom.getText().toString().trim();
        String Prenom=etPrenom.getText().toString().trim();
        String Telephone=etTelephone.getText().toString().trim();
        String Email=etEmail.getText().toString().trim();
        String Pswd=etPassword.getText().toString().trim();
        String Profession = etProfession.getText().toString().trim();
        String Repswd=etRepassword.getText().toString().trim();
        String Sexe="Masculin";
        if(v==btInscription){
            if(isEmpty()){
                afficherMessageErreur();
            }else if(!compareBothPassword(Pswd,Repswd)){
                etRepassword.requestFocus();
                etRepassword.setError("Le mot de passe ne correspond pas");

            }else {
                if(!rbM.isChecked()){
                    Sexe = "Féminin";
                }
                Utilisateur utilisateur=new Utilisateur(Nom,Prenom,Profession,Sexe,Telephone,Email,Pswd);
                TraitementUtilisateur traitementUtilisateur=new TraitementUtilisateur();
                alertDialog.show();
                alertDialog.setMessage("Enrégistrement en cours...");
                traitementUtilisateur.creerCompte(utilisateur, new IResult() {
                    @Override
                    public void getResult(int ok) {
                        if (ok == 1) {
                            alertDialog.dismiss();
                            setResult(1);
//                        View main=findViewById(R.id.main);
                        } else if(ok == 0){
                            alertDialog.dismiss();
                            Toast.makeText(EnregistrementActivity.this, "Erreur!!Veuillez revérifier les données saisies", Toast.LENGTH_LONG).show();
                            etNom.requestFocus();

                        }else  if(ok == -1){
                            alertDialog.dismiss();
                            Toast.makeText(EnregistrementActivity.this, "Cet email existe déja!!Veuillez choisir un autre.", Toast.LENGTH_LONG).show();
                            etEmail.requestFocus();
                        }else  if(ok == -2){
                            alertDialog.dismiss();
                            Toast.makeText(EnregistrementActivity.this, "Erreur!!veuillez saisir un email valide", Toast.LENGTH_LONG).show();
                            etEmail.requestFocus();
                        }else  if(ok == -3){
                            alertDialog.dismiss();
                            Toast.makeText(EnregistrementActivity.this, "Veuillez saisir un mot de passe d'au moins 6 caractères!!", Toast.LENGTH_LONG).show();
                            etPassword.requestFocus();
                            etRepassword.getText().clear();
                        }else  if(ok == -4){
                            alertDialog.dismiss();
                            Toast.makeText(EnregistrementActivity.this, "Vous avez désactivé votre connexion internet.Veuillez la réactiver puis poursuivre", Toast.LENGTH_LONG).show();
                            etPassword.requestFocus();
                        }

                    }

                    @Override
                    public void getResult(boolean ok) { }
                });


            }
        }

        if(v == ivBack){
            onBackPressed();
            finish();
        }
    }
    public boolean isEmpty(){

        if((TextUtils.isEmpty(etEmail.getText().toString()))
                ||(TextUtils.isEmpty(etPassword.getText().toString()))
                ||(TextUtils.isEmpty(etRepassword.getText().toString()))
                ||(TextUtils.isEmpty(etNom.getText().toString()))
                ||(TextUtils.isEmpty(etPrenom.getText().toString()))
                ||(TextUtils.isEmpty(etProfession.getText().toString()))
                ||(TextUtils.isEmpty(etTelephone.getText().toString()))
        )
        {
            return true;
        }else
            return false;

    }

  /*  public boolean isValidEmail(String email){
        boolean valid = EmailValidator.getInstance(false).isValid(email);
        if(valid==true){
            return true;
        }else
            return false;

    }*/

    public boolean compareBothPassword(String pswd,String Repswd){
        if(pswd.equals(Repswd)){
            return true;
        }else return false;
    }

    public void afficherMessageErreur(){
        if((TextUtils.isEmpty(etEmail.getText().toString()))
                &&(TextUtils.isEmpty(etPassword.getText().toString()))
                &&(TextUtils.isEmpty(etRepassword.getText().toString()))
                &&(TextUtils.isEmpty(etNom.getText().toString()))
                &&(TextUtils.isEmpty(etPrenom.getText().toString()))
                &&(TextUtils.isEmpty(etProfession.getText().toString()))
                &&(TextUtils.isEmpty(etTelephone.getText().toString()))
        ){
            etNom.requestFocus();
            etNom.setError("Veuillez saisir le nom");
            etPrenom.setError("Veuillez saisir le prenom");
            etProfession.setError("Veuillez saisir la profession");
            etTelephone.setError("Veuillez siaisr le numero téléphonique");
            etEmail.setError("Veuiller saisir le mail");
            etPassword.setError("Veuillez saisir le mot de passe");
            etRepassword.setError("Veuillez confirmer le mot de passe");

        }else if((TextUtils.isEmpty(etNom.getText().toString()))){
            etNom.requestFocus();
            etNom.setError("Veuillez saisir le nom");
        }else if((TextUtils.isEmpty(etPrenom.getText().toString()))){
            etPrenom.requestFocus();
            etPrenom.setError("Veuillez saisir le prenom");
        }else if((TextUtils.isEmpty(etProfession.getText().toString()))){
            etProfession.requestFocus();
            etProfession.setError("Veuillez saisir la profession");
        }else if(TextUtils.isEmpty(etTelephone.getText().toString())){
            etTelephone.requestFocus();
            etTelephone.setError("Veuiller saisir le numero téléphonique ");
        }
        else if(TextUtils.isEmpty(etEmail.getText().toString())){
            etEmail.requestFocus();
            etEmail.setError("Veuiller saisir le mail");

        }else if(TextUtils.isEmpty(etPassword.getText().toString())){
            etPassword.requestFocus();
            etPassword.setError("Veuillez saisir le mot de passe");

        }else if(TextUtils.isEmpty(etRepassword.getText().toString())){
            etRepassword.requestFocus();
            etRepassword.setError("Veuillez confirmer le mot de passe");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if(resultCode == 1){
                onBackPressed();
                Snackbar.make(root,"Vous êtes maintenant inscrit sur notre plateforme!! ",Snackbar.LENGTH_LONG).show();

            }
        }
    }
}
