package com.example.srp.models;

import java.util.ArrayList;
import java.util.List;

public class NodeCluster {
    int busId;
    String startNode;
    List<String> assignedNodes;
    double totalDistance;

    public NodeCluster(int busId, String startNode) {
        this.busId=busId;
        this.startNode=startNode;
        this.assignedNodes=new ArrayList<>();
        this.totalDistance=0.0;
    }
    public void addNode(String node) {
        if(!assignedNodes.contains(node)) {
            assignedNodes.add(node);
        }
    }

    public List<String> getAllNodes() {
        List<String> nodeList=new ArrayList<>();
        nodeList.add(startNode);
        nodeList.addAll(assignedNodes);
        return nodeList;
    }

    public int getBusId() {
        return busId;
    }

    public String getStartNode() {
        return startNode;
    }

    public List<String> getAssignedNodes() {
        return assignedNodes;
    }

    public double getTotalDistance() {
        return totalDistance;
    }
    public int getNodeCount() {
        return assignedNodes.size();
    }

    public void setTotalDistance(double totalDist) {
        totalDistance=totalDist;
    }

    @Override
    public String toString() {
        return "NodeCluster{" +
                "busId=" + busId +
                ", startNode='" + startNode + '\'' +
                ", assignedNodes=" + assignedNodes +
                ", nodeCount=" + getNodeCount() +
                ", totalDistance=" + String.format("%.2f", totalDistance) +
                '}';
    }
}
