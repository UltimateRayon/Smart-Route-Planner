package com.example.srp.algorithms.balancing;

import com.example.srp.algorithms.clustering.ClusterAssigner;
import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.algorithms.routing.TSPSolver;
import com.example.srp.models.NodeCluster;
import com.example.srp.models.RouteInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadBalancer {
    PathCache pathCache;
    ClusterAssigner clusterAssigner;
    TSPSolver tspSolver;
    RouteEvaluator evaluator;
    double imbalanceThreshold;

    public LoadBalancer(PathCache pathCache, ClusterAssigner clusterAssigner, TSPSolver tspSolver, RouteEvaluator evaluator, double imbalanceThreshold) {
        this.pathCache=pathCache;
        this.clusterAssigner=clusterAssigner;
        this.tspSolver=tspSolver;
        this.evaluator=evaluator;
        this.imbalanceThreshold=imbalanceThreshold;
    }

    public LoadBalancer(PathCache pathCache, ClusterAssigner clusterAssigner, TSPSolver tspSolver, RouteEvaluator evaluator) {
        this(pathCache, clusterAssigner, tspSolver, evaluator, 1.3);
    }

    boolean isBalanced(List<RouteInfo> routes) {
        double imbalance= evaluator.calculateImbalanceRatio(routes);
        return imbalance<=imbalanceThreshold;
    }

    public List<RouteInfo> rebalance(List<NodeCluster> clusters, String startNode, int hour, int maxIteration) {
        List<RouteInfo> currentRoutes=evaluateCurrentRoutes(clusters, startNode, hour);
        double currentImbalance= evaluator.calculateImbalanceRatio(currentRoutes);

        System.out.println("Initial imbalance: " + String.format("%.2f", currentImbalance));

        if (currentImbalance <= imbalanceThreshold) {
            System.out.println("Routes already balanced!");
            return currentRoutes;
        }

        List<RouteInfo> bestRoutes=currentRoutes;
        double bestImbalance=currentImbalance;
        for(int it=0; it<maxIteration; it++) {
            List<RouteInfo> newRoutes = attemptRebalance(clusters, startNode, hour);
            double newImbalance = evaluator.calculateImbalanceRatio(newRoutes);

            System.out.println("Iteration " + (it + 1) +
                    " imbalance: " + String.format("%.2f", newImbalance));

            if(newImbalance<bestImbalance) {
                bestImbalance=newImbalance;
                bestRoutes=newRoutes;

                if(bestImbalance<=imbalanceThreshold) {
                    System.out.println("Achieved balanced routes!");
                    break;
                }
            } else {
                break;
            }
        }
        return bestRoutes;
    }

    private List<RouteInfo> attemptRebalance(List<NodeCluster> clusters, String startNode, int hour) {
        RouteInfo shortest=null;
        RouteInfo longest=null;
        Map<Integer, RouteInfo> routeMap=new HashMap<>();

        for(NodeCluster cluster: clusters) {
            List<String> tour=tspSolver.solveTSP(cluster.getAllNodes(), startNode);
            RouteInfo current=evaluator.evaluateRoute(cluster.getBusId(), tour, hour);
            routeMap.put(cluster.getBusId(), current);

            if(longest==null || current.getTotalDistance()>longest.getTotalDistance()) {
                longest=current;
            }
            if(shortest==null || current.getTotalDistance()<longest.getTotalDistance()) {
                shortest=current;
            }
        }
        if(longest==null || longest.getNodeCount()==1) {
            return new ArrayList<>(routeMap.values());
        }

        NodeCluster longestCluster=clusters.get(longest.getBusId());
        NodeCluster shortestCluster=clusters.get(shortest.getBusId());

        String nodeToMove=findNodeToMove(longestCluster, startNode);
        if(nodeToMove!=null) {
            longestCluster.getAssignedNodes().remove(nodeToMove);
            shortestCluster.addNode(nodeToMove);
        }
        return evaluateCurrentRoutes(clusters, startNode, hour);
    }

    private String findNodeToMove(NodeCluster cluster, String startNode) {
        List<String> nodes=cluster.getAssignedNodes();

        for(String node: nodes) {
            if(node!=startNode) {
                return node;
            }
        }
        return null;
    }

    private List<RouteInfo> evaluateCurrentRoutes(List<NodeCluster> clusters, String startNode, int hour) {
        List<RouteInfo> routes=new ArrayList<>();

        for(NodeCluster cluster: clusters) {
            List<String> tour=tspSolver.solveTSP(cluster.getAllNodes(), startNode);
            RouteInfo routeInfo=evaluator.evaluateRoute(cluster.getBusId(), tour, hour);
            routes.add(routeInfo);
        }
        return routes;
    }

    /**
     * Generate a balance report
     */
    public String generateBalanceReport(List<RouteInfo> routes) {
        StringBuilder report = new StringBuilder();
        report.append("=== Load Balance Report ===\n");

        double makespan = evaluator.calculateMakespan(routes);
        double totalDistance = evaluator.calculateTotalDistance(routes);
        double imbalance = evaluator.calculateImbalanceRatio(routes);

        report.append(String.format("Makespan (longest route): %.2f km\n", makespan));
        report.append(String.format("Total distance: %.2f km\n", totalDistance));
        report.append(String.format("Imbalance ratio: %.2f\n", imbalance));
        report.append(String.format("Status: %s\n",
                imbalance <= imbalanceThreshold ? "BALANCED ✓" : "IMBALANCED ✗"));
        report.append("\nPer-bus breakdown:\n");

        for (RouteInfo route : routes) {
            report.append(String.format("  Bus %d: %.2f km, %d nodes\n",
                    route.getBusId(),
                    route.getTotalDistance(),
                    route.getNodeCount()));
        }

        return report.toString();
    }
}
