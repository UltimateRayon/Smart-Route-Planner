package com.example.srp.io;

import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;
import com.example.srp.models.Edge;
import java.io.IOException;
import java.util.*;

public class GraphDebugChecker {
    public static void main(String[] args) throws IOException {
        MapParser parser = new MapParser();
        Graph graph = parser.parse("map-1");

        System.out.println("=== GRAPH DEBUG INFO ===\n");

        // Check vertices
        System.out.println("VERTICES (" + graph.getAllVertices().size() + "):");
        for (Vertex v : graph.getAllVertices()) {
            System.out.println("  " + v.getId() + " at (" + v.getX() + ", " + v.getY() + ")");
        }

        System.out.println("\n=== EDGES FROM EACH VERTEX ===");

        int totalEdgeObjects = 0;
        Set<String> uniqueEdges = new HashSet<>();

        for (Vertex v : graph.getAllVertices()) {
            List<Edge> neighbors = graph.getNeighborEdge(v.getId());
            System.out.println("\nVertex " + v.getId() + " has " + neighbors.size() + " neighbor edges:");

            for (Edge e : neighbors) {
                totalEdgeObjects++;
                System.out.println("  " + e.getFrom() + " → " + e.getTo() +
                        " (distance: " + e.getDistance() + ")");

                String key = e.getFrom().compareTo(e.getTo()) < 0
                        ? e.getFrom() + "-" + e.getTo()
                        : e.getTo() + "-" + e.getFrom();
                uniqueEdges.add(key);
            }
        }

        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total edge objects: " + totalEdgeObjects);
        System.out.println("Unique edges: " + uniqueEdges.size());
        System.out.println("Expected unique edges: 30");

        if (uniqueEdges.size() < 30) {
            System.out.println("\n⚠️ PROBLEM: Some edges are missing!");
            System.out.println("Check your map JSON file.");
        } else if (uniqueEdges.size() == 30) {
            System.out.println("\n✓ All edges loaded correctly!");
        }

        // Print all unique edges
        System.out.println("\nAll unique edges:");
        uniqueEdges.stream().sorted().forEach(e -> System.out.println("  " + e));
    }
}