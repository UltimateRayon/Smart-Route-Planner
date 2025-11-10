package com.example.srp.algorithms.clustering;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.NodeCluster;
import com.example.srp.models.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyBalancedAssigner implements ClusterAssigner {
    PathCache pathCache;
    /*
    * Turn left (0.0) → Optimize for shortest total distance
    * Turn right (1.0) → Optimize for equal workload
    * Middle (0.5) → Balance both goals
    */
    double totalPenaltyWeight;
    public GreedyBalancedAssigner(PathCache cache, double penalty) {
        pathCache=cache;
        totalPenaltyWeight=penalty;
    }
    public GreedyBalancedAssigner(PathCache cache) {
        this(cache, 0.5); // by default value
    }

    @Override
    public List<NodeCluster> assignNodes(List<String> mandatoryNodes, String startNode, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Number of buses must be positive");
        }
        if (mandatoryNodes.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one mandatory node");
        }
        if (k > mandatoryNodes.size()) {
            throw new IllegalArgumentException("Number of buses cannot exceed number of mandatory nodes");
        }

        // Initialize k clusters, each starting at startNode
        List<NodeCluster> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new NodeCluster(i, startNode));
        }

        List<NodeWithDistance> sortedNodes=new ArrayList<>();
        for(String node: mandatoryNodes) {
            Path path=pathCache.get(startNode, node);
            if (path == null) {
                throw new IllegalStateException("No path found from " + startNode + " to " + node);
            }
            sortedNodes.add(new NodeWithDistance(node, path.getTotalDistance()));
        }
        sortedNodes.sort(Comparator.comparingDouble(n -> n.distance));

        for(NodeWithDistance nd: sortedNodes) {
            String node=nd.nodeId;
            int bestCluster=-1;
            double minCost=Double.POSITIVE_INFINITY;
            for(int i=0; i<clusters.size(); i++) {
                double cost=calculateAssignmentCost(clusters.get(i), node, clusters);
                if(cost<minCost) {
                    minCost=cost;
                    bestCluster=i;
                }
            }
            // Assign the node to best selected cluster
            clusters.get(bestCluster).addNode(node);
        }
        updateClusterDistance(clusters);
        return clusters;
    }

    double calculateAssignmentCost(NodeCluster cluster, String nodeId, List<NodeCluster> allClusters) {
        double distanceCost=calculateDistanceCost(cluster, nodeId);
        double balanceCost=calculateBalanceCost(cluster, allClusters);
        return distanceCost+balanceCost*totalPenaltyWeight;
    }

    double calculateDistanceCost(NodeCluster cluster, String nodeId) {
        List<String> existingNodes=cluster.getAllNodes();
        if(existingNodes.isEmpty()) {
            return 0.0;
        }

        double totalDist = 0.0;
        int count = 0;

        for(String eNode: existingNodes) {
            Path path=pathCache.get(eNode, nodeId);
            if (path != null) {
                totalDist += path.getTotalDistance();
                count++;
            }
        }
        return (count>0)? totalDist/count : 0.0;
    }

    double calculateBalanceCost(NodeCluster cluster, List<NodeCluster> allCluster) {
        int maxSize=0;
        for(NodeCluster cl: allCluster) {
            maxSize=Math.max(maxSize, cl.getNodeCount());
        }
        return (maxSize>0)? (double)cluster.getNodeCount()/maxSize*1000 : 0.0;
    }

    void updateClusterDistance(List<NodeCluster> clusters) {
        for(NodeCluster cluster: clusters) {
            double totalDist=0.0;
            for(String node: cluster.getAssignedNodes()) {
                Path path=pathCache.get(cluster.getStartNode(), node);
                if (path != null) {
                    totalDist += path.getTotalDistance();
                }
            }
            cluster.setTotalDistance(totalDist);
        }
    }

    public static class NodeWithDistance {
        String nodeId;
        double distance;
        NodeWithDistance(String node, double dist) {
            nodeId=node;
            distance=dist;
        }
    }
}
