package addie.tapfwd.blackjack;

import java.util.List;

public class Dealer {

    private Card hiddenCard;
    private List<Card> exposedCards;
    private Hand hand;

    public void addCard(Card card) {
        hand.addCard(card);
    }

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
        return getHand().getValue() - getHiddenCard().getRank().getValue();
    }
}
