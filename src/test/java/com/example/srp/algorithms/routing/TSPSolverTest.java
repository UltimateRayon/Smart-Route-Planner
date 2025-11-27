package com.example.srp.algorithms.routing;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TSP solvers
 */
class TSPSolverTest {

    private PathCache pathCache;
    private TSPSolver nearestNeighbor;
    private TSPSolver twoOpt;

    @BeforeEach
    void setUp() {
        // Create a simple test graph with known distances
        // N1 --- 2 --- N2
        //  |           |
        //  3           2
        //  |           |
        // N3 --- 4 --- N4

        pathCache = new PathCache();

        // Add all pairwise paths
        pathCache.put("N1", "N2", new Path(Arrays.asList("N1", "N2"), 2.0));
        pathCache.put("N1", "N3", new Path(Arrays.asList("N1", "N3"), 3.0));
        pathCache.put("N1", "N4", new Path(Arrays.asList("N1", "N4"), 5.0));

        pathCache.put("N2", "N3", new Path(Arrays.asList("N2", "N3"), 4.0));
        pathCache.put("N2", "N4", new Path(Arrays.asList("N2", "N4"), 2.0));

        pathCache.put("N3", "N4", new Path(Arrays.asList("N3", "N4"), 4.0));

        nearestNeighbor = new NearestNeighborTSP(pathCache);
        twoOpt = new TwoOptTSP(pathCache, nearestNeighbor);
    }

    @Test
    void testNearestNeighborBasic() {
        List<String> nodes = Arrays.asList("N1", "N2", "N3", "N4");
        String startNode = "N1";

        List<String> tour = nearestNeighbor.solveTSP(nodes, startNode);

        assertNotNull(tour);
        assertTrue(tour.size() >= nodes.size() + 1); // At least all nodes + return
        assertEquals(startNode, tour.get(0)); // Starts at start
        assertEquals(startNode, tour.get(tour.size() - 1)); // Ends at start
    }

    @Test
    void testTourContainsAllNodes() {
        List<String> nodes = Arrays.asList("N1", "N2", "N3", "N4");
        String startNode = "N1";

        List<String> tour = nearestNeighbor.solveTSP(nodes, startNode);

        // Check all nodes are visited at least once
        for (String node : nodes) {
            assertTrue(tour.contains(node), "Tour should contain " + node);
        }
    }

    @Test
    void testCalculateTourDistance() {
        List<String> tour = Arrays.asList("N1", "N2", "N4", "N3", "N1");

        // N1->N2: 2.0
        // N2->N4: 2.0
        // N4->N3: 4.0
        // N3->N1: 3.0
        // Total: 11.0

        double distance = nearestNeighbor.calculateTourDistance(tour);
        assertEquals(11.0, distance, 0.001);
    }

    @Test
    void testTwoOptImprovement() {
        List<String> nodes = Arrays.asList("N1", "N2", "N3", "N4");
        String startNode = "N1";

        // Get initial tour from nearest neighbor
        List<String> nnTour = nearestNeighbor.solveTSP(nodes, startNode);
        double nnDistance = nearestNeighbor.calculateTourDistance(nnTour);

        // Optimize with 2-opt
        List<String> optimizedTour = twoOpt.solveTSP(nodes, startNode);
        double optimizedDistance = twoOpt.calculateTourDistance(optimizedTour);

        // 2-opt should be same or better
        assertTrue(optimizedDistance <= nnDistance,
                "2-Opt should not make tour worse");
    }

    @Test
    void testEmptyNodeList() {
        List<String> nodes = Arrays.asList();
        String startNode = "N1";

        List<String> tour = nearestNeighbor.solveTSP(nodes, startNode);

        assertNotNull(tour);
        assertEquals(2, tour.size()); // Just [start, start]
        assertEquals(startNode, tour.get(0));
        assertEquals(startNode, tour.get(1));
    }

    @Test
    void testSingleNode() {
        List<String> nodes = Arrays.asList("N1");
        String startNode = "N1";

        List<String> tour = nearestNeighbor.solveTSP(nodes, startNode);

        assertNotNull(tour);
        assertEquals(2, tour.size()); // [N1, N1]
        assertEquals("N1", tour.get(0));
        assertEquals("N1", tour.get(1));
    }

    @Test
    void testTwoNodes() {
        List<String> nodes = Arrays.asList("N1", "N2");
        String startNode = "N1";

        List<String> tour = nearestNeighbor.solveTSP(nodes, startNode);

        // Should be [N1, N2, N1]
        assertEquals(3, tour.size());
        assertEquals("N1", tour.get(0));
        assertEquals("N2", tour.get(1));
        assertEquals("N1", tour.get(2));

        double distance = nearestNeighbor.calculateTourDistance(tour);
        assertEquals(4.0, distance, 0.001); // N1->N2->N1 = 2+2 = 4
    }

    @Test
    void testOptimalTourForSimpleCase() {
        // For 3 nodes in triangle, test if we get reasonable tour
        PathCache simpleCache = new PathCache();
        simpleCache.put("A", "B", new Path(Arrays.asList("A", "B"), 1.0));
        simpleCache.put("A", "C", new Path(Arrays.asList("A", "C"), 1.0));
        simpleCache.put("B", "C", new Path(Arrays.asList("B", "C"), 1.0));

        TSPSolver simpleSolver = new NearestNeighborTSP(simpleCache);
        List<String> tour = simpleSolver.solveTSP(Arrays.asList("A", "B", "C"), "A");

        // Any valid tour should have distance 3.0 (triangle)
        double distance = simpleSolver.calculateTourDistance(tour);
        assertEquals(3.0, distance, 0.001);
    }
}