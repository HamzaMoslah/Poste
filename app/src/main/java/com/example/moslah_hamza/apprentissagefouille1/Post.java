package com.example.moslah_hamza.apprentissagefouille1;

/**
 * Created by Moslah_Hamza on 28/04/2017.
 */

public class Post {
    private double wait, lon, lat;
    private int id, service, code, cluster;
    private String adress, label;

    public Post(double wait, double lon, double lat, int id, int service, int code, String adress, String label) {
        this.wait = wait;
        this.lon = lon;
        this.lat = lat;
        this.id = id;
        this.service = service;
        this.code = code;
        this.adress = adress;
        this.label = label;
    }

    public Post() {
    }

    public double getWait() {
        return wait;
    }

    public void setWait(double wait) {
        this.wait = wait;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
