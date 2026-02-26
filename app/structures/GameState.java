package structures;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * This class stores the state of the ongoing game.
 * It is created and maintained by the GameActor.
 *
 * Includes:
 * - Turn system state
 * - Mana growth/refill (synced into Player for UI commands)
 * - Deck/hand state with draw rules (hand limit 6)
 *
 * @author Dr. Richard McCreadie
 */
public class GameState {

	// =========================
	// Game flags
	// =========================
	public boolean gameInitialised = false;

	// true = human turn, false = AI turn
	public boolean humanTurn = true;

	// Optional turn counter (incremented when a new human turn starts)
	public int turnNumber = 1;

	// =========================
	// Players
	// =========================
	public Player humanPlayer;
	public Player aiPlayer;

	public Unit humanAvatar;
	public Unit aiAvatar;
	public int humanAvatarX, humanAvatarY;
	public int aiAvatarX, aiAvatarY;

	// =========================
	// Board (9x5 grid)
	// =========================
	public Unit[][] board = new Unit[9][5];

	// =========================
	// Decks and Hands
	// =========================
	public List<Card> humanDeck = new ArrayList<>();
	public List<Card> humanHand = new ArrayList<>();
	public List<Card> aiDeck = new ArrayList<>();
	public List<Card> aiHand = new ArrayList<>();

	// Hand limit and mana rules (adjust according to rule document)
	public static final int HAND_LIMIT = 6;
	public static final int MAX_MANA_CAP = 9;

	// Mana state (kept here for control logic, and synced into Player for UI)
	public int humanMana = 0;
	public int humanMaxMana = 0;
	public int aiMana = 0;
	public int aiMaxMana = 0;

	// =========================
	// --- Selection state (Section C) ---
	// =========================
	public Unit selectedUnit = null;
	public Tile selectedTile = null;
	public Card selectedCard = null;

	// Tiles currently highlighted in the UI
	public List<Tile> highlightedTiles = new ArrayList<>();

	// Movement state (used during animation)
	public Unit movingUnit = null;
	public int moveFromX = -1;
	public int moveFromY = -1;
	public int moveToX = -1;
	public int moveToY = -1;

	// =========================================================
	// Turn system helpers (Owner B)
	// =========================================================

	/** Returns whether the human player is allowed to perform actions. */
	public boolean isHumanActionAllowed() {
		return humanTurn;
	}

	/**
	 * Switches turn from human to AI or AI to human.
	 * Turn number increments when a new human turn begins.
	 */
	public void switchTurn() {
		humanTurn = !humanTurn;
		if (humanTurn) {
			turnNumber += 1;
		}
	}

	/**
	 * Starts a turn for the specified side:
	 * - Increase max mana by 1 up to cap
	 * - Refill current mana to max
	 * - Sync mana into Player objects (for UI commands)
	 * - Draw one card (hand limit 6; if full, card is discarded)
	 * - Clear selection/highlight state
	 *
	 * @return the Card that was successfully added to the hand, or null if none was added
	 */
	public Card startTurnAndDraw(boolean isHuman) {

		// Increase max mana and refill
		if (isHuman) {
			if (humanMaxMana < MAX_MANA_CAP) humanMaxMana++;
			humanMana = humanMaxMana;
			syncPlayerMana(humanPlayer, humanMana, humanMaxMana);
		} else {
			if (aiMaxMana < MAX_MANA_CAP) aiMaxMana++;
			aiMana = aiMaxMana;
			syncPlayerMana(aiPlayer, aiMana, aiMaxMana);
		}

		// Draw one card at start of turn
		Card drawn = drawCardAndReturn(isHuman);

		// Clear selection and highlight state to avoid carry-over issues
		clearSelectionState();

		return drawn;
	}

	/** Backwards-compatible wrapper (kept in case other code calls startTurn). */
	public void startTurn(boolean isHuman) {
		startTurnAndDraw(isHuman);
	}

	/**
	 * Draws one card from deck to hand and returns the card if it was added to hand.
	 * If hand is full (6), the drawn card is discarded (not added).
	 *
	 * @return Card added to hand, or null if none added (deck empty or hand full)
	 */
	public Card drawCardAndReturn(boolean isHuman) {

		List<Card> deck = isHuman ? humanDeck : aiDeck;
		List<Card> hand = isHuman ? humanHand : aiHand;

		if (deck == null || deck.isEmpty()) {
			// Fatigue damage could be implemented here if required by rules
			return null;
		}

		// Remove the top card from deck (index 0 used as deck top)
		Card drawn = deck.remove(0);

		// If hand is full, discard the card
		if (hand.size() >= HAND_LIMIT) {
			return null;
		}

		hand.add(drawn);
		return drawn;
	}

	/** Backwards-compatible wrapper (kept in case other code calls drawCard). */
	public boolean drawCard(boolean isHuman) {
		return drawCardAndReturn(isHuman) != null;
	}

	/**
	 * Clears selected unit, tile, card and highlighted tiles.
	 * Recommended to call on turn switch.
	 */
	public void clearSelectionState() {
		selectedUnit = null;
		selectedTile = null;
		selectedCard = null;
		highlightedTiles.clear();
	}

	// =========================================================
	// Player mana syncing (uses reflection for template compatibility)
	// =========================================================

	/**
	 * Syncs mana fields into a Player instance so BasicCommands.setPlayer*Mana(out, player)
	 * reflects the updated mana on UI.
	 *
	 * This method tries common field/method names to be robust across templates:
	 * - fields: mana, maxMana
	 * - methods: setMana(int), setMaxMana(int)
	 */
	private void syncPlayerMana(Player p, int mana, int maxMana) {
		if (p == null) return;

		// Try setters first
		boolean manaSet = invokeSetter(p, "setMana", mana);
		boolean maxSet = invokeSetter(p, "setMaxMana", maxMana);

		// Try direct fields if setters don't exist
		if (!manaSet) setIntFieldIfExists(p, "mana", mana);
		if (!maxSet) setIntFieldIfExists(p, "maxMana", maxMana);
	}

	private boolean invokeSetter(Object obj, String methodName, int value) {
		try {
			Method m = obj.getClass().getMethod(methodName, int.class);
			m.invoke(obj, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setIntFieldIfExists(Object obj, String fieldName, int value) {
		try {
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.setInt(obj, value);
		} catch (Exception ignored) { }
	}
}