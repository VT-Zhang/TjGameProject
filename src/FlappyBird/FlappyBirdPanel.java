package FlappyBird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FlappyBirdPanel extends JPanel implements ActionListener {
    private static final int PANEL_WIDTH = 480;
    private static final int PANEL_HEIGHT = 640;
    private static final int GROUND_HEIGHT = 80;
    private static final int BIRD_X = 120;
    private static final int BIRD_SIZE = 28;
    private static final int PIPE_WIDTH = 70;
    private static final int PIPE_GAP = 170;
    private static final int PIPE_SPACING = 210;
    private static final int PIPE_SPEED = 4;
    private static final double GRAVITY = 0.55;
    private static final double FLAP_STRENGTH = -8.5;
    private static final int TIMER_DELAY = 16;

    private final Timer timer = new Timer(TIMER_DELAY, this);
    private final Random random = new Random();
    private final List<Pipe> pipes = new ArrayList<>();

    private double birdY;
    private double birdVelocity;
    private boolean started;
    private boolean gameOver;
    private int score;

    public FlappyBirdPanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(132, 209, 250));
        setFocusable(true);
        addKeyListener(new BirdKeyAdapter());
        resetGame();
        timer.start();
    }

    private void resetGame() {
        pipes.clear();
        birdY = PANEL_HEIGHT / 2.0 - BIRD_SIZE / 2.0;
        birdVelocity = 0;
        started = false;
        gameOver = false;
        score = 0;
        addPipe(PANEL_WIDTH + 80);
        addPipe(PANEL_WIDTH + 80 + PIPE_SPACING);
        requestFocusInWindow();
        repaint();
    }

    private void addPipe(int x) {
        int minGapY = 70;
        int maxGapY = PANEL_HEIGHT - GROUND_HEIGHT - PIPE_GAP - 70;
        int gapY = minGapY + random.nextInt(maxGapY - minGapY + 1);
        pipes.add(new Pipe(x, gapY, PIPE_GAP, PIPE_WIDTH, PANEL_HEIGHT, GROUND_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawPipes(g2);
        drawBird(g2);
        drawGround(g2);
        drawScore(g2);

        if (!started && !gameOver) {
            drawCenteredMessage(g2, "Press Space to Start", PANEL_HEIGHT / 2 - 20, 28);
        }

        if (gameOver) {
            drawCenteredMessage(g2, "Game Over", PANEL_HEIGHT / 2 - 30, 34);
            drawCenteredMessage(g2, "Press R to Restart", PANEL_HEIGHT / 2 + 10, 22);
        }
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(new Color(132, 209, 250));
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2.setColor(new Color(255, 255, 255, 140));
        g2.fillOval(60, 90, 90, 40);
        g2.fillOval(300, 130, 120, 45);
    }

    private void drawPipes(Graphics2D g2) {
        g2.setColor(new Color(76, 175, 80));
        for (Pipe pipe : pipes) {
            Rectangle top = pipe.getTopRect();
            Rectangle bottom = pipe.getBottomRect();
            g2.fillRect(top.x, top.y, top.width, top.height);
            g2.fillRect(bottom.x, bottom.y, bottom.width, bottom.height);

            g2.setColor(new Color(56, 142, 60));
            g2.fillRect(top.x - 4, Math.max(0, top.height - 18), top.width + 8, 18);
            g2.fillRect(bottom.x - 4, bottom.y, bottom.width + 8, 18);
            g2.setColor(new Color(76, 175, 80));
        }
    }

    private void drawBird(Graphics2D g2) {
        g2.setColor(new Color(255, 214, 10));
        g2.fillOval(BIRD_X, (int) birdY, BIRD_SIZE, BIRD_SIZE);

        g2.setColor(Color.WHITE);
        g2.fillOval(BIRD_X + 16, (int) birdY + 7, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(BIRD_X + 19, (int) birdY + 10, 4, 4);

        g2.setColor(new Color(255, 120, 20));
        int[] beakX = {BIRD_X + BIRD_SIZE - 2, BIRD_X + BIRD_SIZE + 10, BIRD_X + BIRD_SIZE - 2};
        int[] beakY = {(int) birdY + 14, (int) birdY + 10, (int) birdY + 18};
        g2.fillPolygon(beakX, beakY, 3);
    }

    private void drawGround(Graphics2D g2) {
        g2.setColor(new Color(194, 168, 110));
        g2.fillRect(0, PANEL_HEIGHT - GROUND_HEIGHT, PANEL_WIDTH, GROUND_HEIGHT);
        g2.setColor(new Color(109, 191, 78));
        g2.fillRect(0, PANEL_HEIGHT - GROUND_HEIGHT, PANEL_WIDTH, 12);
    }

    private void drawScore(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.drawString("Score: " + score, 18, 38);
    }

    private void drawCenteredMessage(Graphics2D g2, String text, int y, int size) {
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(85, y - 32, 310, 56, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, size));
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (PANEL_WIDTH - width) / 2, y);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (started && !gameOver) {
            updateBird();
            updatePipes();
            checkCollisionsAndScore();
        }
        repaint();
    }

    private void updateBird() {
        birdVelocity += GRAVITY;
        birdY += birdVelocity;
    }

    private void updatePipes() {
        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.move(PIPE_SPEED);
            if (pipe.isOffScreen()) {
                iterator.remove();
            }
        }

        if (!pipes.isEmpty()) {
            Pipe lastPipe = pipes.get(pipes.size() - 1);
            if (lastPipe.getTopRect().x < PANEL_WIDTH - PIPE_SPACING) {
                addPipe(PANEL_WIDTH);
            }
        }
    }

    private void checkCollisionsAndScore() {
        Rectangle bird = new Rectangle(BIRD_X, (int) birdY, BIRD_SIZE, BIRD_SIZE);

        if (birdY < 0 || birdY + BIRD_SIZE > PANEL_HEIGHT - GROUND_HEIGHT) {
            gameOver = true;
            return;
        }

        for (Pipe pipe : pipes) {
            // Each pipe pair can score only once, after the bird has fully passed it.
            if (!pipe.isScored() && pipe.getTopRect().x + PIPE_WIDTH < BIRD_X) {
                pipe.setScored(true);
                score++;
            }

            if (bird.intersects(pipe.getTopRect()) || bird.intersects(pipe.getBottomRect())) {
                gameOver = true;
                return;
            }
        }
    }

    private void flap() {
        if (gameOver) {
            return;
        }

        started = true;
        birdVelocity = FLAP_STRENGTH;
    }

    private class BirdKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_SPACE, KeyEvent.VK_UP -> flap();
                case KeyEvent.VK_R -> resetGame();
                default -> {
                }
            }
        }
    }
}
