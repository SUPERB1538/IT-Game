package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Handles the completion of unit movement. Synchronizes the backend 
 * board occupancy to prevent ghost units and updates unit position data.
 */
public class UnitStopped implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        Unit unit = gameState.getSelectedUnit();
        if (unit == null) return;

        Board board = gameState.getBoard();
        
        // 1. Clear unit reference from its old tile to prevent duplicates (Ghost Units)
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                Tile t = board.getTile(i, j);
                if (t.getUnit() == unit) {
                    t.setUnit(null);
                }
            }
        }
        
        // 2. Occupy the new tile in the backend logic
        int newX = unit.getPosition().getTilex();
        int newY = unit.getPosition().getTiley();
        Tile newTile = board.getTile(newX, newY);
        
        if (newTile != null) {
            newTile.setUnit(unit);
        }

        // 3. Finalize interaction by clearing the global selection state
        gameState.setSelectedUnit(null);
    }
}