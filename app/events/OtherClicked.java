package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * somewhere that is not on a card tile or the end-turn button.
 *
 * {
 *   messageType = "otherClicked"
 * }
 *
 * Owner B: Turn lock + clear selection/highlights
 *
 * @author Dr. Richard McCreadie
 */
public class OtherClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Turn lock: ignore actions when it is not the human's turn
		if (!gameState.isHumanActionAllowed()) {
			BasicCommands.addPlayer1Notification(out, "It's not your turn.", 2);
			return;
		}

		// Human turn: treat as a "deselect / cancel" action
		clearHighlights(out, gameState);

		gameState.selectedUnit = null;
		gameState.selectedTile = null;
		gameState.selectedCard = null;

		BasicCommands.addPlayer1Notification(out, "Selection cleared.", 2);
	}

	/**
	 * Clears all currently highlighted tiles (draw mode 0).
	 */
	private void clearHighlights(ActorRef out, GameState gameState) {
		if (gameState.highlightedTiles == null) return;
		for (Tile t : gameState.highlightedTiles) {
			BasicCommands.drawTile(out, t, 0);
		}
		gameState.highlightedTiles.clear();
	}
}