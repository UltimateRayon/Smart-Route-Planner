package com.example.srp.io;

import com.example.srp.algorithms.expansion.RouteExpander.RouteSegment;
import com.example.srp.models.DetailedRoute;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Exports route planning results to various formats
 * Supports: JSON, CSV, DOT (GraphViz)
 */
public class RouteExporter {

    /**
     * Export routes to JSON format
     */
    public void exportToJson(List<DetailedRoute> routes, String outputPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create export data structure
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("timestamp", new Date().toString());
        exportData.put("totalBuses", routes.size());

        List<Map<String, Object>> routesData = new ArrayList<>();
        for (DetailedRoute route : routes) {
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("busId", route.getBusId());
            routeData.put("waypoints", route.getWaypoints());
            routeData.put("fullPath", route.getFullNodeSequence());
            routeData.put("totalDistance", route.getTotalDistance());
            routeData.put("totalNodes", route.getTotalNodes());
            routeData.put("hour", route.getHour());

            List<Map<String, Object>> segmentsData = new ArrayList<>();
            for (RouteSegment segment : route.getSegments()) {
                Map<String, Object> segmentData = new HashMap<>();
                segmentData.put("from", segment.getFromNode());
                segmentData.put("to", segment.getToNode());
                segmentData.put("distance", segment.getDistance());
                segmentData.put("path", segment.getNodeSequence());
                segmentsData.add(segmentData);
            }
            routeData.put("segments", segmentsData);

            routesData.add(routeData);
        }
        exportData.put("routes", routesData);

        // Write to file
        File file = new File(outputPath);
        mapper.writeValue(file, exportData);
        System.out.println("✓ Exported to JSON: " + outputPath);
    }

    /**
     * Export routes to CSV format (one file per bus)
     */
    public void exportToCsv(List<DetailedRoute> routes, String outputDirectory) throws IOException {
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (DetailedRoute route : routes) {
            String filename = outputDirectory + "/bus_" + route.getBusId() + "_route.csv";

            try (FileWriter writer = new FileWriter(filename)) {
                // Write header
                writer.write("Step,From,To,Distance(km),Path\n");

                // Write segments
                int step = 1;
                for (RouteSegment segment : route.getSegments()) {
                    writer.write(step++ + ",");
                    writer.write(segment.getFromNode() + ",");
                    writer.write(segment.getToNode() + ",");
                    writer.write(String.format("%.2f", segment.getDistance()) + ",");
                    writer.write("\"" + String.join(" -> ", segment.getNodeSequence()) + "\"\n");
                }

                // Write summary
                writer.write("\nSummary\n");
                writer.write("Total Distance," + String.format("%.2f", route.getTotalDistance()) + " km\n");
                writer.write("Total Nodes," + route.getTotalNodes() + "\n");
                writer.write("Waypoints,\"" + String.join(",", route.getWaypoints()) + "\"\n");
            }

            System.out.println("✓ Exported to CSV: " + filename);
        }
    }

    /**
     * Export routes to DOT format for GraphViz visualization
     */
    public void exportToDot(List<DetailedRoute> routes, String outputPath) throws IOException {
        StringBuilder dot = new StringBuilder();

        // DOT graph header
        dot.append("digraph RouteVisualization {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n\n");

        // Define colors for each bus
        String[] colors = {"red", "blue", "green", "orange", "purple", "brown"};

        // Add routes
        for (DetailedRoute route : routes) {
            int busId = route.getBusId();
            String color = colors[busId % colors.length];

            dot.append("  // Bus " + busId + " route\n");

            List<String> fullPath = route.getFullNodeSequence();
            for (int i = 0; i < fullPath.size() - 1; i++) {
                String from = fullPath.get(i);
                String to = fullPath.get(i + 1);

                dot.append("  \"" + from + "\" -> \"" + to + "\" ");
                dot.append("[color=" + color + ", penwidth=2, ");
                dot.append("label=\"Bus " + busId + "\"];\n");
            }
            dot.append("\n");
        }

        // Add start node styling
        if (!routes.isEmpty()) {
            String startNode = routes.get(0).getWaypoints().get(0);
            dot.append("  \"" + startNode + "\" [shape=doublecircle, style=filled, fillcolor=lightgreen];\n");
        }

        dot.append("}\n");

        // Write to file
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(dot.toString());
        }

        System.out.println("✓ Exported to DOT: " + outputPath);
        System.out.println("  To visualize: dot -Tpng " + outputPath + " -o routes.png");
    }

    /**
     * Export turn-by-turn directions to text file
     */
    public void exportTurnByTurn(List<DetailedRoute> routes, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("═══════════════════════════════════════════════\n");
            writer.write("     SMART ROUTE PLANNER - ROUTE MANIFEST     \n");
            writer.write("═══════════════════════════════════════════════\n\n");

            writer.write("Generated: " + new Date() + "\n");
            writer.write("Total Buses: " + routes.size() + "\n\n");

            for (DetailedRoute route : routes) {
                writer.write(route.getTurnByTurnDirections());
                writer.write("\n" + "─".repeat(50) + "\n\n");
            }
        }

        System.out.println("✓ Exported turn-by-turn: " + outputPath);
    }

    /**
     * Export all formats at once
     */
    public void exportAll(List<DetailedRoute> routes, String outputDirectory) throws IOException {
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = String.valueOf(System.currentTimeMillis());

        exportToJson(routes, outputDirectory + "/routes_" + timestamp + ".json");
        exportToCsv(routes, outputDirectory + "/csv");
        exportToDot(routes, outputDirectory + "/routes_" + timestamp + ".dot");
        exportTurnByTurn(routes, outputDirectory + "/manifest_" + timestamp + ".txt");

        System.out.println("\n✓ All formats exported to: " + outputDirectory);
    }
}
