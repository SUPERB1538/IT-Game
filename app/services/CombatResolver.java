package services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.UnitEntity;
import structures.basic.UnitAnimationType;

/**
 * Attack flow controller (Phase 2 Step 2.3).
 * Behaviour kept identical to GameRulesEngine.tryAttack() from Step 2.2.
 */
public class CombatResolver {

    private final CommandDispatcher ui = new CommandDispatcher();
    private final DamageService damageService = new DamageService();
    private final UnitRemovalService unitRemovalService = new UnitRemovalService();
    private final GameEndChecker gameEndChecker = new GameEndChecker();


    public boolean tryAttack(ActorRef out, GameState gameState, UnitEntity attacker, UnitEntity defender) {
        if (gameState.isGameOver()) return false;
        if (out == null || gameState == null) return false;
        if (attacker == null || defender == null) return false;
        if (attacker.getOwnerPlayerId() == defender.getOwnerPlayerId()) return false;

        // Range: 8-neighbour adjacency
        int ax = attacker.getPosition().getTilex();
        int ay = attacker.getPosition().getTiley();
        int dx = defender.getPosition().getTilex();
        int dy = defender.getPosition().getTiley();

        int diffX = Math.abs(ax - dx);
        int diffY = Math.abs(ay - dy);

        boolean adjacent = (diffX <= 1 && diffY <= 1 && !(diffX == 0 && diffY == 0));
        if (!adjacent) {
            ui.notifyP1(out, "Target out of range", 2);
            return false;
        }

        int t = gameState.getGlobalTurnNumber();
        if (!attacker.canAttack(t)) {
            ui.notifyP1(out, "This unit can't attack again this turn.", 2);
            return false;
        }

        // Attack animation
        BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);

        // Deal attacker -> defender
        damageService.dealDamage(out, gameState, defender, attacker.getAttack());
        attacker.markAttacked(t);

        // Defender death?
        if (defender.isDead()) {
            if (defender instanceof structures.AvatarUnit) {
                gameEndChecker.checkAndHandle(out, gameState);
                return true;
            }

            unitRemovalService.removeUnit(out, gameState, defender, "Unit destroyed");
            BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
            return true;
        }

        // Counterattack
        BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.attack);
        damageService.dealDamage(out, gameState, attacker, defender.getAttack());

        // Attacker death?
        if (attacker.isDead()) {
            if (attacker instanceof structures.AvatarUnit) {
                gameEndChecker.checkAndHandle(out, gameState);
                return true;
            }

            unitRemovalService.removeUnit(out, gameState, attacker, "Unit destroyed");
            if (!defender.isDead()) {
                BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.idle);
            }
            return true;
        }

        // Back to idle
        BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
        BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.idle);
        return true;
    }
}