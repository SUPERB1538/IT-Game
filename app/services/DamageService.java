package services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.AvatarUnit;
import structures.GameState;
import structures.UnitEntity;


public class DamageService {

    private final GameEndChecker gameEndChecker = new GameEndChecker();

    public void dealDamage(ActorRef out, GameState gameState, UnitEntity target, int amount) {
        if (out == null || gameState == null || target == null) return;
        if (amount <= 0) return;

        target.applyDamage(amount);
        BasicCommands.setUnitHealth(out, target, target.getHealth());

        syncIfAvatar(out, gameState, target);

        gameEndChecker.checkAndHandle(out, gameState);
    }

    public void syncIfAvatar(ActorRef out, GameState gameState, UnitEntity unit) {
        if (out == null || gameState == null || unit == null) return;

        AvatarUnit p1 = gameState.getP1Avatar();
        AvatarUnit p2 = gameState.getP2Avatar();

        if (p1 != null && unit.getId() == p1.getId()) {
            gameState.getPlayer1().setHealth(unit.getHealth());
            BasicCommands.setPlayer1Health(out, gameState.getPlayer1());
        }

        if (p2 != null && unit.getId() == p2.getId()) {
            gameState.getPlayer2().setHealth(unit.getHealth());
            BasicCommands.setPlayer2Health(out, gameState.getPlayer2());
        }
    }
}