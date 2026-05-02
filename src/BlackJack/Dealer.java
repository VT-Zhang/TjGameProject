package BlackJack;

public class Dealer extends Player {
    public Dealer() {
        super("Dealer");
    }

    public boolean shouldHit() {
        return getHand().getBestValue() < 17;
    }
}
