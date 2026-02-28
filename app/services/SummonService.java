package services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.*;
import structures.basic.Position;
import structures.basic.Tile;
import utils.BasicObjectBuilders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creature summoning extracted from GameRulesEngine (Phase 2 Step 2.4A).
 * Keeps behaviour identical to your current summon implementation.
 */
public class SummonService {

    private final CommandDispatcher ui = new CommandDispatcher();

    public boolean trySummonFromSelectedCard(ActorRef out, GameState gameState, Tile targetTile, Position targetPos) {
        if (out == null || gameState == null) return false;

        if (gameState.getCurrentPlayerId() != 1) {
            ui.notifyP1(out, "Not your turn", 2);
            return false;
        }

        Integer handPos = gameState.getSelectedCardPos();
        if (handPos == null) return false;

        PlayerState p1 = gameState.getP1State();
        if (p1 == null || p1.getHand() == null) return false;

        Hand hand = p1.getHand();
        CardInstance card = hand.getBySlot(handPos);
        if (card == null) return false;

        if (!gameState.getBoard().isValidPosition(targetPos) || gameState.getBoard().isOccupied(targetPos)) {
            ui.notifyP1(out, "Invalid target", 2);
            return false;
        }

        int cost = card.getManaCost();
        if (!p1.spendMana(cost)) {
            ui.notifyP1(out, "Not enough mana", 2);
            return false;
        }
        BasicCommands.setPlayer1Mana(out, gameState.getPlayer1());

        // Only creature summon handled here
        if (!isCreatureCard(card)) {
            p1.setMana(Math.min(9, p1.getMana() + cost));
            BasicCommands.setPlayer1Mana(out, gameState.getPlayer1());
            ui.notifyP1(out, "Spell cards not implemented", 2);
            return false;
        }

        String unitConfig = resolveUnitConfig(card.getConfigFile(), card.getCardKey());

        if (unitConfig == null || !fileExists(unitConfig)) {
            ui.notifyP1(out, "Unit config missing for: " + card.getCardKey(), 3);
            return false;
        }

        UnitEntity summoned;
        try {
            summoned = (UnitEntity) BasicObjectBuilders.loadUnit(unitConfig, gameState.nextUnitId(), UnitEntity.class);
        } catch (Exception e) {
            summoned = null;
        }

        if (summoned == null) {
            p1.setMana(Math.min(9, p1.getMana() + cost));
            BasicCommands.setPlayer1Mana(out, gameState.getPlayer1());
            ui.notifyP1(out, "Summon failed (unit config missing)", 2);
            return false;
        }

        int[] stats = creatureStats(card.getCardKey());
        int atk = stats[0];
        int hp = stats[1];

        summoned.setOwnerPlayerId(1);
        summoned.setMaxHealth(hp);
        summoned.setHealth(hp);
        summoned.setAttack(atk);
        summoned.setPositionByTile(targetTile);

        // Summoning sickness tracking (same as before)
        summoned.setSummonedOnTurn(gameState.getGlobalTurnNumber());

        // Put on board + index
        gameState.getBoard().putUnit(targetPos, summoned);
        gameState.addUnit(summoned);

        // Draw unit + stats
        try { Thread.sleep(80); } catch (Exception ignored) {}
        BasicCommands.drawUnit(out, summoned, targetTile);
        try { Thread.sleep(80); } catch (Exception ignored) {}
        BasicCommands.setUnitHealth(out, summoned, summoned.getHealth());
        BasicCommands.setUnitAttack(out, summoned, summoned.getAttack());

        // Remove card + compact hand
        hand.removeFromSlot(handPos);
        compactHandLeft(hand);

        // Redraw hand
        ui.redrawHandNormal(out, gameState);

        ui.notifyP1(out, "Summoned!", 2);
        return true;
    }

    private boolean isCreatureCard(CardInstance card) {
        if (card == null) return false;
        String k = card.getCardKey();
        return k != null && k.contains("_c_u_");
    }

    private int[] creatureStats(String cardKey) {
        if (cardKey == null) return new int[]{1, 1};
        String k = cardKey.toLowerCase();

        if (k.contains("bad_omen")) return new int[]{0, 1};
        if (k.contains("gloom_chaser")) return new int[]{3, 1};
        if (k.contains("shadow_watcher")) return new int[]{3, 2};
        if (k.contains("nightsorrow_assassin")) return new int[]{4, 2};
        if (k.contains("rock_pulveriser")) return new int[]{1, 4};
        if (k.contains("bloodmoon_priestess")) return new int[]{3, 3};
        if (k.contains("shadowdancer")) return new int[]{5, 4};

        return new int[]{1, 1};
    }

    private void compactHandLeft(Hand hand) {
        if (hand == null) return;

        for (int slot = Hand.MIN_SLOT; slot < Hand.MAX_SLOT; slot++) {
            if (hand.getBySlot(slot) != null) continue;

            CardInstance next = hand.getBySlot(slot + 1);
            if (next == null) continue;

            hand.removeFromSlot(slot + 1);
            hand.putIntoSlot(slot, next);

            slot = Math.max(Hand.MIN_SLOT - 1, slot - 1);
        }
    }

    private String resolveUnitConfig(String cardConfigPath, String cardKey) {
        String derived = null;
        if (cardConfigPath != null) {
            derived = cardConfigPath.replace("/cards/", "/units/")
                    .replace("\\cards\\", "\\units\\")
                    .replace("_c_u_", "_u_");
            if (fileExists(derived)) return derived;
        }

        String token = extractToken(cardConfigPath, cardKey);
        if (token == null || token.isEmpty()) return derived;

        String unitsDir = "conf/gameconfs/units";
        File dir = new File(unitsDir);
        if (!dir.exists() || !dir.isDirectory()) return derived;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && name.contains(token));
        if (files != null && files.length > 0) return unitsDir + "/" + files[0].getName();

        return derived;
    }

    private boolean fileExists(String pathStr) {
        if (pathStr == null) return false;
        try {
            Path p = Paths.get(pathStr);
            return Files.exists(p);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractToken(String cardConfigPath, String cardKey) {
        String source = (cardConfigPath != null) ? cardConfigPath : cardKey;
        if (source == null) return null;

        String s = source;
        int slash = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (slash >= 0) s = s.substring(slash + 1);
        if (s.endsWith(".json")) s = s.substring(0, s.length() - 5);

        s = s.replace("c_u_", "").replace("u_", "");

        String[] parts = s.split("_");
        if (parts.length >= 3 && isNumeric(parts[0]) && isNumeric(parts[1])) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (sb.length() > 0) sb.append("_");
                sb.append(parts[i]);
            }
            s = sb.toString();
        }
        return s;
    }

    private boolean isNumeric(String x) {
        if (x == null || x.isEmpty()) return false;
        for (int i = 0; i < x.length(); i++) {
            if (!Character.isDigit(x.charAt(i))) return false;
        }
        return true;
    }
}