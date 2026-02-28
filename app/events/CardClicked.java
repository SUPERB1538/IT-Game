package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import services.GameRulesEngine;
import structures.GameState;

public class CardClicked implements EventProcessor {

	private final GameRulesEngine gameRulesEngine = new GameRulesEngine();

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		gameRulesEngine.onCardClicked(out, gameState, message);
	}
}