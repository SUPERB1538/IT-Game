package structures;

import java.util.ArrayList;
import java.util.List;

import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Unit;
import structures.basic.Tile; // Added import for Tile class

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	// Game flags
	public boolean gameInitialised = false;
	public boolean humanTurn = true;
	public int turnNumber = 1;

	// Players
	public Player humanPlayer;
	public Player aiPlayer;

	public Unit humanAvatar;
	public Unit aiAvatar;
	public int humanAvatarX, humanAvatarY;
	public int aiAvatarX, aiAvatarY;

	// Board (9x5 grid)
	public Unit[][] board = new Unit[9][5];

	// Decks and Hands
	public List<Card> humanDeck = new ArrayList<>();
	public List<Card> humanHand = new ArrayList<>();
	public List<Card> aiDeck = new ArrayList<>();
	public List<Card> aiHand = new ArrayList<>();

	// --- Selection state (Extended for Section C) ---
    
    // Tracks the currently selected unit, tile, or card
	public Unit selectedUnit = null;
    public Tile selectedTile = null; // Added: Tracks the tile where the selected unit resides
	public Card selectedCard = null;

    // --- Interaction & Movement State (Added for Section C) ---

    // List to keep track of tiles that are currently highlighted in the UI
	public List<Tile> highlightedTiles = new ArrayList<>();

	// The unit instance that is currently performing a move animation
	public Unit movingUnit = null;

	// Origin coordinates of the moving unit (1-indexed)
	public int moveFromX = -1;
	public int moveFromY = -1;

	// Destination coordinates of the moving unit (1-indexed)
	public int moveToX = -1;
	public int moveToY = -1;
    
}