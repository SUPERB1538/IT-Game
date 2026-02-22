package structures;

import java.util.ArrayList;
import java.util.List;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.Unit;

public class MovementService {

    private static final int W = 9;
    private static final int H = 5;
    private static final int R = 2;

    public List<Tile> getValidMoves(Board board, Unit unit) {
        List<Tile> validTiles = new ArrayList<>(12);
        if (unit == null || board == null) return validTiles;

        final int sx = unit.getPosition().getTilex();
        final int sy = unit.getPosition().getTiley();

        for (int x = 0; x < W; x++) {
            final int dx = Math.abs(sx - x);
            if (dx > R) continue;

            for (int y = 0; y < H; y++) {
                final int dy = Math.abs(sy - y);
                final int d = dx + dy;

                if (d > 0 && d <= R) {
                    Tile t = board.getTile(x, y);
                    if (t != null && !t.hasUnit()) {
                        validTiles.add(t);
                    }
                }
            }
        }
        return validTiles;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < W && y >= 0 && y < H;
    }
}
