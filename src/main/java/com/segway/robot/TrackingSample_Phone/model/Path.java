package com.segway.robot.TrackingSample_Phone.model;

import java.text.DecimalFormat;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class Path {

    private int id;
    private POI start;
    private POI end;

    public Path() {}

    public Path(POI start, POI end) {
        super();
        this.start = start;
        this.end = end;
    }

    public POI getStart() {
        return start;
    }

    public void setStart(POI start) {
        this.start = start;
    }

    public POI getEnd() {
        return end;
    }

    public void setEnd(POI end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        if (start != null && end != null) {
            DecimalFormat threeDec = new DecimalFormat("0.000");
            double distance = Math.hypot(start.getX() - end.getX(), start.getY() - end.getY());
            return start.toString() + " -> " + end.toString() + " : " + threeDec.format(distance)+ " m ";
        } else {
            return "start or end is null";
        }
    }
}