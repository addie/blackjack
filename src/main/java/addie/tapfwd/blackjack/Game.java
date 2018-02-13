package addie.tapfwd.blackjack;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
    Basic Requirements

    The program should start by prompting for the number of players
    at the table. Each player should start with $1000 and should
    be allowed to place any integer bet on each hand.
 */

public class Game {
    private static void println(String s) {
        System.out.println(s);
    }

    private static void print(String s) {
        System.out.print(s);
    }

    private static final int DEFAULT_STARTING_CASH = 1000;
    private static final int DEFAULT_PLAYERS = 1;
    private static final int DEFAULT_DECKS = 6;
    private static final int DEFAULT_MIN_BET = 2;
    private static final int DEFAULT_MAX_BET = 10000;
    private static final int TWENTY_ONE = 21;

    private Scanner in;
    private List<Player> players;
    private Dealer dealer;
    private Deck deck;

    private void play(int startingCash, int numPlayers, int minBet, int maxBet, int numDecks) throws IOException, InterruptedException {

        // Intro
        printSplash();
        println("Welcome to Blackjack! by Addie Bendory");
        println("Number of Players: " + numPlayers + "\nStarting Cash: $" + startingCash);
        print("\n");

        // Create the game components
        in = new Scanner(System.in);
        players = createPlayers(numPlayers, startingCash);
        deck = new Deck(numDecks);
        dealer = new Dealer();

        // Stores the total cash on hand for the game loop
        int totalCash = players.stream().mapToInt(Player::getCash).sum();

        // Game loop
        while (totalCash > 0) {

            showPlayersCashRemaining();
            takeBets(minBet, maxBet);
            dealCards();
            offerInsurance();

            GameStatus status = checkBlackjack();

            // player round
            if (status != GameStatus.COMPLETE) {
                status = playerRound();
            }

            // dealer round
            if (status != GameStatus.COMPLETE) {
                dealerRound();
            }

            // resolve all bets
            resolveBets();

            // remove bankrupt players
            removePlayers(minBet);

            // recalculate cash left
            totalCash = players.stream().mapToInt(Player::getCash).sum();
            if (totalCash > 0) {
                println("Let's play another round!\n");
            }
        }
        println("Game over! Come back when you've got some more money!!");
    }

    // Rounding down the pennies
    private void resolveBets() {
        try {
            int win;
            println("Results of this hand");
            println("--------------------");
            showAllCards(false);
            print("\n");
            for (Player player : players) {
                for (Hand hand : player.getHands()) {
                    Hand.Status status = hand.getStatus();
                    switch (status) {
                        case BLACKJACK:
                            win = (int) (hand.getBet() * Hand.Status.BLACKJACK.getMultiplier());
                            player.addCash(win);
                            println(player.getName() + " got a blackjack!");
                            break;
                        case PUSH:
                            println(player.getName() + " pushed");
                            win = (int) (hand.getBet() * Hand.Status.PUSH.getMultiplier());
                            player.addCash(win);
                            break;
                        case WIN:
                            win = (int) (hand.getBet() * Hand.Status.WIN.getMultiplier());
                            player.addCash(win);
                            println(player.getName() + " won!");
                            break;
                        case LOSS:
                            println(player.getName() + " lost");
                            break;
                        default:
                            throw new IllegalArgumentException("ERROR: Player status is invalid");
                    }
                }
            }
            print("\n");
        } catch (IllegalArgumentException ex) {
            println(ex.getMessage() + "\n");
            System.exit(1);
        }

    }

    private void offerInsurance() {
        Card.Rank dealerCard = dealer.getExposedCards().get(0).getRank();
        // Offering on dealer ACE only but can be changed here
        if (dealerCard == Card.Rank.ACE) {
            for (Player player : players) {
                String input;
                do {
                    showAllCards(true);
                    print("\n");
                    print(player.getName() + ", do you want insurance? > ");
                    input = in.nextLine();
                } while (!validInput(input, new HashSet<>(Arrays.asList("y", "n"))));
                if ("y".equals(input)) {
                    getInsurance(player);
                }
            }
            print("\n");
        }
    }

