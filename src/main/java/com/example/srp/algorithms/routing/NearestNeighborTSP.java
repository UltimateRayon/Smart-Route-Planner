package com.example.srp.algorithms.routing;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.Path;

import java.util.*;

public class NearestNeighborTSP implements TSPSolver{
    PathCache pathCache;
    public NearestNeighborTSP(PathCache pathCache) {
        this.pathCache=pathCache;
    }

    @Override
    public List<String> solveTSP(List<String> nodes, String startNode) {
        //Edge Case #1
        if(nodes.isEmpty()) {
            return Arrays.asList(startNode, startNode);
        }
        // Edge Case #2
        if(nodes.size()==1) {
            if(nodes.get(0)==startNode) {
                return Arrays.asList(startNode, startNode);
            } else {
                return Arrays.asList(startNode, nodes.get(0), startNode);
            }
        }

        Set<String> nodeSet=new HashSet<>(nodes);
        nodeSet.add(startNode);
        List<String> tour=new ArrayList<>();
        Set<String> visited=new HashSet<>();
        String current=startNode;
        tour.add(current);
        visited.add(current);

        while(visited.size()< nodeSet.size()) {
            String nearest=findNearestUnvisited(current, nodeSet, visited);
            if(nearest==null) {
                break;
            }

            visited.add(nearest);
            tour.add(nearest);
            current=nearest;
        }
        tour.add(startNode);
        return tour;
    }

    private String findNearestUnvisited(String current, Set<String> nodeSet, Set<String> visited) {
        String nearest=null;
        double minValue=Double.POSITIVE_INFINITY;
        for(String node: nodeSet) {
            if(visited.contains(node)) {
                continue;
            }
            Path path=pathCache.get(current, node);
            if(path==null) {
                continue;
            }
            double distance= path.getTotalDistance();
            if(distance<minValue) {
                minValue=distance;
                nearest=node;
            }
        }
        return nearest;
    }

    @Override
    public double calculateTourDistance(List<String> tour) {
        if(tour.size()<2) {
            return 0.0;
        }
        double distance=0;
        for(int i=0; i<tour.size()-1; i++) {
            String from= tour.get(i);
            String to=tour.get(i+1);
            Path path=pathCache.get(from, to);
            if(path==null) {
                return Double.POSITIVE_INFINITY;
            }
            distance+=path.getTotalDistance();
        }
        return distance;
    }
}
