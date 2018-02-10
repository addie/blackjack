package addie.tapfwd.blackjack;

import java.util.List;

public class Dealer {

    private Hand hand;

    public Card getHiddenCard() {
        return hand.getCards().get(0);
    }

    public List<Card> getExposedCards() {
        return hand.getCards().subList(1, hand.getCards().size());
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public int getExposedValue() {
        int totalValue = 0;
        for (Card card : getHand().getCards()) {
            totalValue += card.getRank().getValue();
        }
        return totalValue - getHiddenCard().getRank().getValue();
    }
}
