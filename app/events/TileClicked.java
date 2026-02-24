package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import java.util.List;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.MovementService;
import commands.BasicCommands;

/**
 * Handles tile interaction events. Manages the state transition between
 * selecting a unit and issuing a movement command.
 */
public class TileClicked implements EventProcessor {

    private final MovementService ms = new MovementService();

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        final int x = message.get("tilex").asInt();
        final int y = message.get("tiley").asInt();

        if (!ms.isWithinBounds(x, y)) return;

        final Tile clickedTile = gameState.getBoard().getTile(x, y);
        final Unit unitOnTile = clickedTile.getUnit();
        final Unit selectedUnit = gameState.getSelectedUnit();

        // Selection Phase: Highlight valid moves for owned unit
        if (unitOnTile != null && unitOnTile.getOwner() == gameState.getCurrentPlayer()) {
            clearHighlights(out, gameState);
            List<Tile> moves = ms.getValidMoves(gameState.getBoard(), unitOnTile);
            for (Tile m : moves) BasicCommands.drawTile(out, m, 1);
            gameState.setSelectedUnit(unitOnTile);
            return; // Early exit to finalize selection state
        }

        // Execution Phase: Move unit if a valid target tile is clicked
        if (selectedUnit != null) {
            BasicCommands.moveUnitToTile(out, selectedUnit, clickedTile);
            clearHighlights(out, gameState);
            // SelectedUnit is NOT set to null here to allow UnitStopped to access it
        }
    }

    /**
     * Clears all visual highlights on the board UI.
     */
    private void clearHighlights(ActorRef out, GameState gameState) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                BasicCommands.drawTile(out, gameState.getBoard().getTile(i, j), 0);
            }
        }
    }
}