package services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.*;

public class CardDrawService {

    /**
     * Draw rule:
     * - draw 1 from top of deck
     * - if hand full (6 slots), burn the card (do not add)
     * - only render card UI for human (playerId==1)
     */
    public void drawOneCardAtTurnStart(ActorRef out, GameState gameState, int playerId) {

        if (gameState == null) return;

        PlayerState ps = (playerId == 1) ? gameState.getP1State() : gameState.getP2State();
        Deck deck = ps.getDeck();
        Hand hand = ps.getHand();

        if (deck == null || hand == null) return;

        // deck empty => fixed 1 fatigue damage
        if (deck.isEmpty()) {

            AvatarUnit avatar = (playerId == 1)
                    ? gameState.getP1Avatar()
                    : gameState.getP2Avatar();

            if (avatar != null) {

                avatar.applyDamage(1);
                BasicCommands.setUnitHealth(out, avatar, avatar.getHealth());

                if (playerId == 1) {
                    gameState.getPlayer1().setHealth(avatar.getHealth());
                    BasicCommands.setPlayer1Health(out, gameState.getPlayer1());
                } else {
                    gameState.getPlayer2().setHealth(avatar.getHealth());
                    BasicCommands.setPlayer2Health(out, gameState.getPlayer2());
                }

                BasicCommands.addPlayer1Notification(out, "Fatigue: 1 damage", 2);

                if (avatar.getHealth() <= 0) {
                    BasicCommands.addPlayer1Notification(
                            out,
                            (playerId == 1 ? "You lose!" : "You win!"),
                            3
                    );
                }
            }
            return;
        }

        CardInstance ci = deck.drawTop();
        if (ci == null) return;

        // Hand full => burn
        if (hand.isFull()) return;

        java.util.Optional<Integer> slotOpt = hand.firstEmptySlot();
        if (!slotOpt.isPresent()) return;

        int slot = slotOpt.get();
        boolean ok = hand.putIntoSlot(slot, ci);
        if (!ok) return;

        if (playerId == 1 && !gameState.isHandHidden()) {
            try { Thread.sleep(80); } catch (Exception ignored) {}
            BasicCommands.drawCard(out, ci.getVisual(), slot, 0);
        }
    }
}