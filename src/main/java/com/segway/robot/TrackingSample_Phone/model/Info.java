package com.segway.robot.TrackingSample_Phone.model;

/**
 * Created by Alex Pitkin on 02.12.2017.
 */

public class Info {

    private int id;
    private POI start;
    private POI goal;
    private POI next;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public POI getStart() {
        return start;
    }

    public void setStart(POI start) {
        this.start = start;
    }

    public POI getGoal() {
        return goal;
    }

    public void setGoal(POI goal) {
        this.goal = goal;
    }

    public POI getNext() {
        return next;
    }

    public void setNext(POI next) {
        this.next = next;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description+ " " + start.getId()+ " " + goal.getId()+ " " + next.getId();
    }
}
