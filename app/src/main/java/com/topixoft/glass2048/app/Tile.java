package com.topixoft.glass2048.app;

/**
 * Created by vitalypolonetsky on 3/24/14.
 */
public class Tile {

    private Cell position;
    private int value;
    private Cell previousPosition;
    private Tile[] mergedFrom;

    public Tile(Cell position, int value) {
        this.position = position;
        this.value = value;

        this.previousPosition = null;
        this.mergedFrom = null; // Tracks tiles that mergedFrom together
    }

    public void savePosition() {
        this.previousPosition = position;
    }

    public void updatePosition(Cell position) {
        this.position = position;
    }

    private void serialize() {
        // TODO: implement serialize
    }

    public Cell getPosition() {
        return position;
    }

    public int getValue() {
        return value;
    }

    public Tile[] getMergedFrom() {
        return mergedFrom;
    }

    public void setMergedFrom(Tile[] mergedFrom) {
        this.mergedFrom = mergedFrom;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }

    public Cell getPreviousPosition() {
        return previousPosition;
    }
}
