package com.example.srp.io;

import com.example.srp.models.Edge;
import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MapParser {
    private final ObjectMapper mapper;

    public MapParser() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Parses a map JSON file from resources/maps/ directory
     * @param mapName the name of the map file (without .json extension)
     * @return parsed Graph object
     * @throws IOException if file not found or parsing fails
     */
    public Graph parse(String mapName) throws IOException {
        String fileName = "/maps/" + mapName + ".json";

        try (InputStream is = MapParser.class.getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Map file not found: " + fileName);
            }

            JsonNode root = mapper.readTree(is);
            Graph graph = new Graph();

            // Parse vertices first
            if (root.has("vertices")) {
                List<Vertex> vertices = mapper.convertValue(
                        root.get("vertices"),
                        new TypeReference<List<Vertex>>() {}
                );
                for (Vertex v : vertices) {
                    validateVertex(v);
                    graph.addVertex(v);
                }
            } else {
                throw new IOException("Map JSON missing 'vertices' field");
            }

            // Parse edges after
            if (root.has("edges")) {
                List<Edge> edges = mapper.convertValue(
                        root.get("edges"),
                        new TypeReference<List<Edge>>() {}
                );
                for (Edge e : edges) {
                    validateEdge(e);
                    graph.addEdge(e.getId(), e.getFrom(), e.getTo(), e.getDistance(), e.getTraffic());
                }
            } else {
                throw new IOException("Map JSON missing 'edges' field");
            }

            return graph;
        }
    }

    // Validates vertex data
    private void validateVertex(Vertex v) throws IOException {
        if (v.getId() == null || v.getId().trim().isEmpty()) {
            throw new IOException("Vertex missing valid ID");
        }
    }

    // Validates edge data
    private void validateEdge(Edge e) throws IOException {
        if (e.getId() == null || e.getId().trim().isEmpty()) {
            throw new IOException("Edge missing valid ID");
        }
        if (e.getFrom() == null || e.getTo() == null) {
            throw new IOException("Edge " + e.getId() + " missing from/to vertices");
        }
        if (e.getDistance() <= 0) {
            throw new IOException("Edge " + e.getId() + " has invalid distance: " + e.getDistance());
        }
        if (e.getTraffic() == null || e.getTraffic().length != 24) {
            throw new IOException("Edge " + e.getId() + " traffic array must have exactly 24 elements");
        }
        // Validate traffic multipliers are positive
        for (int i = 0; i < 24; i++) {
            if (e.getTraffic()[i] <= 0) {
                throw new IOException("Edge " + e.getId() + " has invalid traffic multiplier at hour " + i);
            }
        }
    }
}