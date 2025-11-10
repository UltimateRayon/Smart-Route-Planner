package com.example.srp.algorithms.pathfinding;

import com.example.srp.models.Edge;
import com.example.srp.models.Graph;
import com.example.srp.models.Path;
import com.example.srp.traffic.TrafficStore;

import java.util.*;

public class Dijkstra {
    private final Graph graph;
    private final TrafficStore ts;

    public Dijkstra(Graph graph, TrafficStore ts) {
        this.graph = graph;
        this.ts = ts;
    }

    double getEffectiveWeight(Edge edge, int hour) {
        double multiplier = ts.getMultipliers(edge.getId(), hour);
        return edge.getDistance() * multiplier;
    }

    public Path findShortestPath(String sourceId, String targetId, int hour) {
        // Initialize distance map
        Map<String, Double> distance = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (var vtxId : graph.getAllVertices()) {
            distance.put(vtxId.getId(), Double.POSITIVE_INFINITY);
        }

        // Priority queue with NodeDist objects
        PriorityQueue<NodeDist> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.dist));

        distance.put(sourceId, 0.0);
        pq.offer(new NodeDist(sourceId, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            String u = current.node;

            // Skip if already visited (handles stale entries in PQ)
            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);

            // Early termination if we reached target
            if (u.equals(targetId)) {
                break;
            }

            // Explore neighbors
            for (Edge edge : graph.getNeighborEdge(u)) {
                String v = edge.getTo();

                // Skip if already visited
                if (visited.contains(v)) {
                    continue;
                }

                double uw = distance.get(u);
                double vw = distance.get(v);
                double ew = getEffectiveWeight(edge, hour);

                // Relaxation step
                if (uw + ew < vw) {
                    distance.put(v, uw + ew);
                    parent.put(v, u);
                    pq.offer(new NodeDist(v, uw + ew));
                }
            }
        }

        // Check if target is reachable
        if (!distance.get(targetId).equals(Double.POSITIVE_INFINITY)) {
            // Reconstruct path
            List<String> path = new ArrayList<>();
            for (String at = targetId; at != null; at = parent.get(at)) {
                path.add(at);
            }
            Collections.reverse(path);
            return new Path(path, distance.get(targetId));
        } else {
            // Target unreachable
            return new Path(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }
    }

    // Helper class to store node with its distance
    private static class NodeDist {
        String node;
        double dist;

        NodeDist(String node, double dist) {
            this.node = node;
            this.dist = dist;
        }
    }
}