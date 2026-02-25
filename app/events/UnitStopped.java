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

		// 1. Extract the unique id of the unit from the event message
		int unitid = message.get("id").asInt();

		// 2. Process the movement completion if a unit was flagged as moving
		if (gameState.movingUnit != null) {

			// 3. Convert GameState 1-indexed coordinates to 0-indexed for array access
			int fx = gameState.moveFromX - 1;
			int fy = gameState.moveFromY - 1;
			int tx = gameState.moveToX - 1;
			int ty = gameState.moveToY - 1;

			// 4. Update the board: clear old position and set the unit in the new position
			gameState.board[fx][fy] = null;
			gameState.board[tx][ty] = gameState.movingUnit;

			// 5. Synchronize the unit's internal position state
			gameState.movingUnit.getPosition().setTilex(gameState.moveToX);
			gameState.movingUnit.getPosition().setTiley(gameState.moveToY);

			// 6. Reset movement bookkeeping fields to allow future moves
			gameState.movingUnit = null;
			gameState.moveFromX = -1;
			gameState.moveFromY = -1;
			gameState.moveToX = -1;
			gameState.moveToY = -1;

			// Debug log to confirm backend synchronization
			System.out.println("DEBUG: Unit " + unitid + " stopped. Board updated from [" + (fx+1) + "," + (fy+1) + "] to [" + (tx+1) + "," + (ty+1) + "]");
		}
	}

}