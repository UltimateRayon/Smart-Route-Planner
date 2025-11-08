package com.example.srp.algorithms.balancing;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.Path;
import com.example.srp.models.RouteInfo;

import java.util.List;

public class RouteEvaluator {
    PathCache pathCache;

    public RouteEvaluator(PathCache pathCache) {
        this.pathCache=pathCache;
    }

    public RouteInfo evaluateRoute(int busId, List<String> tour, int hour) {
        double totalDistance=calculateTourDistance(tour);
        return new RouteInfo(busId, tour, totalDistance, hour);
    }

    private double calculateTourDistance(List<String> tour) {
        if(tour.size()<2) {
            return 0.0;
        }

        double totalDistance=0.0;
        for(int i=0; i<tour.size()-1; i++) {
            String from=tour.get(i);
            String to=tour.get(i+1);
            Path path=pathCache.get(from, to);
            if(path==null) {
                throw new IllegalStateException("No path found from " + from + " to " + to);
            }
            totalDistance+=path.getTotalDistance();
        }
        return totalDistance;
    }

    public double calculateImbalanceRatio(List<RouteInfo> routes) {
        if(routes.isEmpty()) {
            return 1.0;
        }

        double maxDistance=routes.stream()
                .mapToDouble(RouteInfo::getTotalDistance)
                .max()
                .orElse(0.0);

        double minDistance=routes.stream()
                .mapToDouble(RouteInfo::getTotalDistance)
                .min()
                .orElse(1.0);

        if(minDistance==0.0) {
            return Double.POSITIVE_INFINITY;
        }
        return maxDistance/minDistance;
    }

    public double calculateMakespan(List<RouteInfo> routes) {
        return routes.stream()
                .mapToDouble(RouteInfo::getTotalDistance)
                .max()
                .orElse(0.0);
    }

    public double calculateTotalDistance(List<RouteInfo> routes) {
        return routes.stream()
                .mapToDouble(RouteInfo::getTotalDistance)
                .sum();
    }
}
