package com.example.srp.algorithms.clustering;

import com.example.srp.models.NodeCluster;

import java.util.List;

public interface ClusterAssigner {
    List<NodeCluster> assignNodes(List<String> mandatoryNodes, String startNode, int busNo);
}
