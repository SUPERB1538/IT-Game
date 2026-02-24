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
		// Ignore inputs if a unit is currently performing a movement animation.
		// This prevents state inconsistencies between frontend and backend.
		if (gameState.movingUnit != null) return;

		// =========================================================
		// SCENARIO A: Unit Selection
		// Requirement: Click on a friendly unit to select it and highlight valid moves.
		// =========================================================
		if (occupant != null && isHumanUnit(gameState, occupant)) {

			// Clear previous highlighting
			clearHighlight(out, gameState);

			// Update Global Selection State
			gameState.selectedUnit = occupant;
			gameState.selectedTile = clicked;

			// Visual Feedback
			BasicCommands.addPlayer1Notification(out, "Unit Selected", 2);

			// Calculate and render valid movement tiles based on rules
			List<Tile> moves = calculateValidMoves(gameState, occupant);
			highlight(out, gameState, moves);
			return;
		}

		// =========================================================
		// SCENARIO B: Movement Execution
		// Requirement: Click on a highlighted tile to move the selected unit.
		// =========================================================
		if (gameState.selectedUnit != null && isHighlighted(gameState, x, y)) {

			// 1. Capture Start Position (1-indexed)
			int fromX = gameState.selectedUnit.getPosition().getTilex();
			int fromY = gameState.selectedUnit.getPosition().getTiley();

			// 2. Update GameState Bookkeeping for UnitStopped Event
			// Crucial: These fields allow the backend board to update AFTER animation completes.
			gameState.moveFromX = fromX;
			gameState.moveFromY = fromY;
			gameState.moveToX = x;
			gameState.moveToY = y;
			gameState.movingUnit = gameState.selectedUnit;

			// 3. Clear visuals and trigger Animation
			clearHighlight(out, gameState);
			BasicCommands.moveUnitToTile(out, gameState.selectedUnit, clicked);
			return;
		}

		// =========================================================
		// SCENARIO C: Attack Interaction
		// Requirement: Click on an enemy unit within range to attack.
		// =========================================================
		if (gameState.selectedUnit != null && occupant != null && !isHumanUnit(gameState, occupant)) {

			// Check Attack Range (Manhattan Distance)
			// Rules typically allow attacks on adjacent tiles (Distance = 1) or specific ranges.
			int sx = gameState.selectedUnit.getPosition().getTilex();
			int sy = gameState.selectedUnit.getPosition().getTiley();
			int distance = Math.abs(sx - x) + Math.abs(sy - y);

			// Valid Attack Range (Adjacent or Cardinal <= 2 depending on specific unit rules)
			// For this implementation, we check if distance is reasonable for interaction
			if (distance <= 2) {
				BasicCommands.addPlayer1Notification(out, "Attacking Target!", 2);

				// Trigger Attack and Hit animations
				BasicCommands.playUnitAnimation(out, gameState.selectedUnit, UnitAnimationType.attack);
				BasicCommands.playUnitAnimation(out, occupant, UnitAnimationType.hit);

				// Note: Health reduction logic should be handled here or in a separate AttackEvent.

				// Cleanup Selection
				clearHighlight(out, gameState);
				gameState.selectedUnit = null;
				gameState.selectedTile = null;
				return;
			}
		}

		// =========================================================
		// SCENARIO D: Deselection
		// Requirement: Clicking elsewhere clears current selection.
		// =========================================================
		clearHighlight(out, gameState);
		gameState.selectedUnit = null;
		gameState.selectedTile = null;
	}

	// =============================================================
	// Helper Methods
	// =============================================================

	/**
	 * Checks if the unit belongs to the human player.
	 */
	private boolean isHumanUnit(GameState gameState, Unit unit) {
		if (unit == null) return false;
		// Logic: If it is not the AI avatar, we assume it is Human for now.
		if (unit == gameState.aiAvatar) return false;
		return true;
	}

	/**
	 * Renders white highlights on specified tiles.
	 */
	private void highlight(ActorRef out, GameState gameState, List<Tile> tiles) {
		if (tiles == null) return;
		for (Tile t : tiles) {
			BasicCommands.drawTile(out, t, 1); // Mode 1 = Highlight
			gameState.highlightedTiles.add(t);
		}
	}

	/**
	 * Clears all currently highlighted tiles.
	 */
	private void clearHighlight(ActorRef out, GameState gameState) {
		if (gameState.highlightedTiles == null) return;
		for (Tile t : gameState.highlightedTiles) {
			BasicCommands.drawTile(out, t, 0); // Mode 0 = Normal
		}
		gameState.highlightedTiles.clear();
	}

	/**
	 * Verifies if a specific coordinate corresponds to a highlighted tile.
	 */
	private boolean isHighlighted(GameState gameState, int x, int y) {
		if (gameState.highlightedTiles == null) return false;
		for (Tile t : gameState.highlightedTiles) {
			if (t.getTilex() == x && t.getTiley() == y) return true;
		}
		return false;
	}

	/**
	 * Calculates valid moves based on Manhattan Distance and Board Obstacles.
	 * Rules:
	 * 1. Orthogonal moves up to 2 steps (cannot pass through units).
	 * 2. Diagonal moves up to 1 step.
	 */
	private List<Tile> calculateValidMoves(GameState gameState, Unit unit) {
		List<Tile> result = new ArrayList<>();
		if (unit == null || unit.getPosition() == null) return result;

		int x = unit.getPosition().getTilex();
		int y = unit.getPosition().getTiley();

		// 1. Orthogonal Moves (Up, Down, Left, Right)
		int[][] dirs = new int[][] { {1,0}, {-1,0}, {0,1}, {0,-1} };
		for (int[] d : dirs) {
			for (int step = 1; step <= 2; step++) {
				int nx = x + d[0] * step;
				int ny = y + d[1] * step;

				// Check Grid Boundaries
				if (nx < 1 || nx > 9 || ny < 1 || ny > 5) break;

				// Check Obstacles (Cannot move through units)
				if (gameState.board[nx - 1][ny - 1] != null) break;

				result.add(BasicObjectBuilders.loadTile(nx, ny));
			}
		}

		// 2. Diagonal Moves (1 step only)
		int[][] diag = new int[][] { {1,1}, {1,-1}, {-1,1}, {-1,-1} };
		for (int[] d : diag) {
			int nx = x + d[0];
			int ny = y + d[1];

			// Check Grid Boundaries
			if (nx < 1 || nx > 9 || ny < 1 || ny > 5) continue;

			// Check Obstacles (Target tile must be empty)
			if (gameState.board[nx - 1][ny - 1] != null) continue;

			result.add(BasicObjectBuilders.loadTile(nx, ny));
		}

		return result;
	}
}