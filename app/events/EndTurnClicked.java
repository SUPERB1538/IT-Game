package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;

/**
 * Indicates that the user has clicked the end-turn button.
 *
 * {
 *   messageType = "endTurnClicked"
 * }
 *
 * Owner B: Turn system + mana growth/refill + draw + turn lock (minimal AI)
 *
 * @author Dr. Richard McCreadie
 */
public class EndTurnClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Turn lock: ignore if it is not the human's turn
		if (!gameState.humanTurn) {
			BasicCommands.addPlayer1Notification(out, "Not your turn.", 2);
			return;
		}

		// End human turn -> switch to AI
		gameState.switchTurn(); // now humanTurn == false
		BasicCommands.addPlayer1Notification(out, "End Turn. AI turn starts...", 2);

		// Start AI turn: mana grows/refills + draw 1
		Card aiDrawn = gameState.startTurnAndDraw(false);

		// Update UI: mana
		if (gameState.aiPlayer != null) {
			BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
		}
		if (gameState.humanPlayer != null) {
			BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
		}

		// Update UI: show the drawn card if it was added to AI hand
		// playerId: 1 = human, 2 = AI
		if (aiDrawn != null) {
			int pos = gameState.aiHand.size(); // after add, size is the new position (1..6)
			BasicCommands.drawCard(out, aiDrawn, pos, 2);
		}

		// Minimal AI: does nothing, immediately ends its turn
		BasicCommands.addPlayer1Notification(out, "AI ends turn.", 2);

		// Switch back to human
		gameState.switchTurn(); // now humanTurn == true

		// Start human turn: mana grows/refills + draw 1
		Card humanDrawn = gameState.startTurnAndDraw(true);

		// Update UI: mana
		if (gameState.humanPlayer != null) {
			BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
		}
		if (gameState.aiPlayer != null) {
			BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);
		}

		// Update UI: show the drawn card if it was added to human hand
		if (humanDrawn != null) {
			int pos = gameState.humanHand.size(); // after add, size is the new position (1..6)
			BasicCommands.drawCard(out, humanDrawn, pos, 1);
		}

		BasicCommands.addPlayer1Notification(out, "Your turn.", 2);
	}
}