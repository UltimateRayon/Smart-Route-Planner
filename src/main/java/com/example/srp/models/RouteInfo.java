package com.example.srp.models;

import java.util.List;

public class RouteInfo {
    int busId;
    List<String> tour;
    double totalDistance;
    int hour;

    public RouteInfo(int busId, List<String> tour, double totalDistance, int hour) {
        this.busId=busId;
        this.tour=tour;
        this.totalDistance=totalDistance;
        this.hour=hour;
    }

    public int getBusId() {
        return busId;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public int getNodeCount() {
        return tour.size()-1;
    }

    public List<String> getTour() {
        return tour;
    }
}
