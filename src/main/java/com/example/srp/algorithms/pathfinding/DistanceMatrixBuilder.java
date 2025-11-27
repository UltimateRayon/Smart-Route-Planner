package com.example.srp.algorithms.pathfinding;

import com.example.srp.models.Graph;
import com.example.srp.models.Path;
import com.example.srp.models.Vertex;
import com.example.srp.traffic.TrafficStore;

import java.util.Collections;
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

        // 1. Initialize self-loops (Distance 0)
        // This prevents "No path found from N1 to N1" errors when a bus
        // has no assigned nodes and stays at the depot.
        for (Vertex v : nodes) {
            Path selfPath = new Path(Collections.singletonList(v.getId()), 0.0);
            cache.put(v.getId(), v.getId(), selfPath);
        }

        // 2. Calculate all pairs
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