    private void getInsurance(Player player) {
        String input;
        do {
            print("How much? (You can wager up to $" + player.getHands().get(0).getBet() / 2 + ") > ");
            input = in.nextLine();
        } while (Integer.parseInt(input) > (player.getHands().get(0).getBet() / 2));
        int insurance = Integer.parseInt(input);
        player.subCash(insurance);
        player.setInsurance(insurance);
    }

    private void removePlayers(int minBet) {
        players.removeIf(player -> player.getCash() < minBet);
    }

    private void showPlayersCashRemaining() {
        for (Player player : players) {
            println(player.getName() + " has $" + player.getCash() + " remaining");
        }
        print("\n");
    }

    private void takeBets(int minBet, int maxBet) {
        println("Place your bets!");
        for (Player player : players) {
            if (player.getCash() < minBet) {
                continue;
            }
            int playerMaxBet = Integer.min(maxBet, player.getCash());
            print("Enter bet for " + player.getName() + ": ");
            int bet = validateBet(in.nextLine());
            while (!validBet(bet, minBet, playerMaxBet)) {
                println("Invalid bet. Bet must be between $" + minBet + " and $" + playerMaxBet);
                print("Enter bet for " + player.getName() + ": ");
                bet = validateBet(in.nextLine());
            }
            player.setBet(bet);
            player.subCash(bet);
        }
        println("All bets are in.\n");
    }

    private int validateBet(String input) {
        int bet;
        try {
            bet = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            bet = -1;
        }
        return bet;
    }

    private GameStatus playerRound() {
        println("-- Player's Round --");
        GameStatus gameStatus = GameStatus.COMPLETE;
        for (Player player : players) {
            gameStatus = playHand(gameStatus, player, player.getHands().get(0));
        }
        return gameStatus;
    }

    private GameStatus playHand(GameStatus gameStatus, Player player, Hand hand) {
        // Skip any players that blackjacked
        if (hand.getStatus() != Hand.Status.IN_PROGRESS) {
            return gameStatus;
        }

        // Prompt player
        promptPlayerForMove(player, hand);

        // Get card values to check for double down or split
        List<Card> cards = hand.getCards();
        Card.Rank firstRank = cards.get(0).getRank();
        Card.Rank secondRank = cards.get(1).getRank();
        int value = firstRank.getValue() + secondRank.getValue();

        String move;
        if (playerDoubledDown(player, hand, value)) {
            doubleDown(player, hand);
            gameStatus = GameStatus.IN_PROGRESS;
        } else if (firstRank.equals(secondRank)) {
            do {
                print("(h)it or (s)tand or spli(t)? > ");
                move = in.nextLine().toLowerCase();
            } while (!validInput(move, new HashSet<>(Arrays.asList("h", "s", "t"))));
            gameStatus = processChanceToSplit(gameStatus, player, move);
        } else {
            do {
                print("(h)it or (s)tand? > ");
                move = in.nextLine().toLowerCase();
            } while (!validInput(move, new HashSet<>(Arrays.asList("h", "s"))));
            gameStatus = processPlayerAction(gameStatus, player, hand, move);
        }

        return gameStatus;
    }

    private void doubleDown(Player player, Hand hand) {
        int newBet = hand.getBet();
        hand.addBet(newBet);
        player.subCash(newBet);
        Card card = deck.dealCard();
        hand.addCard(card);
        if (hand.getValue() > TWENTY_ONE) {
            println(player.getName() + " busts!\n");
            hand.setStatus(Hand.Status.LOSS);
        }
    }

    private boolean playerDoubledDown(Player player, Hand hand, int value) {
        String move;
        boolean doubleDown = false;
        if (value >= 9 && value <= 11) {
            do {
                print("Double down? (y) or (n) > ");
                move = in.nextLine();
            } while (!validInput(move, new HashSet<>(Arrays.asList("y", "n"))));
            if (player.getCash() < hand.getBet()) {
                println("Not enough cash!!");
            } else if ("y".equals(move)) {
                doubleDown = true;
                hand.setStatus(Hand.Status.STAND);
            }
            print("\n");
        }
        return doubleDown;
    }

