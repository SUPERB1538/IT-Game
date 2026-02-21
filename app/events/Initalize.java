package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;

import commands.BasicCommands;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Tile;
import utils.BasicObjectBuilders;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		if (gameState.gameInitialised) return;

		// 1) Draw 9x5 board
		for (int x = 1; x <= 9; x++) {
			for (int y = 1; y <= 5; y++) {
				Tile tile = BasicObjectBuilders.loadTile(x, y);
				BasicCommands.drawTile(out, tile, 0);
			}
		}

		// 2) Players
		gameState.humanPlayer = new Player();
		gameState.humanPlayer.setHealth(20);
		gameState.humanPlayer.setMana(0);

		gameState.aiPlayer = new Player();
		gameState.aiPlayer.setHealth(20);
		gameState.aiPlayer.setMana(0);

		BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
		BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
		BasicCommands.setPlayer2Health(out, gameState.aiPlayer);
		BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);

		// 3) Mark initialised last
		gameState.gameInitialised = true;
	}

}


