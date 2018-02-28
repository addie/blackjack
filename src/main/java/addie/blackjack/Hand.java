package addie.blackjack;

import java.util.ArrayList;
import java.util.List;

import static addie.blackjack.Hand.Status.IN_PROGRESS;

public class Hand {
    private static final int TWENTY_ONE = 21;

    private int id;
    private int bet;
    private List<Card> cards;
    private int value;
    private Status status;

    public Hand(ArrayList<Card> cards) {
        this.id = 1;
        this.cards = cards;
        this.status = IN_PROGRESS;
        setValue();
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public void addBet(int bet) {
        this.bet += bet;
    }

    private void setValue() {
        int value = 0;
        boolean isAce = false;
        for (Card card : cards) {
            Card.Rank rank = card.getRank();
            value += rank.getValue();
            if (rank == Card.Rank.ACE) {
                isAce = true;
            }
        }
        if (isAce && value > TWENTY_ONE) {
            value -= 10;
        }
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public void addCard(Card card) {
        this.cards.add(card);
        setValue();
    }

    public List<Card> getCards() {
        return cards;
    }


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public enum Status {
        WIN(2.0), LOSS(0.0), PUSH(1.0), IN_PROGRESS(), STAND(), BLACKJACK(2.5);

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
