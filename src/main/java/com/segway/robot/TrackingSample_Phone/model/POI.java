package com.segway.robot.TrackingSample_Phone.model;

import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class POI implements Comparable<POI> {

    private Integer id;
    private String description;
    private String type;
    private  double x;
    private  double y;
    private int areaId;

    // Dijkstra fields
    private double minDistance = Double.POSITIVE_INFINITY;
    private List<Path> adjacencies  = new ArrayList<>();
    private POI previous;

    public POI(){};
    public POI(String description, String type, double x, double y) {
        super();
        this.description = description;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public List<Path> getAdjacencies() {
        return adjacencies;
    }

    public void setAdjacencies(List<Path> adjacencies) {
        this.adjacencies = adjacencies;
    }

    public POI getPrevious() {
        return previous;
    }

    public void setPrevious(POI previous) {
        this.previous = previous;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    @Override
    public String toString() {
        DecimalFormat threeDec = new DecimalFormat("0.000");
        return type + " " + id  + " [" +  threeDec.format(x) + " ; " + threeDec.format(y) + "]" ;
    }

    @Override
    public int compareTo(@NonNull POI other) {
        return Double.compare(minDistance, other.minDistance);
    }

    public boolean isNear (POI poi) {
        if (this.x >= poi.getX() - 0.25 && this.x <= poi.getX() + 0.25) {
            if (this.y >= poi.getY() - 0.25 && this.y <= poi.getY() + 0.25)
            {
                return  true;
            }
            return false;
        }
        return false;
    }
}
