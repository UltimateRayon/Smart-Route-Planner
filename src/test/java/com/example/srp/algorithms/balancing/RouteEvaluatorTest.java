package com.example.srp.algorithms.balancing;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.Path;
import com.example.srp.models.RouteInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RouteEvaluator
 */
class RouteEvaluatorTest {

    private PathCache pathCache;
    private RouteEvaluator evaluator;

    @BeforeEach
    void setUp() {
        pathCache = new PathCache();

        // Simple graph: N1 -> N2 -> N3 -> N1
        pathCache.put("N1", "N2", new Path(Arrays.asList("N1", "N2"), 2.0));
        pathCache.put("N2", "N3", new Path(Arrays.asList("N2", "N3"), 3.0));
        pathCache.put("N3", "N1", new Path(Arrays.asList("N3", "N1"), 4.0));

        evaluator = new RouteEvaluator(pathCache);
    }

    @Test
    void testEvaluateRoute() {
        List<String> tour = Arrays.asList("N1", "N2", "N3", "N1");

        RouteInfo info = evaluator.evaluateRoute(0, tour, 8);

        assertNotNull(info);
        assertEquals(0, info.getBusId());
        assertEquals(9.0, info.getTotalDistance(), 0.001); // 2+3+4 = 9
        assertEquals(8, info.getHour());
        assertEquals(3, info.getNodeCount()); // 4 nodes in tour - 1 = 3
    }

    @Test
    void testCalculateTourDistance() {
        List<String> tour = Arrays.asList("N1", "N2", "N3", "N1");

        double distance = evaluator.calculateTourDistance(tour);

        assertEquals(9.0, distance, 0.001);
    }

    @Test
    void testCalculateMakespan() {
        RouteInfo route1 = new RouteInfo(0, Arrays.asList("N1", "N2", "N1"), 5.0, 8);
        RouteInfo route2 = new RouteInfo(1, Arrays.asList("N1", "N3", "N1"), 8.0, 8);
        RouteInfo route3 = new RouteInfo(2, Arrays.asList("N1", "N2", "N3", "N1"), 6.0, 8);

        List<RouteInfo> routes = Arrays.asList(route1, route2, route3);

        double makespan = evaluator.calculateMakespan(routes);

        assertEquals(8.0, makespan, 0.001); // Maximum is 8.0
    }

    @Test
    void testCalculateTotalDistance() {
        RouteInfo route1 = new RouteInfo(0, Arrays.asList("N1", "N2", "N1"), 5.0, 8);
        RouteInfo route2 = new RouteInfo(1, Arrays.asList("N1", "N3", "N1"), 8.0, 8);
        RouteInfo route3 = new RouteInfo(2, Arrays.asList("N1", "N2", "N3", "N1"), 6.0, 8);

        List<RouteInfo> routes = Arrays.asList(route1, route2, route3);

        double total = evaluator.calculateTotalDistance(routes);

        assertEquals(19.0, total, 0.001); // 5+8+6 = 19
    }

    @Test
    void testCalculateImbalanceRatio() {
        RouteInfo route1 = new RouteInfo(0, Arrays.asList("N1", "N2", "N1"), 10.0, 8);
        RouteInfo route2 = new RouteInfo(1, Arrays.asList("N1", "N3", "N1"), 5.0, 8);

        List<RouteInfo> routes = Arrays.asList(route1, route2);

        double imbalance = evaluator.calculateImbalanceRatio(routes);

        assertEquals(2.0, imbalance, 0.001); // 10/5 = 2.0
    }

    @Test
    void testPerfectBalance() {
        RouteInfo route1 = new RouteInfo(0, Arrays.asList("N1", "N2", "N1"), 5.0, 8);
        RouteInfo route2 = new RouteInfo(1, Arrays.asList("N1", "N3", "N1"), 5.0, 8);

        List<RouteInfo> routes = Arrays.asList(route1, route2);

        double imbalance = evaluator.calculateImbalanceRatio(routes);

        assertEquals(1.0, imbalance, 0.001); // Perfect balance
    }

    @Test
    void testEmptyTour() {
        List<String> emptyTour = Arrays.asList();

        RouteInfo info = evaluator.evaluateRoute(0, emptyTour, 8);

        assertEquals(0.0, info.getTotalDistance(), 0.001);
        assertEquals(0, info.getNodeCount());
    }

    @Test
    void testSingleNodeTour() {
        List<String> singleNode = Arrays.asList("N1");

        RouteInfo info = evaluator.evaluateRoute(0, singleNode, 8);

        assertEquals(0.0, info.getTotalDistance(), 0.001);
        assertEquals(0, info.getNodeCount());
    }

    @Test
    void testInvalidPathThrowsException() {
        List<String> tourWithInvalidPath = Arrays.asList("N1", "N99", "N1");

        assertThrows(IllegalStateException.class, () -> {
            evaluator.evaluateRoute(0, tourWithInvalidPath, 8);
        });
    }
}