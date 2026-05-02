package BlackJack;

public class BlackjackGame {
    private final Deck deck = new Deck();
    private final Player player = new Player("Player");
    private final Dealer dealer = new Dealer();

    private GameState state = GameState.DEALING;
    private String message = "Press New Round to start.";
    private int wins;
    private int losses;
    private int pushes;

    public void startNewRound() {
        player.resetHand();
        dealer.resetHand();
        state = GameState.DEALING;
        message = "Cards are on the way.";

        player.getHand().addCard(deck.dealCard());
        dealer.getHand().addCard(deck.dealCard());
        player.getHand().addCard(deck.dealCard());
        dealer.getHand().addCard(deck.dealCard());
    }

    public void finishInitialDeal() {
        boolean playerBlackjack = player.getHand().isBlackjack();
        boolean dealerBlackjack = dealer.getHand().isBlackjack();

        if (playerBlackjack || dealerBlackjack) {
            if (playerBlackjack && dealerBlackjack) {
                pushes++;
                message = "Push. Both have blackjack.";
            } else if (playerBlackjack) {
                wins++;
                message = "Blackjack. You win.";
            } else {
                losses++;
                message = "Dealer blackjack. You lose.";
            }
            state = GameState.ROUND_OVER;
        } else {
            state = GameState.PLAYER_TURN;
            message = "Your move: Hit or Stand.";
        }
    }

    public Card hitPlayer() {
        if (state != GameState.PLAYER_TURN) {
            return null;
        }

        Card card = deck.dealCard();
        player.getHand().addCard(card);

        if (player.getHand().isBust()) {
            losses++;
            state = GameState.ROUND_OVER;
            message = "Bust. Dealer wins.";
        } else if (player.getHand().getBestValue() == 21) {
            state = GameState.DEALER_TURN;
            message = "21. Dealer's turn.";
        } else {
            message = "Your move: Hit or Stand.";
        }

        return card;
    }

    public void stand() {
        if (state == GameState.PLAYER_TURN) {
            state = GameState.DEALER_TURN;
            message = "Dealer is drawing.";
        }
    }

    public Card drawDealerCard() {
        if (state != GameState.DEALER_TURN) {
            return null;
        }

        Card card = deck.dealCard();
        dealer.getHand().addCard(card);
        return card;
    }

    public void finishDealerTurn() {
        int dealerValue = dealer.getHand().getBestValue();
        int playerValue = player.getHand().getBestValue();

        if (dealerValue > 21) {
            wins++;
            message = "Dealer busts. You win.";
        } else if (dealerValue > playerValue) {
            losses++;
            message = "Dealer wins with " + dealerValue + ".";
        } else if (dealerValue < playerValue) {
            wins++;
            message = "You win with " + playerValue + ".";
        } else {
            pushes++;
            message = "Push at " + playerValue + ".";
        }

        state = GameState.ROUND_OVER;
    }

    public Player getPlayer() {
        return player;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getPushes() {
        return pushes;
    }
}
