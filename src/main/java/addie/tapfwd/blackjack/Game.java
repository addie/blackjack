package addie.tapfwd.blackjack;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.SocketHandler;
import java.util.stream.Collectors;

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

    private void play(int startingCash, int numPlayers, int minBet, int maxBet, int numDecks) throws IOException {

        // Intro
        printSplash();
        println("Welcome to Blackjack!");
        println("Number of Players: " + numPlayers + "\nStarting Cash: $" + startingCash);
        print("\n");
        Scanner in = new Scanner(System.in);

        // Create the game components
        List<Player> players = createPlayers(numPlayers, startingCash);
        Dealer dealer = new Dealer();
        Deck deck = new Deck(numDecks);

        // Stores the total cash on hand for the game loop
        int totalCash = players.stream().mapToInt(Player::getCash).sum();

        // Game loop
        while (totalCash > 0) {
            GameStatus status;
            println("Ladies and gentlemen! Place your bets!");
            println("(Press Ctrl-C to exit)");
            for (Player player : players) {
                takeBet(player, minBet, maxBet);
            }
            println("\nAll bets are in.\n");
            println("Dealing cards...");
            dealCards(deck, players, dealer);
            status = checkNaturals(players, dealer);
            if (status == GameStatus.COMPLETE) {
                println("Hand is over!");
                resolveBets(players);
                continue;
            }
            status = playerRound(deck, players, dealer);
            if (status == GameStatus.COMPLETE) {
                println("Hand is over!");
                resolveBets(players);
                continue;
            }
            dealerRound(deck, dealer, players);
            resolveBets(players);
        }

        println("Game over! Come back when you've got some more money!!");
    }

    // Rounding down the pennies
    private void resolveBets(List<Player> players) {
        try {
            int win = 0;
            for (Player player : players) {
                Hand.Status status = player.getHand().getStatus();
                switch (status) {
                    case NATURAL:
                        win = (int)(player.getBet() * Hand.Status.NATURAL.getMultiplier());
                        player.addCash(win);
                        println(player.getName() + " won big!");
                        break;
                    case PUSH:
                        println(player.getName() + " pushed");
                        win = (int)(player.getBet() * Hand.Status.PUSH.getMultiplier());
                        player.addCash(win);
                        break;
                    case WIN:
                    case BLACKJACK:
                        win = (int)(player.getBet() * Hand.Status.WIN.getMultiplier());
                        player.addCash(win);
                        println(player.getName() + " won!");
                        break;
                    case LOSS:
                        println(player.getName() + " lost");
                        break;
                    default:
                        throw new IllegalArgumentException("Status is invalid");
                }
            }
        } catch (IllegalArgumentException ex) {
            println(ex.getMessage());
        }

    }

    private void takeBet(Player player, int minBet, int maxBet) {
        Scanner in = new Scanner(System.in);
        print("Enter bet for " + player.getName() + ": ");
        Integer bet = Integer.parseInt(in.nextLine());
        while (!validBet(bet, minBet, maxBet)) {
            println("Invalid bet. Bet must be between $" + minBet + " and $" + maxBet);
            print("Enter bet for " + player.getName() + ": ");
            bet = Integer.parseInt(in.nextLine());
        }
        player.setBet(bet);
    }

    private GameStatus playerRound(Deck deck, List<Player> players, Dealer dealer) {
        println("\n-- Player's Round --");
        Scanner in = new Scanner(System.in);
        GameStatus gameStatus = GameStatus.COMPLETE;
        for (Player player : players) {
            if (player.getHand().getStatus() != Hand.Status.IN_PROGRESS) {
                continue;
            }
            showDealerCards(dealer, true);
            showAllPlayerCards(players);
            String move;
            do {
                print(player.getName() + "(h)it or (s)tand? > ");
                move = in.nextLine();
                if ("h".equals(move)) {
                    player.getHand().addCard(deck.dealCard());
                    if (player.getHand().getValue() > TWENTY_ONE) {
                        println(player.getName() + " busts!\n");
                        player.getHand().setStatus(Hand.Status.LOSS);
                        break;
                    }
                } else if (!"s".equals(move)) {
                    println("Invalid input. Try again.");
                }
            } while (!"s".equals(move));
            println(player.getName() + " stands!\n");
            if (Hand.Status.IN_PROGRESS == player.getHand().getStatus()) {
                gameStatus = GameStatus.IN_PROGRESS;
            }
        }
        return gameStatus;
    }

    private void dealerRound(Deck deck, Dealer dealer, List<Player> players) {
        println("\n-- Dealer's Round --");
        int value;
        do {
            showDealerCards(dealer, false);
            showAllPlayerCards(players);
            value = dealer.getHand().getValue();
            if (value <= 16) {
                println("Dealer shows " + value + ". Dealer hits.");
                dealer.getHand().addCard(deck.dealCard());
                showDealerCards(dealer, false);
            } else {
                if (value > 21) {
                    println("Dealer shows " + value + ". Dealer busts!");
                    dealer.getHand().setStatus(Hand.Status.LOSS);
                } else {
                    println("Dealer shows " + value + ". Dealer stands.");
                    showDealerCards(dealer, false);
                }
            }
        } while (value <= 16);
    }

    private void showAllPlayerCards(List<Player> players) {
        for (Player player : players) {
            showCards(player, null, false);
        }
    }

    private void showPlayerCards(Player player) {
        showCards(player, null, false);
    }

    private void showDealerCards(Dealer dealer, boolean dealerHide) {
        showCards(null, dealer, dealerHide);
    }

    private void showCards(Player player, Dealer dealer, boolean dealerHide) {
        if (dealer != null) {
            if (dealerHide) {
                print("Dealer shows: " + dealer.getExposedValue() + " ");
                for (Card card : dealer.getExposedCards())
                    print(UnicodeCardMapping.getCard(card) + " ");
            } else {
                print("Dealer: " + dealer.getHand().getValue() + " ");
                for (Card card : dealer.getHand().getCards())
                    print(UnicodeCardMapping.getCard(card) + " ");
            }
        }
        if (player != null) {
            print(player.getName() + ": " + player.getHand().getValue() + " ");
            for (Card card : player.getHand().getCards())
                print(UnicodeCardMapping.getCard(card) + " ");
        }
        print("\n");
    }

    /*
    If a player's first two cards are an ace and a "ten-card" (a picture card or 10),
    giving him a count of 21 in two cards, this is a natural or "blackjack."
    If any player has a natural and the dealer does not, the dealer immediately pays
    that player one and a half times the amount of his bet. If the dealer has a natural,
    he immediately collects the bets of all players who do not have naturals,
    (but no additional amount). If the dealer and another player both have naturals,
    the bet of that player is a stand-off (a tie), and the player takes back his chips.
     */
    private GameStatus checkNaturals(List<Player> players, Dealer dealer) {
        boolean dealerBlackjack = false;
        GameStatus status = GameStatus.IN_PROGRESS;
        if (dealer.getHand().getStatus() == Hand.Status.BLACKJACK) {
            dealerBlackjack = true;
            println("Dealer blackjack!");
            showDealerCards(dealer, false);
            status = GameStatus.COMPLETE;
        }
        for (Player player : players) {
            if (player.getHand().getValue() == TWENTY_ONE) {
                if (dealerBlackjack) {
                    player.getHand().setStatus(Hand.Status.PUSH);
                    println(player.getName() + " gets a blackjack\n but so does the dealer so player pushes!");
                } else {
                    player.getHand().setStatus(Hand.Status.NATURAL);
                    println(player.getName() + " gets a natural blackjack! $$$");
                }
                showPlayerCards(player);
            } else if (dealerBlackjack) {
                player.getHand().setStatus(Hand.Status.LOSS);
                println(player.getName() + " loses this hand.");
                showPlayerCards(player);
            }
        }
        return status;
    }

    private boolean validBet(int bet, int minBet, int maxBet) {
        return bet >= minBet && bet <= maxBet;
    }

    private void dealCards(Deck deck, List<Player> players, Dealer dealer) {
        for (Player player : players) {
            List<Card> cards = new ArrayList<>();
            cards.add(deck.dealCard());
            cards.add(deck.dealCard());
            Hand hand = new Hand(cards);
            player.setHand(hand);
        }
        List<Card> cards = new ArrayList<>();
        cards.add(deck.dealCard());
        cards.add(deck.dealCard());
        Hand hand = new Hand(cards);
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
    public static void main(String[] args) throws IOException {
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
        options.addOption("d", "decks", true, "maximum bet");

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
            }
            if (cmd.hasOption("p")) {
                numberOfPlayers = Integer.parseInt(cmd.getOptionValue("players"));
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
            }

            Game newGame = new Game();
            newGame.play(startingCash, numberOfPlayers, minBet, maxBet, decks);

        } catch (NumberFormatException numberFormatException) {
            System.out.print("Argument must be a number");
        } catch (MissingArgumentException missingArgumentException) {
            println(missingArgumentException.getMessage());
        } catch (ParseException parseException) {
            println(parseException.getMessage());
            formatter.printHelp("blackjack", options);
        }
    }
}
