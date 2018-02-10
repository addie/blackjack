package addie.tapfwd.blackjack;

import java.util.Objects;

public class Card {
    private Suit suit;
    private Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    enum Suit {
        HEART("hearts"), CLUB("clubs"), SPADE("spades"), DIAMOND("diamonds");
        private String value;

        Suit(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Ace will initially be 11. If deck value is over 21, and has Ace, sub 10
    enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10), ACE(11);
        private int value;

        Rank(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public Rank getRank() {
        return rank;
    }

    public String toString() {
        return "Card(" + rank + " of " + suit.getValue() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit &&
                rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
}