    private boolean validInput(String input, Set<String> validInput) {
        return validInput.contains(input.toLowerCase());
    }

    private void promptPlayerForMove(Player player, Hand hand) {
        showAllCards(player.getName(), hand.getId(),  true);
        println("\n" + player.getName() + "'s move.");
    }

    private GameStatus processPlayerAction(GameStatus gameStatus, Player player, Hand hand, String move) {
        // We split to get here, so just prompt again
        if ("t".equals(move)) {
            promptPlayerForMove(player, hand);
            print("(h)it or (s)tand? > ");
            move = in.nextLine();
        }
        // loop while player doesn't stand or bust
        while ("h".equals(move)) {
            hand.addCard(deck.dealCard());
            if (hand.getValue() > TWENTY_ONE) {
                println(player.getName() + " busts!\n");
                hand.setStatus(Hand.Status.LOSS);
                break;
            }
            promptPlayerForMove(player, hand);
            print("(h)it or (s)tand? > ");
            move = in.nextLine();
        }
        if (Hand.Status.IN_PROGRESS == hand.getStatus()) {
            println(player.getName() + " stands!\n");
            player.getHands().get(0).setStatus(Hand.Status.STAND);
            gameStatus = GameStatus.IN_PROGRESS;
        }
        return gameStatus;
    }

    private GameStatus processChanceToSplit(GameStatus gameStatus, Player player, String move) {
        // handle the split here
        if ("t".equals(move)) {
            if (player.getCash() >= player.getHands().get(0).getBet()) {
                player = splitHands(player);
                for (Hand hand : player.getHands()) {
                    gameStatus = processPlayerAction(gameStatus, player, hand, move);
                }
            } else {
                println("Not enough cash to split.");
                do {
                    print("(h)it or (s)tand? > ");
                    move = in.nextLine().toLowerCase();
                } while (!validInput(move, new HashSet<>(Arrays.asList("h", "s"))));
                Hand firstHand = player.getHands().get(0);
                gameStatus = processPlayerAction(gameStatus, player, firstHand, move);
            }
        } else {
            Hand firstHand = player.getHands().get(0);
            gameStatus = processPlayerAction(gameStatus, player, firstHand, move);
        }
        return gameStatus;
    }

    private Player splitHands(Player player) {
        // get orig
        Hand original = player.getHands().get(0);
        Card first = original.getCards().get(0);
        Card second = original.getCards().get(1);

        // create new hands
        int bet = original.getBet();
        Hand hand1 = new Hand(new ArrayList<>(Arrays.asList(first, deck.dealCard())));
        hand1.setId(1);
        hand1.setBet(bet);
        Hand hand2 = new Hand(new ArrayList<>(Arrays.asList(second, deck.dealCard())));
        hand2.setId(2);
        hand2.setBet(bet);
        player.subCash(bet);
        player.setHands(new ArrayList<>(Arrays.asList(hand1, hand2)));

        return player;
    }

    private void dealerRound() throws InterruptedException {
        println("-- Dealer's Round --");
        int value;
        do {
            showAllCards(false);
            value = dealer.getHand().getValue();
            if (value <= 16) {
                println("Dealer hits.\n");
                dealer.getHand().addCard(deck.dealCard());
            } else if (value <= 21) {
                println("Dealer stands.\n");
            } else {
                println("Dealer busts!\n");
                dealer.getHand().setStatus(Hand.Status.LOSS);
            }
            TimeUnit.SECONDS.sleep(1);
        } while (value <= 16);
        setFinalHandStatus();
    }

    private void setFinalHandStatus() {
        for (Player player : players) {
            for (Hand hand : player.getHands()) {
                Hand dealerHand = dealer.getHand();
                if (Hand.Status.IN_PROGRESS == hand.getStatus() ||
                      Hand.Status.STAND == hand.getStatus()) {
                    int dealerValue = dealer.getHand().getValue();
                    int playerValue = hand.getValue();
                    if (dealerValue < playerValue ||
                         dealerHand.getStatus() == Hand.Status.LOSS) {
                        hand.setStatus(Hand.Status.WIN);
                    } else if (dealerValue > playerValue) {
                        hand.setStatus(Hand.Status.LOSS);
                    } else {
                        hand.setStatus(Hand.Status.PUSH);
                    }
                }
            }
        }
    }

