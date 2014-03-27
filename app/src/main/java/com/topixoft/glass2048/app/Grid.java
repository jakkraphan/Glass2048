package com.topixoft.glass2048.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vitalypolonetsky on 3/23/14.
 */
public class Grid {

    private final int size;
    public final Tile[][] cells;

    public Grid(int size) {
        this(size, null);
    }

    public Grid(int size, Object previousState) {
        this.size = size;
        this.cells = previousState != null ? this.fromState(previousState) : this.empty();
    }

    // Build a grid of the specified size
    private Tile[][] empty() {
        Tile[][] cells = new Tile[this.size][this.size];

        return cells;
    }

    private Tile[][] fromState(Object state) {
        // TODO: implement fromState
        return null;
    }

    // Find the first available random position
    public Cell randomAvailableCell() {
        Cell[] cells = this.availableCells();

        if (cells.length > 0) {
            return cells[(int) (Math.random() * cells.length)];
        } else {
            return null;
        }
    }

    private Cell[] availableCells() {
        final List<Cell> cells = new ArrayList<Cell>();

        this.eachCell(new Callback() {
            public void callback(int x, int y, Tile tile) {
                if (tile == null) {
                    cells.add(new Cell(x, y));
                }
            }
        });

        return cells.toArray(new Cell[0]);
    }

    // Call callback for every cell
    public void eachCell(Callback callback) {
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                callback.callback(x, y, this.cells[x][y]);
            }
        }
    }

    // Check if there are any cells available
    public boolean cellsAvailable() {
        return this.availableCells().length > 0;
    }

    // Check if the specified cell is taken
    public boolean cellAvailable(Cell cell) {
        return !this.cellOccupied(cell);
    }

    private boolean cellOccupied(Cell cell) {
        return this.cellContent(cell) != null;
    }

    public Tile cellContent(Cell cell) {
        if (this.withinBounds(cell)) {
            return this.cells[cell.x][cell.y];
        } else {
            return null;
        }
    }

    // Inserts a tile at its position
    public void insertTile(Tile tile) {
        this.cells[tile.getX()][tile.getY()] = tile;
    }

    public void removeTile(Tile tile) {
        this.cells[tile.getX()][tile.getY()] = null;
    }

    public boolean withinBounds(Cell position) {
        return position.x >= 0 && position.x < this.size &&
                position.y >= 0 && position.y < this.size;
    }

    private void serialize() {
        // TODO: implement serialize
    }
}
