package BlackJack;

public class AnimatedCard {
    private final Card card;
    private double x;
    private double y;
    private final double targetX;
    private final double targetY;
    private boolean faceUp;

    public AnimatedCard(Card card, double startX, double startY, double targetX, double targetY, boolean faceUp) {
        this.card = card;
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.faceUp = faceUp;
    }

    public Card getCard() {
        return card;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
