package com.andsomore.sosinfosante.entite;

public class Agent {
    private  String nomAgence;
    private  String Code;
    private  String login;
    private  String password;
    private  String Contact;
    private String  Adresse;

    public Agent(){}
    public Agent(String nomAgence, String code, String login, String password, String contact, String adresse) {
        this.nomAgence = nomAgence;
        Code = code;
        this.login = login;
        this.password = password;
        Contact = contact;
        Adresse = adresse;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public void setNomAgence(String nomAgence) {
        this.nomAgence = nomAgence;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        Contact = contact;
    }

    public String getAdresse() {
        return Adresse;
    }

    public void setAdresse(String adresse) {
        Adresse = adresse;
    }
}
