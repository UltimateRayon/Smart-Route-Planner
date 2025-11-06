package com.example.srp.algorithms.clustering;

import com.example.srp.models.NodeCluster;

import java.util.List;

public interface ClusterAssigner {
    List<NodeCluster> assignNodes(String startNode, List<String> mandatoryNodes, int busNo);
}
