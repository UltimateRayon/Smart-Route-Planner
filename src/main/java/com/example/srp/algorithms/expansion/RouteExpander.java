package com.example.srp.algorithms.expansion;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.DetailedRoute;
import com.example.srp.models.Path;
import com.example.srp.models.RouteInfo;

import java.util.ArrayList;
import java.util.List;

public class RouteExpander {
    private final PathCache pathCache;

    public RouteExpander(PathCache pathCache) {
        this.pathCache = pathCache;
    }

    public DetailedRoute expandRoute(RouteInfo routeInfo) {
        List<String> tour = routeInfo.getTour();
        List<String> fullNodeSequence = new ArrayList<>();
        List<RouteSegment> segments = new ArrayList<>();

        for (int i = 0; i < tour.size() - 1; i++) {
            String from = tour.get(i);
            String to = tour.get(i + 1);

            Path path = pathCache.get(from, to);
            if (path == null) {
                throw new IllegalStateException("No path found from " + from + " to " + to);
            }

            List<String> segmentNodes = path.getVertices();
            segments.add(new RouteSegment(from, to, segmentNodes, path.getTotalDistance()));
            if (i == 0) {
                fullNodeSequence.addAll(segmentNodes);
            } else {
                // Skip first node (already added as last node of previous segment)
                fullNodeSequence.addAll(segmentNodes.subList(1, segmentNodes.size()));
            }
        }
        return new DetailedRoute(routeInfo.getBusId(), tour, fullNodeSequence, segments, routeInfo.getTotalDistance(), routeInfo.getHour());
    }

    public List<DetailedRoute> expandRoutes(List<RouteInfo> routes) {
        List<DetailedRoute> detailedRoutes = new ArrayList<>();
        for (RouteInfo route : routes) {
            detailedRoutes.add(expandRoute(route));
        }
        return detailedRoutes;
    }

    public static class RouteSegment {

        private final String fromNode;
        private final String toNode;
        private final List<String> nodeSequence;
        private final double distance;
        public RouteSegment(String fromNode, String toNode, List<String> nodeSequence, double distance) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.nodeSequence = nodeSequence;
            this.distance = distance;
        }

        public String getFromNode() {
            return fromNode;
        }

        public String getToNode() {
            return toNode;
        }

        public List<String> getNodeSequence() {
            return nodeSequence;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            return fromNode + " → " + toNode +
                    " (" + String.format("%.2f", distance) + " km) " +
                    "via " + nodeSequence;
        }

    }
}
