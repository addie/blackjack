package addie.blackjack;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private List<Hand> hands = new ArrayList<>();
    private int cash;
    private int insurance;
    private String firstName;
    private String lastName;
    int bet;

    public List<Hand> getHands() {
        return hands;
    }

    public void setHands(List<Hand> hands) {
        this.hands = hands;
    }

     public void addHand(Hand hand) {
        this.hands.add(hand);
    }

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }

    public void subCash(int cash) {
        this.cash -= cash;
    }
    public void addCash(int cash) {
        this.cash += cash;
    }

    public String getName() {
        if (lastName != null) {
            return firstName + " " + lastName;
        } else {
            return firstName;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getInsurance() {
        return insurance;
    }

    public void setInsurance(int insurance) {
        this.insurance = insurance;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }
}
