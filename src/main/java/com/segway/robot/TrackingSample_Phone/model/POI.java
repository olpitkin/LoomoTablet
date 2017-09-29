package com.segway.robot.TrackingSample_Phone.model;

import java.text.DecimalFormat;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class POI {

    private int id;
    private String description;
    private String type;
    private  double x;
    private  double y;
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

    @Override
    public String toString() {
        DecimalFormat threeDec = new DecimalFormat("0.000");
        return type + " " + id  + " [" +  threeDec.format(x) + " ; " + threeDec.format(y) + "]" ;
    }

}
