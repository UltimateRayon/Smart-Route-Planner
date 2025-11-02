package com.example.srp.app; // or wherever you keep app utilities

import com.example.srp.algorithms.PathCache;
import com.example.srp.models.Path;

import java.text.DecimalFormat;
import java.util.List;

public class PrintUtils {

    /**
     * Print distance matrix and path sequences for the given mandatory node ids.
     *
     * @param mandatoryIds list of vertex ids, e.g. ["N1","N3","N5"]
     * @param cache        precomputed PathCache (must be built for these ids)
     */
    public static void printMandatoryDistanceMatrix(List<String> mandatoryIds, PathCache cache) {
        DecimalFormat df = new DecimalFormat("#0.00");

        System.out.println("=== Mandatory Node Distance Matrix ===");
        // header
        System.out.print("From\\To");
        for (String id : mandatoryIds) System.out.print("\t" + id);
        System.out.println();

        // matrix rows
        for (String fromId : mandatoryIds) {
            System.out.print(fromId);
            for (String toId : mandatoryIds) {
                if (fromId.equals(toId)) {
                    System.out.print("\t" + "--");
                    continue;
                }

                Path p = cache.get(fromId, toId);
                if (p == null) {
                    System.out.print("\t" + "N/A");
                } else {
                    double dist = p.getTotalDistance();
                    if (Double.isInfinite(dist) || Double.isNaN(dist)) {
                        System.out.print("\t" + "INF");
                    } else {
                        System.out.print("\t" + df.format(dist));
                    }
                }
            }
            System.out.println();
        }

        // print detailed paths
        System.out.println("\n=== Path Sequences (if available) ===");
        for (String fromId : mandatoryIds) {
            for (String toId : mandatoryIds) {
                if (fromId.equals(toId)) continue;
                Path p = cache.get(fromId, toId);
                if (p == null) {
                    System.out.println(fromId + " -> " + toId + " : (no path cached)");
                } else {
                    double dist = p.getTotalDistance();
                    String distStr = (Double.isInfinite(dist) || Double.isNaN(dist)) ? "INF" : df.format(dist);
                    System.out.println(fromId + " -> " + toId + " : " + distStr +
                            "   path=" + String.join("->", p.getVertices()));
                }
            }
        }
    }
}

