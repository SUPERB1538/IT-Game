package structures;

import java.util.ArrayList;
import java.util.List;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Service class for calculating valid movement ranges on a 9x5 grid.
 * Adheres to Manhattan distance rules with optimized pruning for performance.
 */
public class MovementService {

    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 5;
    private static final int MAX_MOVEMENT_RANGE = 2;

    /**
     * Calculates all unoccupied tiles within a movement range of 2.
     * @param board Current game board state.
     * @param unit The unit currently being calculated for movement.
     * @return List of valid target tiles for movement.
     */
    public List<Tile> getValidMoves(Board board, Unit unit) {
        List<Tile> validTiles = new ArrayList<>(12);
        if (unit == null || board == null) return validTiles;

        final int startX = unit.getPosition().getTilex();
        final int startY = unit.getPosition().getTiley();

        for (int x = 0; x < BOARD_WIDTH; x++) {
            final int distX = Math.abs(startX - x);
            if (distX > MAX_MOVEMENT_RANGE) continue; // Performance pruning

            for (int y = 0; y < BOARD_HEIGHT; y++) {
                final int distY = Math.abs(startY - y);
                final int totalDist = distX + distY;

                // Ensure tile is within range and not the current tile
                if (totalDist > 0 && totalDist <= MAX_MOVEMENT_RANGE) {
                    Tile target = board.getTile(x, y);
                    if (target != null && !target.hasUnit()) {
                        validTiles.add(target);
                    }
                }
            }
        }
        return validTiles;
    }

    /**
     * Checks if the provided coordinates reside within the 9x5 grid bounds.
     */
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT;
    }
}