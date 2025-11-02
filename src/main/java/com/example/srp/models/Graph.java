package com.example.srp.models;

import java.util.*;

public class Graph {
    public Map<String, List<Edge>> adjList = new HashMap<>();

    public void addEdge(String id, String from, String to, double distance, double[] traffic) {
        Edge e1=new Edge(id, from, to, distance, traffic);
        Edge e2=new Edge(id, to, from, distance, traffic); //bidirectional
        adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(e1);
        adjList.computeIfAbsent(to, k -> new ArrayList<>()).add(e2);
    }

    public List<Edge> getNeighborEdge(String node) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    public Set<String> getVertices() {
        return adjList.keySet();
    }
}
