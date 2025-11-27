package com.example.srp.algorithms.pathfinding;

import com.example.srp.models.Graph;
import com.example.srp.models.Path;
import com.example.srp.models.Vertex;
import com.example.srp.traffic.TrafficStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Dijkstra's algorithm
 */
class DijkstraTest {

    private Graph graph;
    private TrafficStore trafficStore;
    private Dijkstra dijkstra;

    /**
     * Helper method to create a vertex
     */
    private Vertex createVertex(String id, double x, double y) {
        Vertex v = new Vertex();
        v.setId(id);
        v.setX(x);
        v.setY(y);
        return v;
    }

    /**
     * Helper method to create traffic array (no traffic)
     */
    private double[] createNoTraffic() {
        double[] traffic = new double[24];
        for (int i = 0; i < 24; i++) {
            traffic[i] = 1.0;
        }
        return traffic;
    }

    /**
     * Mock traffic store implementation
     */
    private static class MockTrafficStore implements TrafficStore {
        @Override
        public double getMultipliers(String edge, int hour) {
            return 1.0;
        }

        @Override
        public Map<String, Double> getMultipliersForHour(int hour) {
            return new HashMap<>();
        }
    }

    @BeforeEach
    void setUp() {
        // Initialize graph
        graph = new Graph();

        // Create vertices
        graph.addVertex(createVertex("N1", 0, 0));
        graph.addVertex(createVertex("N2", 100, 0));
        graph.addVertex(createVertex("N3", 200, 0));
        graph.addVertex(createVertex("N4", 0, 100));
        graph.addVertex(createVertex("N5", 200, 100));

        // Create edges with no traffic multiplier
        double[] noTraffic = createNoTraffic();

        // Build simple connected graph:
        // N1 --2-- N2 --3-- N3
        //  |               |
        //  4               2
        //  |               |
        // N4 ------5------ N5

        graph.addEdge("E1", "N1", "N2", 2.0, noTraffic);
        graph.addEdge("E2", "N2", "N3", 3.0, noTraffic);
        graph.addEdge("E3", "N1", "N4", 4.0, noTraffic);
        graph.addEdge("E4", "N3", "N5", 2.0, noTraffic);
        graph.addEdge("E5", "N4", "N5", 5.0, noTraffic);

        // Create traffic store and dijkstra
        trafficStore = new MockTrafficStore();
        dijkstra = new Dijkstra(graph, trafficStore);
    }

    @Test
    void testGraphInitialization() {
        // Verify graph was set up correctly
        assertNotNull(graph);
        assertEquals(5, graph.getAllVertices().size(), "Graph should have 5 vertices");

        // Verify vertices exist by checking their IDs
        var allVertexIds = graph.getAllVertices().stream()
                .map(Vertex::getId)
                .toList();

        assertTrue(allVertexIds.contains("N1"), "Graph should contain vertex N1");
        assertTrue(allVertexIds.contains("N2"), "Graph should contain vertex N2");
        assertTrue(allVertexIds.contains("N3"), "Graph should contain vertex N3");
        assertTrue(allVertexIds.contains("N4"), "Graph should contain vertex N4");
        assertTrue(allVertexIds.contains("N5"), "Graph should contain vertex N5");
    }

    @Test
    void testDirectPath() {
        // Test direct path from N1 to N2 (distance = 2)
        Path path = dijkstra.findShortestPath("N1", "N2", 0);

        assertNotNull(path, "Path should not be null");
        assertFalse(path.getVertices().isEmpty(), "Path should contain vertices");
        assertEquals(2.0, path.getTotalDistance(), 0.001, "Distance should be 2.0");
    }

    @Test
    void testPathContainsStartAndEnd() {
        // Test that path starts at N1 and ends at N2
        Path path = dijkstra.findShortestPath("N1", "N2", 0);

        assertNotNull(path);
        assertTrue(path.getVertices().size() >= 2);
        assertEquals("N1", path.getVertices().get(0), "Path should start at N1");
        assertEquals("N2", path.getVertices().get(path.getVertices().size() - 1), "Path should end at N2");
    }

    @Test
    void testIndirectPath() {
        // Test path from N1 to N3
        // Shortest: N1 -> N2 -> N3 (distance = 2 + 3 = 5)
        Path path = dijkstra.findShortestPath("N1", "N3", 0);

        assertNotNull(path);
        assertEquals(5.0, path.getTotalDistance(), 0.001, "Distance N1->N3 should be 5.0");
    }

    @Test
    void testShortestPathChoice() {
        // Test from N1 to N5
        // Option 1: N1 -> N2 -> N3 -> N5 (distance = 2 + 3 + 2 = 7)
        // Option 2: N1 -> N4 -> N5 (distance = 4 + 5 = 9)
        // Should choose option 1

        Path path = dijkstra.findShortestPath("N1", "N5", 0);

        assertNotNull(path);
        assertEquals(7.0, path.getTotalDistance(), 0.001, "Should choose shortest path (7.0)");
    }

    @Test
    void testSameSourceAndTarget() {
        // Path from node to itself should be 0
        Path path = dijkstra.findShortestPath("N1", "N1", 0);

        assertNotNull(path);
        assertEquals(0.0, path.getTotalDistance(), 0.001, "Distance to self should be 0");
        assertEquals(1, path.getVertices().size(), "Path to self should only contain start node");
        assertEquals("N1", path.getVertices().get(0), "Should start and end at N1");
    }

    @Test
    void testReversePath() {
        // Test that reverse path has same distance (bidirectional)
        Path path1 = dijkstra.findShortestPath("N1", "N2", 0);
        Path path2 = dijkstra.findShortestPath("N2", "N1", 0);

        assertNotNull(path1);
        assertNotNull(path2);
        assertEquals(path1.getTotalDistance(), path2.getTotalDistance(), 0.001,
                "Bidirectional paths should have same distance");
    }

    @Test
    void testWithTraffic() {
        // Create traffic store with heavy traffic on E1
        TrafficStore heavyTrafficStore = new TrafficStore() {
            @Override
            public double getMultipliers(String edge, int hour) {
                return edge.equals("E1") ? 2.0 : 1.0;
            }

            @Override
            public Map<String, Double> getMultipliersForHour(int hour) {
                Map<String, Double> map = new HashMap<>();
                map.put("E1", 2.0);
                return map;
            }
        };

        Dijkstra dijkstraWithTraffic = new Dijkstra(graph, heavyTrafficStore);

        // Path from N1 to N2 with traffic on E1
        // Direct with traffic: 2.0 * 2.0 = 4.0
        Path path = dijkstraWithTraffic.findShortestPath("N1", "N2", 0);

        assertNotNull(path);
        assertEquals(4.0, path.getTotalDistance(), 0.001, "Distance should include traffic multiplier");
    }

    @Test
    void testMultipleHops() {
        // Test longer path: N1 -> N4 -> N5
        Path path = dijkstra.findShortestPath("N1", "N5", 0);

        // Should be N1->N2->N3->N5 (7.0) instead of N1->N4->N5 (9.0)
        assertNotNull(path);
        assertTrue(path.getTotalDistance() > 0, "Distance should be positive");
        assertEquals(7.0, path.getTotalDistance(), 0.001);
    }
}