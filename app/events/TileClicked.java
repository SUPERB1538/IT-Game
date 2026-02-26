package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import structures.GameState;
import java.util.ArrayList;
import java.util.List;
import commands.BasicCommands;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;

/**
 * Section C: Grid Interaction Implementation.
 * Handles unit selection, movement validity (Manhattan distance), and attack triggering.
 */
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Turn lock: ignore all tile interactions when it is not the human's turn
		if (!gameState.isHumanActionAllowed()) {
			// Optional: clear any leftover highlight/selection to avoid confusing UI
			clearHighlight(out, gameState);
			gameState.selectedUnit = null;
			gameState.selectedTile = null;

			BasicCommands.addPlayer1Notification(out, "It's not your turn.", 2);
			return;
		}

		// 1. Extract frontend coordinates (1-indexed)
		int x = message.get("tilex").asInt();
		int y = message.get("tiley").asInt();

		// 2. Boundary Check: Ensure click is within the 9x5 grid
		if (x < 1 || x > 9 || y < 1 || y > 5) return;

		// 3. Retrieve objects
		// Note: The frontend uses 1-based indexing, while the backend board array uses 0-based.
		Tile clicked = BasicObjectBuilders.loadTile(x, y);
		Unit occupant = gameState.board[x - 1][y - 1];

		// 4. Asynchronous Movement Guard
		if (gameState.movingUnit != null) return;

		// SCENARIO A: Unit Selection
		if (occupant != null && isHumanUnit(gameState, occupant)) {

			clearHighlight(out, gameState);

			gameState.selectedUnit = occupant;
			gameState.selectedTile = clicked;

			BasicCommands.addPlayer1Notification(out, "Unit Selected", 2);

			List<Tile> moves = calculateValidMoves(gameState, occupant);
			highlight(out, gameState, moves);
			return;
		}

		// SCENARIO B: Movement Execution
		if (gameState.selectedUnit != null && isHighlighted(gameState, x, y)) {

			int fromX = gameState.selectedUnit.getPosition().getTilex();
			int fromY = gameState.selectedUnit.getPosition().getTiley();

			gameState.moveFromX = fromX;
			gameState.moveFromY = fromY;
			gameState.moveToX = x;
			gameState.moveToY = y;
			gameState.movingUnit = gameState.selectedUnit;

			clearHighlight(out, gameState);
			BasicCommands.moveUnitToTile(out, gameState.selectedUnit, clicked);
			return;
		}

		// SCENARIO C: Attack Interaction
		if (gameState.selectedUnit != null && occupant != null && !isHumanUnit(gameState, occupant)) {

			int sx = gameState.selectedUnit.getPosition().getTilex();
			int sy = gameState.selectedUnit.getPosition().getTiley();
			int distance = Math.abs(sx - x) + Math.abs(sy - y);

			if (distance <= 2) {
				BasicCommands.addPlayer1Notification(out, "Attacking Target!", 2);

				BasicCommands.playUnitAnimation(out, gameState.selectedUnit, UnitAnimationType.attack);
				BasicCommands.playUnitAnimation(out, occupant, UnitAnimationType.hit);

				clearHighlight(out, gameState);
				gameState.selectedUnit = null;
				gameState.selectedTile = null;
				return;
			}
		}

		// SCENARIO D: Deselection
		clearHighlight(out, gameState);
		gameState.selectedUnit = null;
		gameState.selectedTile = null;
	}

	// Helper Methods ...

	private boolean isHumanUnit(GameState gameState, Unit unit) {
		if (unit == null) return false;
		if (unit == gameState.aiAvatar) return false;
		return true;
	}

	private void highlight(ActorRef out, GameState gameState, List<Tile> tiles) {
		if (tiles == null) return;
		for (Tile t : tiles) {
			BasicCommands.drawTile(out, t, 1);
			gameState.highlightedTiles.add(t);
		}
	}

	private void clearHighlight(ActorRef out, GameState gameState) {
		if (gameState.highlightedTiles == null) return;
		for (Tile t : gameState.highlightedTiles) {
			BasicCommands.drawTile(out, t, 0);
		}
		gameState.highlightedTiles.clear();
	}

	private boolean isHighlighted(GameState gameState, int x, int y) {
		if (gameState.highlightedTiles == null) return false;
		for (Tile t : gameState.highlightedTiles) {
			if (t.getTilex() == x && t.getTiley() == y) return true;
		}
		return false;
	}

	private List<Tile> calculateValidMoves(GameState gameState, Unit unit) {
		List<Tile> result = new ArrayList<>();
		if (unit == null || unit.getPosition() == null) return result;

		int x = unit.getPosition().getTilex();
		int y = unit.getPosition().getTiley();

		int[][] dirs = new int[][] { {1,0}, {-1,0}, {0,1}, {0,-1} };
		for (int[] d : dirs) {
			for (int step = 1; step <= 2; step++) {
				int nx = x + d[0] * step;
				int ny = y + d[1] * step;

				if (nx < 1 || nx > 9 || ny < 1 || ny > 5) break;
				if (gameState.board[nx - 1][ny - 1] != null) break;

				result.add(BasicObjectBuilders.loadTile(nx, ny));
			}
		}

		int[][] diag = new int[][] { {1,1}, {1,-1}, {-1,1}, {-1,-1} };
		for (int[] d : diag) {
			int nx = x + d[0];
			int ny = y + d[1];

			if (nx < 1 || nx > 9 || ny < 1 || ny > 5) continue;
			if (gameState.board[nx - 1][ny - 1] != null) continue;

			result.add(BasicObjectBuilders.loadTile(nx, ny));
		}

		return result;
	}
}