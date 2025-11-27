package com.example.srp.io;

import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;
import java.io.IOException;
import java.util.*;

public class GraphConnectivityChecker {
    public static void main(String[] args) throws IOException {
        MapParser parser = new MapParser();
        Graph graph = parser.parse("map-1");

        System.out.println("Total vertices: " + graph.getAllVertices().size());
        System.out.println("Total edges: " + countEdges(graph));

        // Check connectivity from start node
        String startNode = "N1";
        Set<String> reachable = findReachableNodes(graph, startNode);

        System.out.println("\nReachable from " + startNode + ": " + reachable.size());
        System.out.println("Reachable nodes: " + reachable);

        if (reachable.size() == graph.getAllVertices().size()) {
            System.out.println("✓ Graph is fully connected!");
        } else {
            System.out.println("✗ Graph is NOT fully connected!");
            Set<String> unreachable = new HashSet<>();
            for (Vertex v : graph.getAllVertices()) {
                unreachable.add(v.getId());
            }
            unreachable.removeAll(reachable);
            System.out.println("Unreachable nodes: " + unreachable);
        }
    }

    private static int countEdges(Graph graph) {
        Set<String> edges = new HashSet<>();
        for (Vertex v : graph.getAllVertices()) {
            for (var e : graph.getNeighborEdge(v.getId())) {
                String key = e.getFrom().compareTo(e.getTo()) < 0
                        ? e.getFrom() + "-" + e.getTo()
                        : e.getTo() + "-" + e.getFrom();
                edges.add(key);
            }
        }
        return edges.size();
    }

    private static Set<String> findReachableNodes(Graph graph, String startNode) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            String node = queue.poll();
            for (var edge : graph.getNeighborEdge(node)) {
                String neighbor = edge.getTo();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }
}