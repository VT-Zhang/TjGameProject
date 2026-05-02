package FlappyBird;

import java.awt.*;

public class Pipe {
    private final Rectangle topRect;
    private final Rectangle bottomRect;
    private boolean scored;

    public Pipe(int x, int gapY, int gapHeight, int width, int panelHeight, int groundHeight) {
        topRect = new Rectangle(x, 0, width, gapY);
        int bottomY = gapY + gapHeight;
        bottomRect = new Rectangle(x, bottomY, width, panelHeight - groundHeight - bottomY);
    }

    public void move(int speed) {
        topRect.x -= speed;
        bottomRect.x -= speed;
    }

    public boolean isOffScreen() {
        return topRect.x + topRect.width < 0;
    }

    public Rectangle getTopRect() {
        return topRect;
    }

    public Rectangle getBottomRect() {
        return bottomRect;
    }

    public boolean isScored() {
        return scored;
    }

    public void setScored(boolean scored) {
        this.scored = scored;
    }
}
