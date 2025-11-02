package com.example.srp.algorithms;

import com.example.srp.models.Edge;
import com.example.srp.models.Graph;
import com.example.srp.models.Path;
import com.example.srp.traffic.TrafficStore;

import java.util.*;

public class Dijkstra {
    Graph graph;
    TrafficStore ts;

    public Dijkstra(Graph graph, TrafficStore ts) {
        this.graph=graph;
        this.ts=ts;
    }

    double getEffectiveWeight(Edge edge, int hour) {
        double multiplier = ts.getMultipliers(edge.getId(), hour);
        return edge.getDistance()*multiplier;
    }

    public Path findShortestPath(String sourceId, String targetId, int hour) {
        Map<String, Double> distance=new HashMap<>();
        Map<String, String> parent=new HashMap<>();
        for(var vtxId: graph.getVertices()) {
            distance.put(vtxId, Double.POSITIVE_INFINITY);
        }
        PriorityQueue<String> pq=new PriorityQueue<>(Comparator.comparingDouble(distance::get));

        distance.put(sourceId, 0.0);
        pq.add(sourceId);
        while(!pq.isEmpty()) {
            String u=pq.poll();
            if(u==targetId) {break;}

            for(Edge edge: graph.getNeighborEdge(u)) {
                String v=edge.getTo();
                double uw=distance.get(u);
                double vw=distance.get(v);
                double ew=getEffectiveWeight(edge, hour);
                if(uw+ew<vw) {
                    distance.put(v, uw+ew);
                    parent.put(v, u);
                    pq.add(v);
                }
            }
        }
        List<String> path=new ArrayList<>();
        for(String at=targetId; at!=null; at=parent.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return new Path(path, distance.get(targetId));
    }
}
