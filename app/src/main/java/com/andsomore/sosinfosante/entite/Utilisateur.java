package com.andsomore.sosinfosante.entite;

public class Utilisateur {


    private int idUtilisateur;


    private String Nom;


    private String Prenom;


    private String Profession;


    private String Sexe;

    private String Contact;


    private String Email;

    private String Password;

    public Utilisateur(){}


    public Utilisateur(String nom, String prenom, String profession, String sexe, String contact, String email, String password) {

        Nom = nom;
        Prenom = prenom;
        Profession = profession;
        Sexe = sexe;
        Contact = contact;
        Email = email;
        Password = password;
    }

    public Utilisateur(String nom, String prenom, String profession, String telephone, String email, String pswd) {
        Nom = nom;
        Prenom = prenom;
        Profession = profession;
        Contact = telephone;
        Email = email;
        Password = pswd;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return Nom;
    }

    public void setNom(String nom) {
        Nom = nom;
    }

    public String getPrenom() {
        return Prenom;
    }

    public void setPrenom(String prenom) {
        Prenom = prenom;
    }

    public String getProfession() {
        return Profession;
    }

    public void setProfession(String profession) {
        Profession = profession;
    }

    public String getSexe() {
        return Sexe;
    }

    public void setSexe(String sexe) {
        Sexe = sexe;
    }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        Contact = contact;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
