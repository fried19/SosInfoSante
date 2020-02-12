package com.andsomore.sosinfosante.entite;

import java.util.Date;

public class Intervention {
    private String codeAgent;
    private Date   dateItervention;
    private double longitude;
    private double latitude;
    private String idIncident;
    private String idIntervention;

    public Intervention(String codeAgent, Date dateItervention, String idIncident,String idIntervention,double longitude,double latitude) {
        this.codeAgent = codeAgent;
        this.dateItervention = dateItervention;
        this.idIncident = idIncident;
        this.idIntervention = idIntervention;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Intervention() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getIdIntervention() {
        return idIntervention;
    }

    public void setIdIntervention(String idIntervention) {
        this.idIntervention = idIntervention;
    }

    public String getCodeAgent() {
        return codeAgent;
    }

    public void setCodeAgent(String codeAgent) {
        this.codeAgent = codeAgent;
    }

    public Date getDateItervention() {
        return dateItervention;
    }

    public void setDateItervention(Date dateItervention) {
        this.dateItervention = dateItervention;
    }

    public String getIdIncident() {
        return idIncident;
    }

    public void setIdIncident(String idIncident) {
        this.idIncident = idIncident;
    }
}
