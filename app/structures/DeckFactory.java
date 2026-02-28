package structures;

import java.util.ArrayList;
import java.util.List;

import structures.basic.Card;
import utils.BasicObjectBuilders;


public final class DeckFactory {

    private static int NEXT_CARD_ID = 1;
    private static int nextCardId() {
        return NEXT_CARD_ID++;
    }

    /**
     * Human deck (ordered, 20 cards, 2 copies each).
     */
    public static Deck buildHumanDeck(GameState state) {
        List<CardInstance> list = new ArrayList<>();

        // 2022 set style configs (as in your screenshot)
        addTwo(list, "conf/gameconfs/cards/1_1_c_u_bad_omen.json");
        addTwo(list, "conf/gameconfs/cards/1_2_c_s_hornoftheforsaken.json");
        addTwo(list, "conf/gameconfs/cards/1_3_c_u_gloom_chaser.json");
        addTwo(list, "conf/gameconfs/cards/1_4_c_u_shadow_watcher.json");
        addTwo(list, "conf/gameconfs/cards/1_5_c_s_wraithling_swarm.json");
        addTwo(list, "conf/gameconfs/cards/1_6_c_u_nightsorrow_assassin.json");
        addTwo(list, "conf/gameconfs/cards/1_7_c_u_rock_pulveriser.json");
        addTwo(list, "conf/gameconfs/cards/1_8_c_s_dark_terminus.json");
        addTwo(list, "conf/gameconfs/cards/1_9_c_u_bloodmoon_priestess.json");
        addTwo(list, "conf/gameconfs/cards/1_a1_c_u_shadowdancer.json");

        return new Deck(list);
    }

    public static Deck buildAIDeck(GameState state) {
        // keep it simple for now
        return buildHumanDeck(state);
    }

    private static void addTwo(List<CardInstance> list, String configPath) {
        CardInstance c1 = buildCard(configPath);
        CardInstance c2 = buildCard(configPath);

        if (c1 != null) list.add(c1);
        if (c2 != null) list.add(c2);
    }

    private static CardInstance buildCard(String configPath) {
        try {
            Card visual = BasicObjectBuilders.loadCard(configPath, nextCardId(), Card.class);

            // if config broken, don't crash the game
            if (visual == null) {
                System.err.println("[DeckFactory] loadCard returned null: " + configPath);
                return null;
            }

            // Pull mana from the loaded Card
            int mana = visual.getManacost();

            // Use configPath as cardKey (works fine for identifying the card)
            return new CardInstance(configPath, mana,configPath, visual);

        } catch (Exception e) {
            System.err.println("[DeckFactory] Failed to load card config: " + configPath);
            e.printStackTrace();
            return null;
        }
    }
}