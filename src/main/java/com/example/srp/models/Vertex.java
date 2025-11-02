package com.example.srp.models;

public class Vertex {
    String id;
    double x;
    double y;

    public void setId(String id) {
        this.id = id;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Vertex{id='" + id + "', x='" + x + "', y='" + y +"'}";
    }
}

