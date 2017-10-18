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
    public PathFinding() {}

    public void computePaths(POI source) {
        source.setMinDistance(0);
        PriorityQueue<POI> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(source);
        repositoryPath.setAdjacencies(source);
        while (!vertexQueue.isEmpty()) {
            POI u = vertexQueue.poll();
            for (Path e : u.getAdjacencies()) {
                POI v = findPOI(e.getEnd());
                repositoryPath.setAdjacencies(v);
                double weight = e.getWeight();
                double distanceThroughU = u.getMinDistance() + weight;
                if (distanceThroughU < v.getMinDistance()) {
                        vertexQueue.remove(v);
                        v.setMinDistance(distanceThroughU);
                        Log.e(v.toString(), String.valueOf(distanceThroughU));
                        v.setPrevious(u);
                        vertexQueue.add(v);
                }
            }
        }
    }

    public List<POI> getShortestPathTo(POI target) {
        POI t = findPOI(target);
        List<POI> path = new ArrayList<>();
        for (POI vertex = t; vertex != null; vertex = vertex.getPrevious()) {
            path.add(vertex);
        }
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

    public POI findPOI(POI poi) {
        for (POI p : poiList) {
            if (p.getId() == poi.getId()) {
                return p;
            }
        }
        return null;
    }
}