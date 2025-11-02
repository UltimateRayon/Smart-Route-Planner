package com.example.srp.traffic;

import com.example.srp.models.Edge;
import com.example.srp.models.Graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTrafficStore implements TrafficStore {
    Map<String, double[]> trafficData;

    public JsonTrafficStore(Graph graph) {
        this.trafficData=new HashMap<>();
        for(List<Edge> edges: graph.adjList.values()) {
            for(Edge edge: edges) {
                trafficData.put(edge.getId(), edge.getTraffic());
            }
        }
    }

    @Override
    public double getMultipliers(String edge, int hour) {
        return trafficData.get(edge)[hour];
    }

    @Override
    public Map<String, Double> getMultipliersForHour(int hour) {
        Map<String, Double> snapshot=new HashMap<>();
        for(var entry: trafficData.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue()[hour]);
        }
        return snapshot;
    }
}
