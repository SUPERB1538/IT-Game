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
 * Indicates that the core game loop in the browser is starting, meaning
 * that it is ready to receive commands from the back-end.
 *
 * {
 *   messageType = "initalize"
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

		// 2) Initialize mana state (keep GameState and Player consistent)
		// Start at 0/0 by default; turn start will increase max mana and refill.
		gameState.humanMana = 0;
		gameState.humanMaxMana = 0;
		gameState.aiMana = 0;
		gameState.aiMaxMana = 0;

		// Sync into Player objects so setPlayer*Mana(out, player) displays correctly.
		// (GameState.startTurnAndDraw() also syncs mana every turn start.)
		gameState.startTurn(false); // This would draw a card, so do NOT call it here.
		// Instead, just call the UI setters after the players are created.
		// BasicCommands will read current mana from Player; if Player defaults to 0 mana, this is fine.

		// 3) Health/Mana UI
		BasicCommands.setPlayer1Health(out, gameState.humanPlayer);
		BasicCommands.setPlayer1Mana(out, gameState.humanPlayer);
		BasicCommands.setPlayer2Health(out, gameState.aiPlayer);
		BasicCommands.setPlayer2Mana(out, gameState.aiPlayer);

		// 4) Draw 9x5 board
		for (int x = 1; x <= 9; x++) {
			for (int y = 1; y <= 5; y++) {
				Tile tile = BasicObjectBuilders.loadTile(x, y);
				BasicCommands.drawTile(out, tile, 0);
			}
		}

		// 5) Draw Avatars (Human: 2,3; AI: 8,3)
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

		// 6) Store GameState avatars and coordinates
		gameState.humanAvatar = humanAvatar;
		gameState.aiAvatar = aiAvatar;

		gameState.humanAvatarX = hx;
		gameState.humanAvatarY = hy;

		gameState.aiAvatarX = ax;
		gameState.aiAvatarY = ay;

		// 7) Store board occupancy
		gameState.board[hx - 1][hy - 1] = humanAvatar;
		gameState.board[ax - 1][ay - 1] = aiAvatar;

		// 8) Load decks
		gameState.humanDeck.clear();
		gameState.aiDeck.clear();
		gameState.humanHand.clear();
		gameState.aiHand.clear();

		gameState.humanDeck.addAll(OrderedCardLoader.getPlayer1Cards(1));
		gameState.aiDeck.addAll(OrderedCardLoader.getPlayer2Cards(1));

		// 9) Human draws 3 cards (and display them)
		for (int i = 0; i < 3; i++) {
			if (gameState.humanDeck.isEmpty()) break;
			Card card = gameState.humanDeck.remove(0);
			gameState.humanHand.add(card);

			// playerId should match your template convention: 1 = Human, 2 = AI
			BasicCommands.drawCard(out, card, i + 1, 1);
		}

		// 10) AI draws 3 cards (not displayed by default)
		for (int i = 0; i < 3; i++) {
			if (gameState.aiDeck.isEmpty()) break;
			Card card = gameState.aiDeck.remove(0);
			gameState.aiHand.add(card);

			// If your rules require AI hand to be displayed, uncomment:
			// BasicCommands.drawCard(out, card, i + 1, 2);
		}

		BasicCommands.addPlayer1Notification(out, "Game Started", 2);
	}
}