package com.example.srp.models;

import java.util.ArrayList;

public class Edge {
    String id;
    String from;
    String to;
    double distance;
    double[] traffic=new double[24];

    Edge() {}
    Edge(String id, String from, String to, double distance, double[] traffic) {
        this.id=id;
        this.from=from;
        this.to=to;
        this.distance=distance;
        this.traffic=traffic;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setTraffic(double[] traffic) {
        this.traffic = traffic;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getDistance() {
        return distance;
    }

    public double[] getTraffic() {
        return traffic;
    }

    @Override
    public String toString() {
        return "Edge{id='" + id + "', from='" + from + "', to='" + to + "', distance=" + distance + "}";
    }
}