    private void showAllCards(boolean dealerHidden) {
        showAllCards(StringUtils.EMPTY, 1, dealerHidden);
    }

    private void showAllCards(String name, int activeHand, boolean dealerHidden) {
        showDealerCards(dealerHidden);
        for (Player player : players) {
            for (Hand hand : player.getHands()) {
                if (name.equals(player.getName()) && activeHand == hand.getId()) {
                    showPlayerCards(player, hand,"*");
                } else {
                    showPlayerCards(player, hand, StringUtils.EMPTY);
                }
            }
        }
    }

    private void showDealerCards(boolean cardHidden) {
        if (cardHidden) {
            print("Dealer shows: " + dealer.getExposedValue() + " ");
            StringBuilder sb = new StringBuilder();
            String hiddenCard = sb.appendCodePoint(0x1F0A0).toString();
            print(hiddenCard + " ");
            for (Card card : dealer.getExposedCards())
                print(UnicodeCardMapping.getCard(card) + " ");
        } else {
            print("Dealer: " + dealer.getHand().getValue() + " ");
            for (Card card : dealer.getHand().getCards())
                print(UnicodeCardMapping.getCard(card) + " ");
        }
        print("\n");
    }

    private void showPlayerCards(Player player, Hand hand, String prefix) {
        if (!StringUtils.EMPTY.equals(prefix)) {
            prefix += " ";
        }
        print(prefix + player.getName() + ": " + hand.getValue() + " ");
        for (Card card : hand.getCards()) {
            print(UnicodeCardMapping.getCard(card) + " ");
        }
        print("\n");
    }

    /*
    via bicyclecards.com
    If a player's first two cards are an ace and a "ten-card" (a picture card or 10),
    giving him a count of 21 in two cards, this is a natural or "blackjack."
    If any player has a natural and the dealer does not, the dealer immediately pays
    that player one and a half times the amount of his bet. If the dealer has a natural,
    he immediately collects the bets of all players who do not have naturals,
    (but no additional amount). If the dealer and another player both have naturals,
    the bet of that player is a stand-off (a tie), and the player takes back his chips.
     */
    private GameStatus checkBlackjack() {
        Hand dealerHand = dealer.getHand();
        GameStatus status = GameStatus.IN_PROGRESS;
        if (dealerHand.getValue() == TWENTY_ONE) {
            dealerHand.setStatus(Hand.Status.BLACKJACK);
            println("\n *** Dealer Blackjack! *** \n");
            status = GameStatus.COMPLETE;
        }
        for (Player player : players) {
            int insurance = player.getInsurance();
            if (insurance > 0 && Hand.Status.BLACKJACK == dealerHand.getStatus()) {
                println(player.getName() + " wins insurance bet!");
                player.addCash(insurance * 3);
            }
            Hand playerHand = player.getHands().get(0);
            if (playerHand.getValue() == TWENTY_ONE) {
                if (dealerHand.getStatus() == Hand.Status.BLACKJACK) {
                    playerHand.setStatus(Hand.Status.PUSH);
                    println(player.getName() + " has a blackjack\n but so does the dealer so player pushes!\n");
                    showAllCards(false);
                    print("\n");
                } else {
                    playerHand.setStatus(Hand.Status.BLACKJACK);
                    println(player.getName() + " has a blackjack! $$$\n");
                    showAllCards(true);
                    print("\n");
                }
            } else if (dealerHand.getStatus() == Hand.Status.BLACKJACK) {
                playerHand.setStatus(Hand.Status.LOSS);
                println(player.getName() + " loses this hand.\n");
                showAllCards(false);
                print("\n");
            }
        }
        return status;
    }

