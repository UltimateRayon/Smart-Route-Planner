package com.example.srp.algorithms.routing;

import com.example.srp.algorithms.pathfinding.PathCache;
import com.example.srp.models.Path;

import java.util.ArrayList;
import java.util.List;

public class TwoOptTSP implements TSPSolver{
    PathCache pathCache;
    TSPSolver initialSolver;
    int maxIteration;

    TwoOptTSP(PathCache pathCache, TSPSolver initialSolver, int maxIteration) {
        this.pathCache=pathCache;
        this.initialSolver=initialSolver;
        this.maxIteration=maxIteration;
    }

    public TwoOptTSP(PathCache pathCache, TSPSolver initialSolver) {
        this(pathCache, initialSolver, 100);
    }

    @Override
    public List<String> solveTSP(List<String> nodes, String startNode) {
        List<String> tour=initialSolver.solveTSP(nodes, startNode);

        if(tour.size()<=3) {
            return tour; //Too small to optimize;
        }

        tour=twoOptImprove(tour);
        return tour;
    }

    private List<String> twoOptImprove(List<String> tour) {
        List<String> bestTour=new ArrayList<>(tour);
        double bestDistance=calculateTourDistance(tour);
        boolean improved=true;
        int iteration=0;

        while(improved && iteration<maxIteration) {
             improved=false;
             iteration++;

             for(int i=1; i<tour.size()-2; i++) {
                 for(int j=i+1; j<tour.size()-1; j++) {
                     List<String> newTour=twoOptSwap(tour, i, j);
                     double newDistance=calculateTourDistance(newTour);
                     if(newDistance<bestDistance) {
                         bestDistance=newDistance;
                         bestTour=newTour;
                         improved=true;
                         break;
                     }
                 }
                 if(improved) {
                     break; //Go to while loop
                 }
             }
        }
        return bestTour;
    }

    private List<String> twoOptSwap(List<String> tour, int i, int j) {
        List<String> newTour=new ArrayList<>();
        for(int k=0; k<i; k++) {
            newTour.add(tour.get(k));
        }
        for(int k=j; k>=i; k--) {
            newTour.add(tour.get(k));
        }
        for(int k=j+1; k<tour.size(); k++) {
            newTour.add(tour.get(k));
        }
        return newTour;
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
