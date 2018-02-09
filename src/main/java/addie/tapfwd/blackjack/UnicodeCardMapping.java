package addie.tapfwd.blackjack;

import java.util.HashMap;
import java.util.Map;

public class UnicodeCardMapping {

    private static final Map<Card, String> map = new HashMap<>();
    static {
        map.put(new Card(Card.Suit.SPADE, Card.Rank.ACE), "\u1F0A1");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.TWO), "\u1F0A2");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.THREE), "\u1F0A3");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.FOUR), "\u1F0A4");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.FIVE), "\u1F0A5");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.SIX), "\u1F0A6");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.SEVEN), "\u1F0A7");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.EIGHT), "\u1F0A8");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.NINE), "\u1F0A9");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.TEN), "\u1F0AA");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.JACK), "\u1F0AB");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.QUEEN), "\u1F0AD");
        map.put(new Card(Card.Suit.SPADE, Card.Rank.KING), "\u1F0AE");

        map.put(new Card(Card.Suit.HEART, Card.Rank.ACE), "\u1F0B1");
        map.put(new Card(Card.Suit.HEART, Card.Rank.TWO), "\u1F0B2");
        map.put(new Card(Card.Suit.HEART, Card.Rank.THREE), "\u1F0B3");
        map.put(new Card(Card.Suit.HEART, Card.Rank.FOUR), "\u1F0B4");
        map.put(new Card(Card.Suit.HEART, Card.Rank.FIVE), "\u1F0B5");
        map.put(new Card(Card.Suit.HEART, Card.Rank.SIX), "\u1F0B6");
        map.put(new Card(Card.Suit.HEART, Card.Rank.SEVEN), "\u1F0B7");
        map.put(new Card(Card.Suit.HEART, Card.Rank.EIGHT), "\u1F0B8");
        map.put(new Card(Card.Suit.HEART, Card.Rank.NINE), "\u1F0B9");
        map.put(new Card(Card.Suit.HEART, Card.Rank.TEN), "\u1F0BA");
        map.put(new Card(Card.Suit.HEART, Card.Rank.JACK), "\u1F0BB");
        map.put(new Card(Card.Suit.HEART, Card.Rank.QUEEN), "\u1F0BD");
        map.put(new Card(Card.Suit.HEART, Card.Rank.KING), "\u1F0BE");

        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.ACE), "\u1F0C1");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.TWO), "\u1F0C2");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.THREE), "\u1F0C3");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.FOUR), "\u1F0C4");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.FIVE), "\u1F0C5");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.SIX), "\u1F0C6");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.SEVEN), "\u1F0C7");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.EIGHT), "\u1F0C8");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.NINE), "\u1F0C9");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.TEN), "\u1F0CA");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.JACK), "\u1F0CB");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.QUEEN), "\u1F0CD");
        map.put(new Card(Card.Suit.DIAMOND, Card.Rank.KING), "\u1F0CE");

        map.put(new Card(Card.Suit.CLUB, Card.Rank.ACE), "\u1F0D1");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.TWO), "\u1F0D2");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.THREE), "\u1F0D3");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.FOUR), "\u1F0D4");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.FIVE), "\u1F0D5");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.SIX), "\u1F0D6");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.SEVEN), "\u1F0D7");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.EIGHT), "\u1F0D8");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.NINE), "\u1F0D9");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.TEN), "\u1F0DA");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.JACK), "\u1F0DB");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.QUEEN), "\u1F0DD");
        map.put(new Card(Card.Suit.CLUB, Card.Rank.KING), "\u1F0DE");
    }

    public static String getCard(Card card) {
        return map.get(card);
    }
}
