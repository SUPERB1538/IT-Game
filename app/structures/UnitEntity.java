package structures;

import structures.basic.ImageCorrection;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationSet;

import java.util.Objects;


public class UnitEntity extends Unit {

    private int maxHealth;
    private int health;
    private int attack;

    // Optional: ownership / controlling player id (keep simple for now)
    private int ownerPlayerId;

    public UnitEntity() {
        super();
    }


    // -----------------------------
    // Per-turn action limits
    // -----------------------------
    private int summonedOnTurn = -1;     // turn number when the unit was summoned
    private int lastTurnMoved = -1;      // turn number when moved
    private int lastTurnAttacked = -1;   // turn number when attacked

    public UnitEntity(int id,
                      UnitAnimationSet animations,
                      ImageCorrection correction,
                      Tile spawnTile,
                      int maxHealth,
                      int attack,
                      int ownerPlayerId) {
        super(id, animations, correction, spawnTile);

        if (maxHealth <= 0) throw new IllegalArgumentException("maxHealth must be > 0");
        if (attack < 0) throw new IllegalArgumentException("attack must be >= 0");

        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.ownerPlayerId = ownerPlayerId;

    }

    // -----------------------------
    // Basic stats
    // -----------------------------

    public void setMaxHealth(int maxHealth) {
        if (maxHealth <= 0) throw new IllegalArgumentException("maxHealth must be > 0");
        this.maxHealth = maxHealth;
        if (health > maxHealth) health = maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        // clamp
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        if (attack < 0) throw new IllegalArgumentException("attack must be >= 0");
        this.attack = attack;
    }

    public int getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public void setOwnerPlayerId(int ownerPlayerId) {
        this.ownerPlayerId = ownerPlayerId;
    }

    public void applyDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("damage must be >= 0");
        setHealth(this.health - amount);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void moveTo(Tile tile) {
        Objects.requireNonNull(tile, "tile cannot be null");
        setPositionByTile(tile);
    }

    public void setSummonedOnTurn(int turnNumber) {
        this.summonedOnTurn = turnNumber;
    }

    public boolean isSummonedThisTurn(int currentTurn) {
        return summonedOnTurn == currentTurn;
    }

    public boolean canMove(int currentTurn) {
        // cannot act on summon turn
        if (isSummonedThisTurn(currentTurn)) return false;
        return lastTurnMoved != currentTurn;
    }

    public boolean canAttack(int currentTurn) {
        // cannot act on summon turn
        if (isSummonedThisTurn(currentTurn)) return false;
        return lastTurnAttacked != currentTurn;
    }

    public void markMoved(int currentTurn) {
        this.lastTurnMoved = currentTurn;
    }

    public void markAttacked(int currentTurn) {
        this.lastTurnAttacked = currentTurn;
    }

    public void resetTurnFlags(int currentTurn) {
    }
}