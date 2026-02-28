package structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;


public class Deck {

    private final Deque<CardInstance> cards = new ArrayDeque<>();

    public Deck(List<CardInstance> orderedCardsTopToBottom) {
        if (orderedCardsTopToBottom != null) {
            for (CardInstance c : orderedCardsTopToBottom) {
                cards.addLast(c);
            }
        }
    }

    public void shuffle() {
        shuffle(new Random());
    }

    public void shuffle(Random rng) {
        if (cards.size() <= 1) return;

        List<CardInstance> tmp = new ArrayList<>(cards); // keeps current order
        Collections.shuffle(tmp, rng);

        cards.clear();
        for (CardInstance c : tmp) {
            cards.addLast(c);
        }
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public CardInstance drawTop() {
        return cards.pollFirst();
    }
}