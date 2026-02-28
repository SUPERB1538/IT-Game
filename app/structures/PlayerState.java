package structures;

import structures.basic.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PlayerState {

    private final int playerId; // 1 or 2
    private Player player;

    private Deck deck;
    private Hand hand;

    public PlayerState(int playerId, Player player) {
        this.playerId = playerId;
        this.player = Objects.requireNonNull(player);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = Objects.requireNonNull(player);
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public int getHealth() {
        return player.getHealth();
    }

    public void setHealth(int health) {
        player.setHealth(clamp(health, 0, 20));
    }

    public int getMana() {
        return player.getMana();
    }

    public void setMana(int mana) {
        player.setMana(clamp(mana, 0, 9));
    }

    public boolean canAfford(int manaCost) {
        return getMana() >= manaCost;
    }

    public boolean spendMana(int manaCost) {
        if (manaCost < 0) return false;
        if (!canAfford(manaCost)) return false;
        setMana(getMana() - manaCost);
        return true;
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }


}