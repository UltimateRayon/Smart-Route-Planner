package com.example.srp.integration;

import com.example.srp.algorithms.balancing.RouteEvaluator;
import com.example.srp.algorithms.clustering.ClusterAssigner;
import com.example.srp.algorithms.clustering.GreedyBalancedAssigner;
import com.example.srp.algorithms.pathfinding.DistanceMatrixBuilder;
import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.algorithms.routing.NearestNeighborTSP;
import com.example.srp.algorithms.routing.TSPSolver;
import com.example.srp.algorithms.routing.TwoOptTSP;
import com.example.srp.io.MapParser;
import com.example.srp.models.Graph;
import com.example.srp.models.NodeCluster;
import com.example.srp.models.RouteInfo;
import com.example.srp.models.Vertex;
import com.example.srp.traffic.JsonTrafficStore;
import com.example.srp.traffic.TrafficStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the complete pipeline
 * Tests all phases working together: A -> B -> C -> D -> E -> F
 */
class EndToEndIntegrationTest {

    @Test
    void testCompletePipeline() throws Exception {
        // Phase A: Load map
        MapParser parser = new MapParser();
        Graph graph = parser.parse("map-1");

        assertNotNull(graph);
        assertTrue(graph.getAllVertices().size() > 0);

        // Phase B: Setup traffic
        TrafficStore trafficStore = new JsonTrafficStore(graph);
        assertNotNull(trafficStore);

        int hour = 8;

        // Phase C: Build distance matrix
        Collection<Vertex> allVertices = graph.getAllVertices();
        DistanceMatrixBuilder dmBuilder = new DistanceMatrixBuilder(graph, trafficStore, hour);
        PathCache pathCache = dmBuilder.build(new ArrayList<>(allVertices));

        assertNotNull(pathCache);

        // Phase D: Cluster assignment
        String startNode = "N1";
        List<String> mandatoryNodes = Arrays.asList("N2", "N3", "N4", "N5");
        int numBuses = 2;

        ClusterAssigner assigner = new GreedyBalancedAssigner(pathCache, 0.5);
        List<NodeCluster> clusters = assigner.assignNodes(mandatoryNodes, startNode, numBuses);

        assertNotNull(clusters);
        assertEquals(numBuses, clusters.size());

        // Verify all mandatory nodes are assigned
        int totalAssignedNodes = clusters.stream()
                .mapToInt(c -> c.getAssignedNodes().size())
                .sum();
        assertEquals(mandatoryNodes.size(), totalAssignedNodes);

        // Phase E: TSP routing
        TSPSolver nearestNeighbor = new NearestNeighborTSP(pathCache);
        TSPSolver tspSolver = new TwoOptTSP(pathCache, nearestNeighbor);

        List<RouteInfo> routes = new ArrayList<>();
        for (NodeCluster cluster : clusters) {
            List<String> tour = tspSolver.solveTSP(cluster.getAllNodes(), startNode);

            assertNotNull(tour);
            assertTrue(tour.size() >= 2); // At least start and return
            assertEquals(startNode, tour.get(0)); // Starts at start node
            assertEquals(startNode, tour.get(tour.size() - 1)); // Ends at start node

            RouteEvaluator evaluator = new RouteEvaluator(pathCache);
            RouteInfo routeInfo = evaluator.evaluateRoute(cluster.getBusId(), tour, hour);
            routes.add(routeInfo);
        }

        // Phase F: Evaluate results
        RouteEvaluator evaluator = new RouteEvaluator(pathCache);

        double makespan = evaluator.calculateMakespan(routes);
        double totalDistance = evaluator.calculateTotalDistance(routes);
        double imbalance = evaluator.calculateImbalanceRatio(routes);

        // Basic sanity checks
        assertTrue(makespan > 0, "Makespan should be positive");
        assertTrue(totalDistance > 0, "Total distance should be positive");
        assertTrue(imbalance >= 1.0, "Imbalance should be >= 1.0");

        // Verify each route has positive distance
        for (RouteInfo route : routes) {
            assertTrue(route.getTotalDistance() > 0, "Route distance should be positive");
            assertTrue(route.getNodeCount() > 0, "Route should visit nodes");
        }
    }

    @Test
    void testWithSingleBus() throws Exception {
        MapParser parser = new MapParser();
        Graph graph = parser.parse("map-1");

        TrafficStore trafficStore = new JsonTrafficStore(graph);
        int hour = 8;

        Collection<Vertex> allVertices = graph.getAllVertices();
        DistanceMatrixBuilder dmBuilder = new DistanceMatrixBuilder(graph, trafficStore, hour);
        PathCache pathCache = dmBuilder.build(new ArrayList<>(allVertices));

        String startNode = "N1";
        List<String> mandatoryNodes = Arrays.asList("N2", "N3", "N4", "N5");
        int numBuses = 1; // Single bus

        ClusterAssigner assigner = new GreedyBalancedAssigner(pathCache, 0.5);
        List<NodeCluster> clusters = assigner.assignNodes(mandatoryNodes, startNode, numBuses);

        assertEquals(1, clusters.size());
        assertEquals(mandatoryNodes.size(), clusters.get(0).getAssignedNodes().size());

        // Should be perfectly balanced (only 1 bus)
        TSPSolver tspSolver = new NearestNeighborTSP(pathCache);
        List<String> tour = tspSolver.solveTSP(clusters.get(0).getAllNodes(), startNode);

        RouteEvaluator evaluator = new RouteEvaluator(pathCache);
        RouteInfo route = evaluator.evaluateRoute(0, tour, hour);

        List<RouteInfo> routes = Arrays.asList(route);
        double imbalance = evaluator.calculateImbalanceRatio(routes);

        assertEquals(1.0, imbalance, 0.001); // Perfect balance with 1 bus
    }

    @Test
    void testWithMultipleBuses() throws Exception {
        MapParser parser = new MapParser();
        Graph graph = parser.parse("map-1");

        TrafficStore trafficStore = new JsonTrafficStore(graph);
        int hour = 8;

        Collection<Vertex> allVertices = graph.getAllVertices();
        DistanceMatrixBuilder dmBuilder = new DistanceMatrixBuilder(graph, trafficStore, hour);
        PathCache pathCache = dmBuilder.build(new ArrayList<>(allVertices));

        String startNode = "N1";
        List<String> mandatoryNodes = Arrays.asList("N2", "N3", "N4", "N5");

        // Test with different number of buses
        for (int numBuses = 2; numBuses <= 4; numBuses++) {
            ClusterAssigner assigner = new GreedyBalancedAssigner(pathCache, 0.5);
            List<NodeCluster> clusters = assigner.assignNodes(mandatoryNodes, startNode, numBuses);

            assertEquals(numBuses, clusters.size());

            // Verify all nodes assigned
            int totalNodes = clusters.stream()
                    .mapToInt(c -> c.getAssignedNodes().size())
                    .sum();
            assertEquals(mandatoryNodes.size(), totalNodes);
        }
    }
}