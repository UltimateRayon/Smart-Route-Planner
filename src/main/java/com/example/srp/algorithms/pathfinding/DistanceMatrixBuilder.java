package com.example.srp.algorithms.pathfinding;

import com.example.srp.models.Graph;
import com.example.srp.models.Path;
import com.example.srp.models.Vertex;
import com.example.srp.traffic.TrafficStore;

import java.util.List;

public class DistanceMatrixBuilder {
    private final Graph graph;
    private final TrafficStore store;
    private final int hour;

    public DistanceMatrixBuilder(Graph graph, TrafficStore ts, int hour) {
        this.graph = graph;
        this.store = ts;
        this.hour = hour;
    }

    public PathCache build(List<Vertex> nodes) {
        Dijkstra dstra=new Dijkstra(graph, store);
        PathCache cache=new PathCache();

        for(int i=0; i<nodes.size(); i++) {
            for(int j=i+1; j<nodes.size(); j++) {
                Vertex from=nodes.get(i);
                Vertex to=nodes.get(j);
                Path path=dstra.findShortestPath(from.getId(), to.getId(), hour);
                cache.put(from.getId(), to.getId(), path);
            }
        }
        return cache;
    }
}
