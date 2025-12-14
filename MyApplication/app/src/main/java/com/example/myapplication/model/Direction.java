package com.example.myapplication.model;

public class Direction {
    private String instruction;
    private String distance;
    private String maneuver;

    public Direction(String instruction, String distance, String maneuver) {
        this.instruction = instruction;
        this.distance = distance;
        this.maneuver = maneuver;
    }

    public String getInstruction() { return instruction; }
    public String getDistance() { return distance; }
    public String getManeuver() { return maneuver; }
}
