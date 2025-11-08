package com.example.srp.app;

import com.example.srp.algorithms.balancing.LoadBalancer;
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
import com.example.srp.models.Vertex;
import com.example.srp.models.RouteInfo;
import com.example.srp.traffic.JsonTrafficStore;
import com.example.srp.traffic.TrafficStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Example demonstrating Phases A through E
 * Complete flow: Load map → Build distances → Cluster nodes → Optimize routes
 */
public class ConsoleApp {
    public static void main(String[] args) {
        try {
            // Phase A: Load map
            System.out.println("=== Phase A: Loading Map ===");
            MapParser parser = new MapParser();
            Graph graph = parser.parse("map-1");
            System.out.println("Loaded graph with " + graph.getVertices().size() + " vertices\n");

            // Phase B: Setup traffic
            System.out.println("=== Phase B: Traffic Setup ===");
            TrafficStore trafficStore = new JsonTrafficStore(graph);
            int currentHour = 8; // 8 AM - rush hour
            System.out.println("Traffic data loaded for hour: " + currentHour + "\n");

            // Phase C: Build distance matrix
            System.out.println("=== Phase C: Building Distance Matrix ===");
            Collection<Vertex> allVertices = graph.getVertices();
            DistanceMatrixBuilder dmBuilder = new DistanceMatrixBuilder(graph, trafficStore, currentHour);
            PathCache pathCache = dmBuilder.build(new java.util.ArrayList<>(allVertices));
            System.out.println("Distance matrix built\n");

            // Phase D: Cluster assignment
            System.out.println("=== Phase D: Node Clustering ===");
            String startNode = "N1";
            List<String> mandatoryNodes = Arrays.asList("N2", "N16", "N18", "N12", "N6", "N7", "N3", "N20", "N10", "N4", "N15", "N13");
            int numBuses = 3;

            ClusterAssigner assigner = new GreedyBalancedAssigner(pathCache, 0.5);
            List<NodeCluster> clusters = assigner.assignNodes(startNode, mandatoryNodes, numBuses);

            System.out.println("Clusters created:");
            for (NodeCluster cluster : clusters) {
                System.out.println("  " + cluster);
            }
            System.out.println();

            // Phase E: TSP routing
            System.out.println("=== Phase E: TSP Route Optimization ===");
            TSPSolver nearestNeighbor = new NearestNeighborTSP(pathCache);
            TSPSolver tspSolver = new TwoOptTSP(pathCache, nearestNeighbor);

            List<List<String>> optimizedTours = new ArrayList<>();
            for (NodeCluster cluster : clusters) {
                List<String> tour = tspSolver.solveTSP(cluster.getAllNodes(), startNode);
                optimizedTours.add(tour);
                System.out.println("  Bus " + cluster.getBusId() + " route: " + tour);
            }
            System.out.println();

            // Phase F: Distance computation and balance check
            System.out.println("=== Phase F: Distance Computation & Balance Check ===");
            RouteEvaluator evaluator = new RouteEvaluator(pathCache);

            // Evaluate all routes
            List<RouteInfo> routes = new ArrayList<>();
            for (int i = 0; i < clusters.size(); i++) {
                NodeCluster cluster = clusters.get(i);
                List<String> tour = optimizedTours.get(i);
                RouteInfo routeInfo = evaluator.evaluateRoute(cluster.getBusId(), tour, currentHour);
                routes.add(routeInfo);
            }

            // Display results
            System.out.println("\n📊 Route Evaluation Results:\n");
            for (RouteInfo route : routes) {
                System.out.println("🚌 Bus " + route.getBusId() + ":");
                System.out.println("   Route: " + route.getTour());
                System.out.println("   Distance: " + String.format("%.2f km", route.getTotalDistance()));
                System.out.println("   Nodes visited: " + route.getNodeCount());
                System.out.println();
            }

            // Calculate metrics
            double makespan = evaluator.calculateMakespan(routes);
            double totalDistance = evaluator.calculateTotalDistance(routes);
            double imbalance = evaluator.calculateImbalanceRatio(routes);

            System.out.println("📈 Overall Metrics:");
            System.out.println("   Makespan (longest route): " + String.format("%.2f km", makespan));
            System.out.println("   Total distance (all buses): " + String.format("%.2f km", totalDistance));
            System.out.println("   Load imbalance ratio: " + String.format("%.2fx", imbalance));

            // Check if balanced
            double balanceThreshold = 1.3; // 30% imbalance acceptable
            boolean isBalanced = imbalance <= balanceThreshold;
            System.out.println("   Status: " + (isBalanced ? "✓ BALANCED" : "✗ IMBALANCED"));
            System.out.println();

            // Optional: Try rebalancing if needed
            if (!isBalanced) {
                System.out.println("=== Attempting Rebalancing ===");
                LoadBalancer balancer = new LoadBalancer(pathCache, assigner, tspSolver,
                        evaluator, balanceThreshold);

                List<RouteInfo> rebalancedRoutes = balancer.rebalance(clusters, startNode,
                        currentHour, 5);

                System.out.println("\n📊 After Rebalancing:\n");
                for (RouteInfo route : rebalancedRoutes) {
                    System.out.println("🚌 Bus " + route.getBusId() + ":");
                    System.out.println("   Route: " + route.getTour());
                    System.out.println("   Distance: " + String.format("%.2f km", route.getTotalDistance()));
                    System.out.println("   Nodes visited: " + route.getNodeCount());
                    System.out.println();
                }
                String report = balancer.generateBalanceReport(rebalancedRoutes);
                System.out.println(report);
            }

            // Summary
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║              Process Complete! ✓               ║");
            System.out.println("╚════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
