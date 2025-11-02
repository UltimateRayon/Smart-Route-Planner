package com.example.srp.algorithms;

import com.example.srp.models.Path;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathCache {
    Map<String, Map<String, Path>> cache=new HashMap<>();

    public void put(String from, String to, Path path) {
        cache.computeIfAbsent(from, k-> new HashMap<>()).put(to, path);
        cache.computeIfAbsent(to, k-> new HashMap<>()).put(from, path.reversed());
    }

    public Path get(String from, String to) {
        return cache.getOrDefault(from, Collections.emptyMap()).get(to);
    }
}
