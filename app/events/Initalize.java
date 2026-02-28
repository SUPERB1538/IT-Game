package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import services.GameInitializationService;
import structures.GameState;

public class Initalize implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		new GameInitializationService().initializeGame(out, gameState);
	}
}