package com.example.srp.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path {
    private final List<String> vertices;
    private final double totalDistance;

    public Path(List<String> vertices, double totalDistance) {
        this.vertices=vertices;
        this.totalDistance=totalDistance;
    }

    public List<String> getVertices() {
        return vertices;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public Path reversed() {
        List<String> reversedNodes = new java.util.ArrayList<>(vertices);
        Collections.reverse(reversedNodes);
        return new Path(reversedNodes, totalDistance);
    }

    @Override
    public String toString() {
        return "Path{" +
                "vertices=" + vertices +
                ", totalDistance=" + totalDistance +
                '}';
    }
}
