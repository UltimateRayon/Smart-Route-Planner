package com.example.srp.algorithms.pathfinding;

import com.example.srp.models.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PathCache
 */
class PathCacheTest {

    private PathCache cache;

    @BeforeEach
    void setUp() {
        cache = new PathCache();
    }

    @Test
    void testPutAndGet() {
        Path path = new Path(Arrays.asList("N1", "N2", "N3"), 5.0);

        cache.put("N1", "N3", path);

        Path retrieved = cache.get("N1", "N3");
        assertNotNull(retrieved);
        assertEquals(5.0, retrieved.getTotalDistance(), 0.001);
        assertEquals(3, retrieved.getVertices().size());
    }

    @Test
    void testBidirectionalStorage() {
        // When you store N1->N3, it should also store N3->N1 (reversed)
        Path path = new Path(Arrays.asList("N1", "N2", "N3"), 5.0);

        cache.put("N1", "N3", path);

        // Should be able to retrieve in reverse
        Path reversed = cache.get("N3", "N1");
        assertNotNull(reversed);
        assertEquals(5.0, reversed.getTotalDistance(), 0.001);

        // Vertices should be reversed
        assertEquals("N3", reversed.getVertices().get(0));
        assertEquals("N2", reversed.getVertices().get(1));
        assertEquals("N1", reversed.getVertices().get(2));
    }

    @Test
    void testGetNonExistent() {
        Path path = cache.get("N1", "N99");
        assertNull(path);
    }

    @Test
    void testMultiplePaths() {
        Path path1 = new Path(Arrays.asList("N1", "N2"), 2.0);
        Path path2 = new Path(Arrays.asList("N1", "N3"), 3.0);
        Path path3 = new Path(Arrays.asList("N2", "N3"), 4.0);

        cache.put("N1", "N2", path1);
        cache.put("N1", "N3", path2);
        cache.put("N2", "N3", path3);

        assertEquals(2.0, cache.get("N1", "N2").getTotalDistance(), 0.001);
        assertEquals(3.0, cache.get("N1", "N3").getTotalDistance(), 0.001);
        assertEquals(4.0, cache.get("N2", "N3").getTotalDistance(), 0.001);

        // Test reverse paths
        assertEquals(2.0, cache.get("N2", "N1").getTotalDistance(), 0.001);
        assertEquals(3.0, cache.get("N3", "N1").getTotalDistance(), 0.001);
        assertEquals(4.0, cache.get("N3", "N2").getTotalDistance(), 0.001);
    }

    @Test
    void testOverwrite() {
        Path path1 = new Path(Arrays.asList("N1", "N2"), 2.0);
        Path path2 = new Path(Arrays.asList("N1", "N2"), 1.5);

        cache.put("N1", "N2", path1);
        cache.put("N1", "N2", path2); // Overwrite

        Path retrieved = cache.get("N1", "N2");
        assertEquals(1.5, retrieved.getTotalDistance(), 0.001);
    }

    @Test
    void testSelfLoop() {
        Path path = new Path(Arrays.asList("N1"), 0.0);

        cache.put("N1", "N1", path);

        Path retrieved = cache.get("N1", "N1");
        assertNotNull(retrieved);
        assertEquals(0.0, retrieved.getTotalDistance(), 0.001);
    }
}