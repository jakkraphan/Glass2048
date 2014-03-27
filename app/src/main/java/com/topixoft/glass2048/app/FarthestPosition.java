package com.topixoft.glass2048.app;

/**
 * Created by vitalypolonetsky on 3/24/14.
 */
public class FarthestPosition {
    public final Cell farthest;
    public final Cell next;

    public FarthestPosition(Cell farthest, Cell next) {
        this.farthest = farthest;
        this.next = next;
    }
}
