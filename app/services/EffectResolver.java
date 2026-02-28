package services;

import akka.actor.ActorRef;
import structures.GameState;
import structures.CardInstance;
import structures.UnitEntity;

/**
 * Phase 2 Step 2.4C:
 * Spell resolution entry point.
 *
 * For now: safe stub (no board changes) to keep project stable.
 * Later: implement per-card effects here.
 */
public class EffectResolver {

    private final CommandDispatcher ui = new CommandDispatcher();

    public void applySpellToUnit(ActorRef out, GameState gameState, CardInstance spellCard, UnitEntity target) {
        if (out == null || gameState == null || spellCard == null || target == null) return;

        ui.notifyP1(out, "Spell played: " + spellCard.getCardKey(), 2);
    }
}