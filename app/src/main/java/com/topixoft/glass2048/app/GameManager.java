package com.topixoft.glass2048.app;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vitalypolonetsky on 3/23/14.
 */
public class GameManager implements GameManagerInput {

    private static final int START_TILES = 2;

    private final int size;
    private final InputManager inputManager;
    private final StorageManager storageManager;
    private final Actuator actuator;

    private boolean over;
    private boolean won;
    private boolean keepPlaying;

    private Grid grid;
    private int score;

    public GameManager(int size, InputManager inputManager, Actuator actuator, StorageManager storageManager) {
        this.size = size; // Size of the grid
        this.inputManager = inputManager;
        this.storageManager = storageManager;
        this.actuator = actuator;

        this.inputManager.bind(this);

        setup();
    }

    // Restart the game
    public void restart() {
        this.storageManager.clearGameState();
        this.actuator.continueGame(); // Clear the game won/lost message
        this.setup();
    }

    // Keep playing after winning (allows going over 2048)
    public void keepPlaying() {
        this.keepPlaying = true;
        this.actuator.continueGame(); // Clear the game won/lost message
    }

    // Return true if the game is lost, or has won and the user hasn't kept playing
    private boolean isGameTerminated() {
        if (this.over || (this.won && !this.keepPlaying)) {
            return true;
        } else {
            return false;
        }
    }

    // Set up the game
    private void setup() {
        Object previousState = this.storageManager.getGameState();

        // Reload the game from a previous game if present
        if (previousState != null) {
            // TODO: implement previousState
//            this.grid        = new Grid(previousState.grid.size,
//                                        previousState.grid.cells); // Reload grid
//            this.score       = previousState.score;
//            this.over        = previousState.over;
//            this.won         = previousState.won;
//            this.keepPlaying = previousState.keepPlaying;
        } else {
            this.grid        = new Grid(this.size);
            this.score       = 0;
            this.over        = false;
            this.won         = false;
            this.keepPlaying = false;

            // Add the initial tiles
            this.addStartTiles();
        }

        // Update the actuator
        this.actuate();
    }

