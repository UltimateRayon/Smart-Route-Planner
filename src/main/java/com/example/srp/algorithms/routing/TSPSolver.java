package com.example.srp.algorithms.routing;

import java.util.List;

public interface TSPSolver {
    List<String> solveTSP(List<String> nodes, String startNode);
    double calculateTourDistance(List<String> tour);
}
