package com.andsomore.sosinfosante.entite;

import java.util.Date;

public class Incident {
    private String idUtilisateur;
    private String idIncident;
    private Date   dateIncidence;
    private String status;
    private String description;
    private String quartier;
    private String codeSecouriste;
    private double latitude;
    private double longitude;

    public Incident(String idIncident, String idUtilisateur, Date dateIncidence, String quartier, String description, String status,String codeSecouriste, Double latitude, double longitude) {
        this.idUtilisateur = idUtilisateur;
        this.dateIncidence = dateIncidence;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idIncident = idIncident;
        this.status = status;
        this.quartier = quartier;
        this.codeSecouriste = codeSecouriste;
    }

    public Incident(){}

    public String getCodeSecouriste() {
        return codeSecouriste;
    }

    public void setCodeSecouriste(String codeSecouriste) {
        this.codeSecouriste = codeSecouriste;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdIncident() {
        return idIncident;
    }

    public String getQuartier() { return quartier; }

    public void setQuartier(String quartier) { this.quartier = quartier; }

    public void setIdIncident(String idIncident) {
        this.idIncident = idIncident;
    }

    public String getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(String idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Date getDateIncidence() {
        return dateIncidence;
    }

    public void setDateIncidence(Date dateIncidence) {
        this.dateIncidence = dateIncidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
