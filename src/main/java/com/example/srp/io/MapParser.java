package com.example.srp.io;

import com.example.srp.models.Edge;
import com.example.srp.models.Graph;
import com.example.srp.models.Vertex;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public class MapParser {
    public Graph mapParser(String mapName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Graph graph = new Graph();
        String fileName = "/maps/" + mapName + ".json";
        try (InputStream is = MapParser.class.getResourceAsStream(fileName)) {
            if (is == null) {
                throw new Exception("Resource not found: " + mapName);
            }
            JsonNode root = mapper.readTree(is);

            List<Vertex> vertices = mapper.convertValue(root.get("vertices"), new TypeReference<List<Vertex>>() {
            });
            List<Edge> edges = mapper.convertValue(root.get("edges"), new TypeReference<List<Edge>>() {
            });

            // Add edges
            for (Edge e : edges) {
                graph.addEdge(e.getId(), e.getFrom(), e.getTo(), e.getDistance(), e.getTraffic());
            }
            return graph;
        }
    }
}