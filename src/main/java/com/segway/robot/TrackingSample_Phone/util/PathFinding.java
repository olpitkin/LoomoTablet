package com.segway.robot.TrackingSample_Phone.util;

import android.util.Log;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.model.Path;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by Alex Pitkin on 02.10.2017.
 */

public class PathFinding {

    RepositoryPOI repositoryPOI  = new RepositoryPOI();
    RepositoryPath repositoryPath = new RepositoryPath();

    List<POI> poiList = (LinkedList<POI>) repositoryPOI.getAllPOI();

    //  USAGE :
    //  computePaths(poiList.get(0)); // run Dijkstra
    //  System.out.println("Distance to " + poiList.get(3).toString() + ": " + poiList.get(3).getMinDistance());
    //  List<POI> path = getShortestPathTo(poiList.get(3));
    public PathFinding() {
        for (POI poi : poiList) {
            for (Path path : repositoryPath.getAllPathsFromPoi(poi)) {
                poi.getAdjacencies().add(path);
            }
        }
    }

    public void computePaths(POI source) {
        source.setMinDistance(0);
        PriorityQueue<POI> vertexQueue = new PriorityQueue<POI>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            POI u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Path e : u.getAdjacencies()) {

                // DUMB CODE :C
                int id = e.getEnd().getId();
                POI v = null;
                for (POI p : poiList) {
                    if (p.getId() == id) {
                        v = p;
                    }
                }
                if (v != null) {
                    double weight = e.getWeight();
                    double distanceThroughU = u.getMinDistance() + weight;
                    if (distanceThroughU < v.getMinDistance()) {
                        vertexQueue.remove(v);
                        v.setMinDistance(distanceThroughU);
                        v.setPrevious(u);
                        vertexQueue.add(v);
                } else {
                     throw new Error("something went wrong");
                    }
                }
            }
        }
    }

    public static List<POI> getShortestPathTo(POI target) {
        List<POI> path = new ArrayList<POI>();
        for (POI vertex = target; vertex != null; vertex = vertex.getPrevious())
            path.add(vertex);

        Collections.reverse(path);
        Log.i("SHORTEST PATH: ", path.toString());
        return path;
    }

    public POI getNearestPOI(POI myLocation) {
        POI nearest = null;
        double weight = Double.POSITIVE_INFINITY;
        double newWeight;
        for (POI p : poiList) {
            newWeight = Math.hypot(myLocation.getX() - p.getX(), myLocation.getY() - p.getY());
            if (newWeight < weight) {
                weight = newWeight;
                nearest = p;
            }
        }
        Log.i("Nearest POI:", nearest.toString());
        return nearest;
    }
}