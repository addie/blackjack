# Blackjack

This is a basic command line blackjack game. It has be ability to support multiple players, and supports the common casino blackjack rules such as doubling down, splitting, and insurance. (Surrender is not currently supported).

## Usage

`java -jar blackjack.jar [args]`

	-h,--help            help
	-c,--cash <arg>      starting cash for players
	-d,--decks <arg>     number of decks
	-m,--minbet <arg>    minimum bet
	-M,--maxbet <arg>    maximum bet
	-p,--players <arg>   number of players

## Defaults

	Players: 1
	Cash: $1000
	Min Bet: $2
	Max Bet: $10,000
	Number of Decks: 6

## Rules

### Double Down

* Player may double down on 9, 10, or 11

### Insurance

* Insurance pays out 2:1 
* You can wager up to half of your initial bet

### Splitting

* Initial hand my be split if they are the same rank 
