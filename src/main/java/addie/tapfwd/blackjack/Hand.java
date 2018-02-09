package addie.tapfwd.blackjack;

import java.util.List;

import static addie.tapfwd.blackjack.Hand.Status.IN_PROGRESS;

public class Hand {
    private static final int BLACKJACK = 21;

    private List<Card> cards;
    private int value;
    private Status status;

    public Hand(List<Card> cards) {
        this.cards = cards;
        status = IN_PROGRESS;
        setValue();
    }

    public void addCard(Card card) {
        this.cards.add(card);
        setValue();
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getValue() {
        return this.value;
    }

    private void setValue() {
        int value = 0;
        boolean isAce = false;
        for (Card card : cards) {
            Card.Rank rank = card.getRank();
            if (rank == Card.Rank.ACE) {
                isAce = true;
            }
            value += rank.getValue();
        }
        if (isAce && value > BLACKJACK) {
            value -= 10;
        }
        this.value = value;
        if (value == BLACKJACK) {
            setStatus(Status.BLACKJACK);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        WIN(2.0), NATURAL(2.5), LOSS(0.0), PUSH(1.0), IN_PROGRESS(), BLACKJACK(2.0);

        private double multiplier;
        Status() {
        }
        Status(double multiplier) {
            this.multiplier = multiplier;
        }
        public double getMultiplier() {
            return multiplier;
        }
    }

}
