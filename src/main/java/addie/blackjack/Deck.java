package addie.blackjack;

import java.util.*;

public class Deck {
    private static final int SINGLE_DECK_SIZE = 52;
    private static final int DEFAULT_CASINO_DECKS = 6;

    private int size;
    private List<Card> cards;
    private int cardsLeft;
    private int amountOfDecks = DEFAULT_CASINO_DECKS;

    public Deck(int amountOfDecks) {
        this.amountOfDecks = amountOfDecks;
        this.init();
    }

    public Deck() {
        this.init();
    }

    private void init() {
        this.size = SINGLE_DECK_SIZE * amountOfDecks;
        this.cardsLeft = size;
        this.cards = new ArrayList<>(this.size);
        for (int i = 0; i < amountOfDecks; i++) {
            for (Card.Suit suit : Card.Suit.values()) {
                for (Card.Rank rank : Card.Rank.values()) {
                    cards.add(new Card(suit, rank));
                }
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(this.cards);
    }

    public Card dealCard() {
        if (cardsLeft > 0) {
            cardsLeft -= 1;
            return cards.get(cardsLeft);
        } else {
            init();
            return dealCard();
        }
    }
}
