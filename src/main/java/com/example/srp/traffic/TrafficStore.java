package com.example.srp.traffic;

import com.example.srp.models.Edge;

import java.util.List;
import java.util.Map;

public interface TrafficStore {
    double getMultipliers(String edge, int hour);
    Map<String, Double> getMultipliersForHour(int hour);
}
