package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import java.util.List;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.MovementService;
import commands.BasicCommands;

public class TileClicked implements EventProcessor {

    private final MovementService ms = new MovementService();

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        final int x = message.get("tilex").asInt();
        final int y = message.get("tiley").asInt();

        if (!ms.isWithinBounds(x, y)) return;

        final Tile t = gameState.getBoard().getTile(x, y);
        final Unit u = t.getUnit();
        final Unit s = gameState.getSelectedUnit();

        if (u != null && u.getOwner() == gameState.getCurrentPlayer()) {
            refresh(out, gameState);
            List<Tile> moves = ms.getValidMoves(gameState.getBoard(), u);
            for (Tile m : moves) BasicCommands.drawTile(out, m, 1);
            gameState.setSelectedUnit(u);
            return;
        }

        if (s != null) {
            BasicCommands.moveUnitToTile(out, s, t);
            refresh(out, gameState);
            gameState.setSelectedUnit(null);
        }
    }

    private void refresh(ActorRef out, GameState gameState) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                BasicCommands.drawTile(out, gameState.getBoard().getTile(i, j), 0);
            }
        }
    }
}
