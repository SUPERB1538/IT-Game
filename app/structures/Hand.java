package structures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Hand {

    public static final int MIN_SLOT = 1;
    public static final int MAX_SLOT = 6;

    private final Map<Integer, CardInstance> slotToCard = new HashMap<>();

    public boolean isSlotValid(int slot) {
        return slot >= MIN_SLOT && slot <= MAX_SLOT;
    }

    public boolean isFull() {
        return slotToCard.size() >= (MAX_SLOT - MIN_SLOT + 1);
    }

    public Optional<Integer> firstEmptySlot() {
        for (int s = MIN_SLOT; s <= MAX_SLOT; s++) {
            if (!slotToCard.containsKey(s)) return Optional.of(s);
        }
        return Optional.empty();
    }

    public CardInstance getBySlot(int slot) {
        return slotToCard.get(slot);
    }

    public boolean putIntoSlot(int slot, CardInstance card) {
        if (!isSlotValid(slot) || card == null) return false;
        if (slotToCard.containsKey(slot)) return false;
        slotToCard.put(slot, card);
        return true;
    }

    public CardInstance removeFromSlot(int slot) {
        return slotToCard.remove(slot);
    }

    public Map<Integer, CardInstance> view() {
        return java.util.Collections.unmodifiableMap(slotToCard);
    }
}