    private boolean validBet(int bet, int minBet, int maxBet) {
        return bet >= minBet && bet <= maxBet;
    }

    private void dealCards() {
        println("Dealing cards...\n");
        for (Player player : players) {
            Hand hand = new Hand(new ArrayList<>(Arrays.asList(deck.dealCard(), deck.dealCard())));
            hand.setBet(player.getBet());
            player.setHands(Collections.singletonList(hand));
        }
        Hand hand = new Hand(new ArrayList<>(Arrays.asList(deck.dealCard(), deck.dealCard())));
        dealer.setHand(hand);
    }

    private List<Player> createPlayers(int numOfPlayers, int cash) {
        List<Player> players = new ArrayList<>();
        Scanner in = new Scanner(System.in);
        for (int i = 1; i <= numOfPlayers; i++) {
            Player player = new Player();
            player.setCash(cash);
            String name;
            do {
                print("Enter name for Player " + i + ": ");
                name = in.nextLine();
            } while (StringUtils.isEmpty(name));
            String[] names = name.split(" ");
            String firstName = names[0];
            player.setFirstName(firstName);
            if (names.length > 1) {
                String lastName = names[1];
                player.setLastName(lastName);
            }
            players.add(player);
        }
        return players;
    }

    private void printSplash() throws IOException {
        InputStream inputStream = Game.class.getResourceAsStream("/splashscreen.txt");
        println(IOUtils.toString(inputStream, "utf-8"));
    }

    // see https://commons.apache.org/proper/commons-cli/usage.html
    public static void main(String[] args) throws IOException, InterruptedException {
        // create the command line parser and formatter
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "help");
        options.addOption("p", "players", true, "number of players");
        options.addOption("c", "cash", true, "starting cash for players");
        options.addOption("m", "minbet", true, "minimum bet");
        options.addOption("M", "maxbet", true, "maximum bet");
        options.addOption("d", "decks", true, "number of decks");

        try {
            int startingCash = DEFAULT_STARTING_CASH;
            int numberOfPlayers = DEFAULT_PLAYERS;
            int minBet = DEFAULT_MIN_BET;
            int maxBet = DEFAULT_MAX_BET;
            int decks = DEFAULT_DECKS;
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                // print help
                formatter.printHelp("blackjack", options);
                System.exit(0);
            }
            if (cmd.hasOption("c")) {
                startingCash = Integer.parseInt(cmd.getOptionValue("cash"));
                if (startingCash < 100 || startingCash > 10000) {
                    throw new IllegalArgumentException("Starting cash must be between $10 and $10,000");
                }
            }
            if (cmd.hasOption("p")) {
                numberOfPlayers = Integer.parseInt(cmd.getOptionValue("players"));
                if (numberOfPlayers < 1) {
                    throw new IllegalArgumentException("Must have at least 1 player");
                }
                if (numberOfPlayers > 9) {
                    throw new IllegalArgumentException("Cannot have over 9 players");
                }
            }
            if (cmd.hasOption("m")) {
                minBet = Integer.parseInt(cmd.getOptionValue("minbet"));
            }
            if (cmd.hasOption("M")) {
                maxBet = Integer.parseInt(cmd.getOptionValue("maxbet"));
            }
            if (maxBet - minBet < 10) {
                throw new IllegalArgumentException("Max bet must be at least $10 more than min bet.");
            }
            if (cmd.hasOption("d")) {
                decks = Integer.parseInt(cmd.getOptionValue("decks"));
                if (decks < 1 || decks > 10) {
                    throw new IllegalArgumentException("Number of decks must be between 1 and 10");
                }
            }

            Game newGame = new Game();
            newGame.play(startingCash, numberOfPlayers, minBet, maxBet, decks);

        } catch (NumberFormatException numberFormatException) {
            System.out.print("Arguments must be integers");
        } catch (MissingArgumentException missingArgumentException) {
            println(missingArgumentException.getMessage());
        } catch (ParseException parseException) {
            println(parseException.getMessage());
            formatter.printHelp("blackjack", options);
        } finally {
            print("\n\n");
        }
    }
}