    // Set up the initial tiles to start the game with
    private void addStartTiles() {
        for (int i = 0; i < START_TILES; i++) {
            this.addRandomTile();
        }
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 2));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 4));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 8));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 16));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 32));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 64));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 128));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 256));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 512));
//        this.grid.insertTile(new Tile(this.grid.randomAvailableCell(), 1024));
    }

    // Adds a tile in a random position
    private void addRandomTile() {
        if (this.grid.cellsAvailable()) {
            int value = Math.random() < 0.9 ? 2 : 4;
            Tile tile = new Tile(this.grid.randomAvailableCell(), value);

            this.grid.insertTile(tile);
        }
    }

    // Sends the updated grid to the actuator
    private void actuate() {
        if (this.storageManager.getBestScore() < this.score) {
            this.storageManager.setBestScore(this.score);
        }

        // Clear the state when the game is over (game over only, not win)
        if (this.over) {
            this.storageManager.clearGameState();
        } else {
            this.storageManager.setGameState(this.serialize());
        }

        this.actuator.actuate(this.grid,
                this.score,
                this.over,
                this.won,
                this.storageManager.getBestScore(),
                this.isGameTerminated()
        );

    }

    // Represent the current game as an object
    private Object serialize() {
        // TODO: implement serialize
        return null;
//        return {
//                grid:        this.grid.serialize(),
//                score:       this.score,
//                over:        this.over,
//                won:         this.won,
//                keepPlaying: this.keepPlaying
//        };
    }

    // Save all tile positions and remove merger info
    private void prepareTiles() {
        this.grid.eachCell(new Callback() {
            @Override
            public void callback(int x, int y, Tile tile) {
                if (tile != null) {
                    tile.setMergedFrom(null);
                    tile.savePosition();
                }
            }
        });
    }

    // Move a tile and its representation
    private void moveTile(Tile tile, Cell cell) {
        this.grid.cells[tile.getX()][tile.getY()] = null;
        this.grid.cells[cell.x][cell.y] = tile;
        tile.updatePosition(cell);
    }

    // Move tiles on the grid in the specified direction
    public void move(Direction direction) {

        if (this.isGameTerminated()) return; // Don't do anything if the game's over

        Cell vector = direction.getVector();
        Traversals traversals = this.buildTraversals(vector);
        boolean moved = false;

        // Save the current tile positions and remove merger information
        this.prepareTiles();

        // Traverse the grid in the right direction and move tiles
        for(int x : traversals.x) {
            for (int y : traversals.y) {
                Cell cell = new Cell(x, y);
                Tile tile = this.grid.cellContent(cell);

                if (tile != null) {
                    FarthestPosition positions = this.findFarthestPosition(cell, vector);
                    Tile next = this.grid.cellContent(positions.next);

                    // Only one merger per row traversal?
                    if (next != null && next.getValue() == tile.getValue() && next.getMergedFrom() == null) {
                        Tile merged = new Tile(positions.next, tile.getValue() * 2);
                        merged.setMergedFrom(new Tile[] { tile, next });

                        this.grid.insertTile(merged);
                        this.grid.removeTile(tile);

                        // Converge the two tiles' positions
                        tile.updatePosition(positions.next);

                        // Update the score
                        this.score += merged.getValue();

                        // The mighty 2048 tile
                        if (merged.getValue() == 2048) this.won = true;
                    } else {
                        this.moveTile(tile, positions.farthest);
                    }

                    if (!this.positionsEqual(cell, tile)) {
                        moved = true; // The tile moved from its original cell!
                    }
                }
            }
        }

        if (moved) {
            this.addRandomTile();

            if (!this.movesAvailable()) {
                this.over = true; // Game over!
            }

            this.actuate();
        }
    }

    public enum Direction {
        // Vectors representing tile movement
        Up(0, -1), // Up
        Right(1, 0), // Right
        Down(0, 1), // Down
        Left(-1, 0); // Left

        Cell vector;

        Direction(int x, int y) {
            vector = new Cell(x, y);
        }

        // Get the vector representing the chosen direction
        public Cell getVector() {
            return vector;
        }
    }

    // Build a list of positions to traverse in the right order
    private Traversals buildTraversals(Cell vector) {
        List<Integer> x = new LinkedList<Integer>();
        List<Integer> y = new LinkedList<Integer>();

        for (int pos = 0; pos < this.size; pos++) {
            x.add(pos);
            y.add(pos);
        }

        // Always traverse from the farthest cell in the chosen direction
        if (vector.x == 1) Collections.reverse(x);
        if (vector.y == 1) Collections.reverse(y);

        return new Traversals(x, y);
    }

    private FarthestPosition findFarthestPosition(Cell cell, Cell vector) {
        Cell previous = null;

        // Progress towards the vector direction until an obstacle is found
        do {
            previous = cell;
            cell = new Cell(previous.x + vector.x, previous.y + vector.y);
        } while (this.grid.withinBounds(cell) &&
                this.grid.cellAvailable(cell));

        return new FarthestPosition(
                previous,
                cell // Used to check if a merge is required
        );
    }

    private boolean movesAvailable() {
        return this.grid.cellsAvailable() || this.tileMatchesAvailable();
    }

    // Check for available matches between tiles (more expensive check)
    private boolean tileMatchesAvailable() {
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                Tile tile = this.grid.cellContent(new Cell(x, y));

                if (tile != null) {
                    for (Direction direction : Direction.values()) {
                        Cell vector = direction.getVector();
                        Cell cell = new Cell(x + vector.x, y + vector.y);

                        Tile other  = this.grid.cellContent(cell);

                        if (other != null && other.getValue() == tile.getValue()) {
                            return true; // These two tiles can be merged
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean positionsEqual(Cell first, Tile second) {
        return first.x == second.getX() && first.y == second.getY();
    }

}
