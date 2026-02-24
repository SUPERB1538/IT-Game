package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;

import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;

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
public class Initalize implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Prevent duplicate initialization
		if (gameState.gameInitialised) return;
		gameState.gameInitialised = true;

		// 1) Initialize both players
		gameState.humanPlayer = new Player();
		gameState.aiPlayer = new Player();

		// 2) Health/Mana
		BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
		BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
		BasicCommands.setPlayer2Health(out, gameState.aiPlayer);
		BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);

		// 3) 9x5 Board
		for (int x = 1; x <= 9; x++) {
			for (int y = 1; y <= 5; y++) {
				Tile tile = BasicObjectBuilders.loadTile(x, y);
				BasicCommands.drawTile(out, tile, 0);
			}
		}

		// 4) Draw Avatar（Human：2,3；AI：8,3）
		int hx = 2, hy = 3;
		Tile humanTile = BasicObjectBuilders.loadTile(hx, hy);
		Unit humanAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 1, Unit.class);
		humanAvatar.setPositionByTile(humanTile);
		BasicCommands.drawUnit(out, humanAvatar, humanTile);
		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		BasicCommands.setUnitHealth(out, humanAvatar, gameState.humanPlayer.getHealth());
		BasicCommands.setUnitAttack(out, humanAvatar, 2);

		int ax = 8, ay = 3;
		Tile aiTile = BasicObjectBuilders.loadTile(ax, ay);
		Unit aiAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 2, Unit.class);
		aiAvatar.setPositionByTile(aiTile);
		BasicCommands.drawUnit(out, aiAvatar, aiTile);
		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		BasicCommands.setUnitHealth(out, aiAvatar, gameState.aiPlayer.getHealth());
		BasicCommands.setUnitAttack(out, aiAvatar, 2);

		// 5) Store GameState
		gameState.humanAvatar = humanAvatar;
		gameState.aiAvatar = aiAvatar;

		gameState.humanAvatarX = hx;
		gameState.humanAvatarY = hy;

		gameState.aiAvatarX = ax;
		gameState.aiAvatarY = ay;

		// 6) Store board
		gameState.board[hx - 1][hy - 1] = humanAvatar;
		gameState.board[ax - 1][ay - 1] = aiAvatar;

		// 7) Load deck
		gameState.humanDeck.clear();
		gameState.aiDeck.clear();
		gameState.humanHand.clear();
		gameState.aiHand.clear();

		gameState.humanDeck.addAll(OrderedCardLoader.getPlayer1Cards(1));
		gameState.aiDeck.addAll(OrderedCardLoader.getPlayer2Cards(1));

		// 8) Human draws 3 cards
		for (int i = 0; i < 3; i++) {
			if (gameState.humanDeck.isEmpty()) break;
			Card card = gameState.humanDeck.remove(0);
			gameState.humanHand.add(card);
			BasicCommands.drawCard(out, card, i + 1, 0);
		}
		// 9) AI cards
		for (int i = 0; i < 3; i++) {
			if (gameState.aiDeck.isEmpty()) break;
			Card card = gameState.aiDeck.remove(0);
			gameState.aiHand.add(card);
		}



		

		BasicCommands.addPlayer1Notification(out, "Game Started", 2);
	}
}