package services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.UnitEntity;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.UnitAnimationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Movement logic extracted from GameRulesEngine (Phase 2 Step 2.2).
 * Keeps behaviour identical to your previous movement code.
 */
public class MovementService {

    private final CommandDispatcher ui = new CommandDispatcher();

    public List<Position> computeDefaultMoves(GameState gameState, Position from) {
        List<Position> positions = new ArrayList<>();
        int x = from.getTilex();
        int y = from.getTiley();

        // Cardinal 1 tile
        addIfValidEmpty(positions, gameState, tilePos(x + 1, y));
        addIfValidEmpty(positions, gameState, tilePos(x - 1, y));
        addIfValidEmpty(positions, gameState, tilePos(x, y + 1));
        addIfValidEmpty(positions, gameState, tilePos(x, y - 1));

        // Cardinal 2 tiles
        addIfValidEmpty(positions, gameState, tilePos(x + 2, y));
        addIfValidEmpty(positions, gameState, tilePos(x - 2, y));
        addIfValidEmpty(positions, gameState, tilePos(x, y + 2));
        addIfValidEmpty(positions, gameState, tilePos(x, y - 2));

        // Diagonal 1 tile
        addIfValidEmpty(positions, gameState, tilePos(x + 1, y + 1));
        addIfValidEmpty(positions, gameState, tilePos(x + 1, y - 1));
        addIfValidEmpty(positions, gameState, tilePos(x - 1, y + 1));
        addIfValidEmpty(positions, gameState, tilePos(x - 1, y - 1));

        return positions;
    }

    /**
     * Move the currently selected unit to a target tile if allowed.
     * Returns true if the move executed, false otherwise.
     *
     * Behaviour matches your old GameRulesEngine.moveSelectedUnitTo(...).
     */
    public boolean moveSelectedUnitTo(ActorRef out, GameState gameState, Position targetPos, Tile targetTile) {

        Integer selectedId = gameState.getSelectedUnitId();
        if (selectedId == null) return false;

        UnitEntity unit = gameState.getUnitById(selectedId);
        if (unit == null) return false;

        int t = gameState.getGlobalTurnNumber();
        if (!unit.canMove(t)) {
            ui.notifyP1(out, "This unit can't move again this turn.", 2);
            return false;
        }

        if (!gameState.getBoard().isValidPosition(targetPos)) return false;
        if (gameState.getBoard().isOccupied(targetPos)) return false;

        Position from = unit.getPosition();

        // Update backend occupancy first
        gameState.getBoard().moveUnit(from, targetPos);

        // Sync unit position (tile + pixel via tile)
        unit.moveTo(targetTile);

        unit.markMoved(t);

        // UI animation (same as before)
        BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.move);
        BasicCommands.moveUnitToTile(out, unit, targetTile);
        BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.idle);

        return true;
    }

    // ----------------------------
    // Helpers
    // ----------------------------

    private void addIfValidEmpty(List<Position> out, GameState gameState, Position p) {
        if (!gameState.getBoard().isValidPosition(p)) return;
        if (gameState.getBoard().isOccupied(p)) return;
        out.add(p);
    }

    private Position tilePos(int tilex, int tiley) {
        Position p = new Position();
        p.setTilex(tilex);
        p.setTiley(tiley);
        return p;
    }
}