package com.example.srp.app;

import com.example.srp.algorithms.Dijkstra;
import com.example.srp.algorithms.DistanceMatrixBuilder;
import com.example.srp.algorithms.PathCache;
import com.example.srp.io.MapParser;
import com.example.srp.models.Edge;
import com.example.srp.models.Graph;
import com.example.srp.models.Path;
import com.example.srp.models.Vertex;
import com.example.srp.traffic.JsonTrafficStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        Graph graph=new Graph();
        MapParser map=new MapParser();
        System.out.print("Enter map name: ");
        String mapName=scanner.nextLine();
        try {
            graph= map.mapParser(mapName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=== Printing the graph ===");
        for (String node : graph.getVertices()) {
            System.out.println("Node: " + node);
            for (Edge e : graph.getNeighborEdge(node)) {
                System.out.println("  -> " + e.getTo() + ", distance=" + e.getDistance());
            }
        }
        JsonTrafficStore traffic=new JsonTrafficStore(graph);
        System.out.println();
        System.out.println("All Traffic Multipliers for hour 10:");
        System.out.println(traffic.getMultipliersForHour(10));
        System.out.println();
        System.out.println("Shortest path from N1 to N13");
        Dijkstra ds=new Dijkstra(graph, traffic);
        System.out.println(ds.findShortestPath("N1", "N13", 2));
        System.out.println();
        List<String> mandatoryIds = List.of("N1", "N9", "N5", "N17");
        List<Vertex> mandatoryVertices = new ArrayList<>();
        for (String id : mandatoryIds) {
            Vertex v = new Vertex();
            v.setId(id);
            mandatoryVertices.add(v);
        }
        DistanceMatrixBuilder builder = new DistanceMatrixBuilder(graph, traffic, 5);
        PathCache cache = builder.build(mandatoryVertices);

        PrintUtils.printMandatoryDistanceMatrix(mandatoryIds, cache);
    }
}
