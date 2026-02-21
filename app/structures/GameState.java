package structures;

import java.util.ArrayList;
import java.util.List;

import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Unit;
/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
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

	// Board (9x5 grid)
	public Unit[][] board = new Unit[9][5];

	// Decks and Hands
	public List<Card> humanDeck = new ArrayList<>();
	public List<Card> humanHand = new ArrayList<>();
	public List<Card> aiDeck = new ArrayList<>();
	public List<Card> aiHand = new ArrayList<>();

	// Selection state
	public Unit selectedUnit = null;
	public Card selectedCard = null;
	
}
