package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import services.TurnManager;
import structures.GameState;

public class EndTurnClicked implements EventProcessor {

	private final TurnManager turnService = new TurnManager();

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		turnService.onEndTurn(out, gameState, message);
	}
}