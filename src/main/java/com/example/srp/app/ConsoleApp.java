package com.example.srp.app;

import com.example.srp.algorithms.balancing.RouteEvaluator;
import com.example.srp.algorithms.clustering.ClusterAssigner;
import com.example.srp.algorithms.clustering.GreedyBalancedAssigner;
import com.example.srp.algorithms.expansion.RouteExpander;
import com.example.srp.algorithms.pathfinding.DistanceMatrixBuilder;
import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.algorithms.routing.NearestNeighborTSP;
import com.example.srp.algorithms.routing.TSPSolver;
import com.example.srp.algorithms.routing.TwoOptTSP;
import com.example.srp.io.MapParser;
import com.example.srp.io.RouteExporter;
import com.example.srp.models.*;
import com.example.srp.traffic.JsonTrafficStore;
import com.example.srp.traffic.TrafficStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Example demonstrating Phase G: Route Expansion & Finalization
 * Shows how to expand routes and export in multiple formats
 */
public class ConsoleApp {
    public static void main(String[] args) {
        try {
            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║   Smart Route Planner - Phase G Demo          ║");
            System.out.println("║   Route Expansion & Export                     ║");
            System.out.println("╚════════════════════════════════════════════════╝\n");

            // Phases A-F (abbreviated - run full pipeline)
            System.out.println("Running Phases A-F...\n");

            MapParser parser = new MapParser();
            Graph graph = parser.parse("map-1");

            TrafficStore trafficStore = new JsonTrafficStore(graph);
            int currentHour = 8;

            Collection<Vertex> allVertices = graph.getAllVertices();
            DistanceMatrixBuilder dmBuilder = new DistanceMatrixBuilder(graph, trafficStore, currentHour);
            PathCache pathCache = dmBuilder.build(new ArrayList<>(allVertices));

            String startNode = "N1";
            List<String> mandatoryNodes = Arrays.asList("N2", "N3", "N4", "N5");
            int numBuses = 2;

            ClusterAssigner assigner = new GreedyBalancedAssigner(pathCache, 0.5);
            List<NodeCluster> clusters = assigner.assignNodes(mandatoryNodes, startNode, numBuses);

            TSPSolver nearestNeighbor = new NearestNeighborTSP(pathCache);
            TSPSolver tspSolver = new TwoOptTSP(pathCache, nearestNeighbor);

            RouteEvaluator evaluator = new RouteEvaluator(pathCache);
            List<RouteInfo> routes = new ArrayList<>();
            for (NodeCluster cluster : clusters) {
                List<String> tour = tspSolver.solveTSP(cluster.getAllNodes(), startNode);
                RouteInfo routeInfo = evaluator.evaluateRoute(cluster.getBusId(), tour, currentHour);
                routes.add(routeInfo);
            }

            System.out.println("✓ Phases A-F completed\n");

            // Phase G: Route Expansion
            System.out.println("=== Phase G: Route Expansion ===\n");

            RouteExpander expander = new RouteExpander(pathCache);
            List<DetailedRoute> detailedRoutes = expander.expandRoutes(routes);

            System.out.println("✓ Routes expanded to detailed format\n");

            // Display expanded routes
            System.out.println("📍 Detailed Routes:\n");
            for (DetailedRoute route : detailedRoutes) {
                System.out.println("Bus " + route.getBusId() + ":");
                System.out.println("  Waypoints: " + route.getWaypoints());
                System.out.println("  Full path: " + route.getFullNodeSequence());
                System.out.println("  Total nodes: " + route.getTotalNodes());
                System.out.println("  Distance: " + String.format("%.2f km", route.getTotalDistance()));
                System.out.println("\n  Segments:");

                int step = 1;
                for (var segment : route.getSegments()) {
                    System.out.println("    " + step++ + ". " + segment);
                }
                System.out.println();
            }

            // Export to multiple formats
            System.out.println("=== Exporting Results ===\n");

            RouteExporter exporter = new RouteExporter();
            String outputDir = "output";

            try {
                // Export all formats
                exporter.exportAll(detailedRoutes, outputDir);

                // Also show turn-by-turn for one route
                System.out.println("\n=== Sample Turn-by-Turn Directions ===\n");
                if (!detailedRoutes.isEmpty()) {
                    System.out.println(detailedRoutes.get(0).getTurnByTurnDirections());
                }

            } catch (Exception e) {
                System.err.println("⚠️  Warning: Export failed - " + e.getMessage());
                System.err.println("   (This is normal if output directory is not writable)");
            }

            // Summary
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║            Phase G Complete! ✓                 ║");
            System.out.println("╚════════════════════════════════════════════════╝");

            System.out.println("\nGenerated outputs:");
            System.out.println("  • JSON: Complete route data");
            System.out.println("  • CSV: Per-bus route tables");
            System.out.println("  • DOT: GraphViz visualization");
            System.out.println("  • TXT: Turn-by-turn manifest");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}