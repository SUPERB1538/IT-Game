package services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.UnitEntity;
import structures.basic.UnitAnimationType;
import structures.AvatarUnit;

/**
 * Single place to remove a unit from game state + UI (Phase 2 Step 2.3).
 */
public class UnitRemovalService {

    private final CommandDispatcher ui = new CommandDispatcher();

    public void removeUnit(ActorRef out, GameState gameState, UnitEntity dead, String reasonNotification) {
        if (out == null || gameState == null || dead == null) return;

        if (dead instanceof AvatarUnit) {
            return;
        }

        // UI: death animation + delete
        BasicCommands.playUnitAnimation(out, dead, UnitAnimationType.death);
        BasicCommands.deleteUnit(out, dead);

        // Backend: remove from board and index
        gameState.getBoard().removeUnit(dead.getPosition());
        gameState.removeUnitById(dead.getId());

        if (reasonNotification != null && !reasonNotification.isEmpty()) {
            ui.notifyP1(out, reasonNotification, 2);
        }
    }
}