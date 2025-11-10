package com.example.srp.models;

import com.example.srp.algorithms.expansion.RouteExpander.RouteSegment;

import java.util.List;

public class DetailedRoute {
    private final int busId;
    private final List<String> waypoints;           // High-level waypoints (mandatory nodes)
    private final List<String> fullNodeSequence;    // Complete node-by-node path
    private final List<RouteSegment> segments;      // Segment-by-segment breakdown
    private final double totalDistance;
    private final int hour;

    public DetailedRoute(int busId, List<String> waypoints, List<String> fullNodeSequence, List<RouteSegment> segments, double totalDistance, int hour) {
        this.busId = busId;
        this.waypoints = waypoints;
        this.fullNodeSequence = fullNodeSequence;
        this.segments = segments;
        this.totalDistance = totalDistance;
        this.hour = hour;
    }

    public int getBusId() {
        return busId;
    }

    public List<String> getWaypoints() {
        return waypoints;
    }

    public List<String> getFullNodeSequence() {
        return fullNodeSequence;
    }

    public List<RouteSegment> getSegments() {
        return segments;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public int getHour() {
        return hour;
    }

    public int getTotalNodes() {
        return fullNodeSequence.size();
    }

    public String getTurnByTurnDirections() {
        StringBuilder sb = new StringBuilder();
        sb.append("Route for Bus ").append(busId).append(":\n");
        sb.append("Total Distance: ").append(String.format("%.2f km", totalDistance)).append("\n");
        sb.append("\nTurn-by-turn:\n");

        int step = 1;
        for (RouteSegment segment : segments) {
            sb.append(step++).append(". ");
            sb.append("From ").append(segment.getFromNode());
            sb.append(" to ").append(segment.getToNode());
            sb.append(" (").append(String.format("%.2f km", segment.getDistance())).append(")\n");
            sb.append("   Via: ").append(String.join(" → ", segment.getNodeSequence())).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "DetailedRoute{" +
                "busId=" + busId +
                ", waypoints=" + waypoints +
                ", totalNodes=" + fullNodeSequence.size() +
                ", distance=" + String.format("%.2f", totalDistance) +
                '}';
    }
}
