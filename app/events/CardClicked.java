package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a card.
 * The event returns the position in the player's hand the card resides within.
 *
 * {
 *   messageType = "cardClicked"
 *   position = <hand index position [1-6]>
 * }
 *
 * Owner B: Turn lock for card interactions
 *
 * @author Dr. Richard McCreadie
 */
public class CardClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Turn lock: ignore all card interactions when it is not the human's turn
		if (!gameState.isHumanActionAllowed()) {
			BasicCommands.addPlayer1Notification(out, "It's not your turn.", 2);
			return;
		}

		// Read hand position (1-6)
		int handPosition = message.get("position").asInt();

		// Safety check: ensure within expected range
		if (handPosition < 1 || handPosition > GameState.HAND_LIMIT) {
			BasicCommands.addPlayer1Notification(out, "Invalid card position.", 2);
			return;
		}

		// Minimal implementation:
		// Store selected card instance (if you maintain selectedCard)
		// Note: hands are 0-indexed in Java lists
		if (gameState.humanHand != null && handPosition <= gameState.humanHand.size()) {
			gameState.selectedCard = gameState.humanHand.get(handPosition - 1);
			BasicCommands.addPlayer1Notification(out, "Card Selected", 2);
		} else {
			BasicCommands.addPlayer1Notification(out, "No card in that slot.", 2);
		}
	}
}