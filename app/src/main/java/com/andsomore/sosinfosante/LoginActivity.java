package com.andsomore.sosinfosante;

import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andsomore.sosinfosante.dao.TraitementUtilisateur;
import com.andsomore.sosinfosante.entite.Utilisateur;
import com.andsomore.sosinfosante.idao.IResult;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBack;
    private EditText etEmail, etPassword;
    private Button btConnexion;
    private TextView tvRegister;
    private Animation uptodown, downtoup;
    private RelativeLayout rl1, rl2;
    private AlertDialog alertDialog;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitViews();
        ivBack.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        btConnexion.setOnClickListener(this);
        rl1.setAnimation(uptodown);
        rl2.setAnimation(downtoup);


    }

    private void InitViews() {
        ivBack = findViewById(R.id.ivBack);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);
        btConnexion = findViewById(R.id.btConnexion);
        rl1 = findViewById(R.id.rl1);
        rl2 = findViewById(R.id.rl2);
        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown2);
        downtoup = AnimationUtils.loadAnimation(this, R.anim.downtoup);
        alertDialog = new SpotsDialog(this);
    }

    @Override
    public void onClick(View v) {
        if (v == ivBack) {
            onBackPressed();
            finish();
        }

        if (v == btConnexion) {
            if (isEmpty()) {
                afficherMessageErreur();
            } else {
                String Email, Password;
                Email = etEmail.getText().toString();
                Password = etPassword.getText().toString();
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setEmail(Email);
                utilisateur.setPassword(Password);

                TraitementUtilisateur traitementUtilisateur = new TraitementUtilisateur();
                alertDialog.show();
                alertDialog.setMessage("Connexion en cours...");
                traitementUtilisateur.seConnecter(this, utilisateur, new IResult() {
                    @Override
                    public void getResult(int ok) {
                        if (ok == 0) {
                            alertDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Une erreur quelconque s'est produite!! Veuillez ressayer.", Toast.LENGTH_LONG).show();
                            etEmail.requestFocus();
                        } else if (ok == -1) {
                            alertDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Erreur!!veuillez saisir un email valide", Toast.LENGTH_LONG).show();
                            etEmail.requestFocus();
                        } else if (ok == -2) {
                            traitementUtilisateur.isSecouriste(LoginActivity.this, Email, Password, new IResult() {
                                @Override
                                public void getResult(int val) {

                                }

                                @Override
                                public void getResult(boolean ok) {
                                    if (ok) {
                                        LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainSecouristeActivity.class));
                                        finish();
                                        alertDialog.dismiss();
                                    } else {
                                        alertDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Erreur!!Cet email ou le mot de passe est introuvable.Veuillez saisir un autre", Toast.LENGTH_LONG).show();
                                        etEmail.requestFocus();
                                    }
                                }
                            });

                        } else if (ok == -3) {
                            traitementUtilisateur.isSecouriste(LoginActivity.this, Email, Password, new IResult() {
                                @Override
                                public void getResult(int val) {

                                }

                                @Override
                                public void getResult(boolean ok) {
                                    if (ok) {

                                        LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainSecouristeActivity.class));
                                        finish();
                                        alertDialog.dismiss();
                                    } else {
                                        alertDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Erreur!! Le mot de passe ou l'imail est introuvable.Veuillez saisir un autre", Toast.LENGTH_LONG).show();
                                        etPassword.requestFocus();
                                        etPassword.getText().clear();
                                    }
                                }
                            });

                        } else if (ok == -4) {
                            alertDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Le compte d'utilisateur est désactivé!!Veuillez consulter l'administrateur pour plus d'infos .", Toast.LENGTH_LONG).show();
                            etEmail.requestFocus();
                        }
                    }

                    @Override
                    public void getResult(boolean ok) {
                        {
                            if (ok) {
                                LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                                alertDialog.dismiss();
                            }


                        }
                    }
                });


            }

        }
    }

    public void afficherMessageErreur() {
        if ((TextUtils.isEmpty(etEmail.getText().toString().trim()))
                && (TextUtils.isEmpty(etPassword.getText().toString().trim()))) {
            etEmail.requestFocus();
            etEmail.setError("Veuiller saisir le nom d'utilisateur");
            etPassword.setError("Veuillez saisir le mot de passe");

        } else if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.requestFocus();
            etEmail.setError("Veuiller saisir le nom d'utilisateur");


        } else if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            etPassword.requestFocus();
            etPassword.setError("Veuillez saisir le mot de passe");

        }

    }

    public boolean isEmpty() {

        if ((TextUtils.isEmpty(etEmail.getText().toString().trim()))
                || (TextUtils.isEmpty(etPassword.getText().toString().trim()))) {
            return true;
        } else
            return false;

    }
}
