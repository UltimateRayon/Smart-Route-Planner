package com.example.srp.app;

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
import com.example.srp.traffic.JsonTrafficStore;
import com.example.srp.traffic.TrafficStore;

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

            // Phase E: TSP routing for each cluster
            System.out.println("=== Phase E: TSP Route Optimization ===");

            // Create TSP solvers
            TSPSolver nearestNeighbor = new NearestNeighborTSP(pathCache);
            TSPSolver twoOpt = new TwoOptTSP(pathCache, nearestNeighbor);

            for (NodeCluster cluster : clusters) {
                System.out.println("Bus " + cluster.getBusId() + ":");

                List<String> nodes = cluster.getAllNodes();
                System.out.println("  Nodes to visit: " + nodes);

                // Solve with Nearest Neighbor
                List<String> nnTour = nearestNeighbor.solveTSP(nodes, startNode);
                double nnDistance = nearestNeighbor.calculateTourDistance(nnTour);
                System.out.println("  Nearest Neighbor tour: " + nnTour);
                System.out.println("  NN Distance: " + String.format("%.2f", nnDistance));

                // Improve with 2-Opt
                List<String> optimizedTour = twoOpt.solveTSP(nodes, startNode);
                double optimizedDistance = twoOpt.calculateTourDistance(optimizedTour);
                System.out.println("  2-Opt optimized tour: " + optimizedTour);
                System.out.println("  Optimized Distance: " + String.format("%.2f", optimizedDistance));

                double improvement = ((nnDistance - optimizedDistance) / nnDistance) * 100;
                System.out.println("  Improvement: " + String.format("%.2f%%", improvement));
                System.out.println();
            }

            // Summary
            System.out.println("=== Summary ===");
            double totalDistance = 0.0;
            double maxDistance = 0.0;

            for (NodeCluster cluster : clusters) {
                List<String> tour = twoOpt.solveTSP(cluster.getAllNodes(), startNode);
                double distance = twoOpt.calculateTourDistance(tour);
                totalDistance += distance;
                maxDistance = Math.max(maxDistance, distance);

                System.out.println("Bus " + cluster.getBusId() +
                        " final distance: " + String.format("%.2f", distance));
            }

            System.out.println("Total distance (all buses): " + String.format("%.2f", totalDistance));
            System.out.println("Makespan (longest route): " + String.format("%.2f", maxDistance));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
