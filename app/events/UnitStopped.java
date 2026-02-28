package events;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import structures.GameState;

/**
 * Indicates that a unit instance has stopped moving.
 * This is where the backend board array is officially updated.
 * * {
 * messageType = “unitStopped”
 * id = <unit id>
 * }
 * * @author Dr. Richard McCreadie
 *
 */
public class UnitStopped implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {


	}

}