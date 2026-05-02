package Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakePanel extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 24;
    private static final int GRID_HEIGHT = 24;
    private static final int PANEL_WIDTH = GRID_WIDTH * TILE_SIZE;
    private static final int PANEL_HEIGHT = GRID_HEIGHT * TILE_SIZE;
    private static final int START_LENGTH = 3;
    private static final int TIMER_DELAY = 120;

    private final Random random = new Random();
    private final Timer timer = new Timer(TIMER_DELAY, this);
    private final List<Point> snake = new ArrayList<>();

    private Point food;
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private boolean running = false;
    private int score = 0;

    public SnakePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(22, 28, 36));
        setFocusable(true);
        addKeyListener(new SnakeKeyAdapter());
        startGame();
    }

    private void startGame() {
        snake.clear();

        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;
        for (int i = 0; i < START_LENGTH; i++) {
            snake.add(new Point(startX - i, startY));
        }

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        running = true;

        spawnFood();
        timer.start();
        requestFocusInWindow();
    }

    private void spawnFood() {
        Point candidate;
        do {
            candidate = new Point(random.nextInt(GRID_WIDTH), random.nextInt(GRID_HEIGHT));
        } while (snake.contains(candidate));
        food = candidate;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawGrid(graphics);
        drawFood(graphics);
        drawSnake(graphics);
        drawScore(graphics);

        if (!running) {
            drawGameOver(graphics);
        }
    }

    private void drawGrid(Graphics graphics) {
        graphics.setColor(new Color(38, 48, 60));
        for (int x = 0; x <= PANEL_WIDTH; x += TILE_SIZE) {
            graphics.drawLine(x, 0, x, PANEL_HEIGHT);
        }
        for (int y = 0; y <= PANEL_HEIGHT; y += TILE_SIZE) {
            graphics.drawLine(0, y, PANEL_WIDTH, y);
        }
    }

    private void drawFood(Graphics graphics) {
        graphics.setColor(new Color(255, 107, 107));
        graphics.fillOval(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawSnake(Graphics graphics) {
        for (int i = 0; i < snake.size(); i++) {
            Point segment = snake.get(i);
            graphics.setColor(i == 0 ? new Color(126, 217, 87) : new Color(78, 168, 61));
            graphics.fillRoundRect(
                    segment.x * TILE_SIZE,
                    segment.y * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE,
                    8,
                    8
            );
        }
    }

    private void drawScore(Graphics graphics) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
        graphics.drawString("Score: " + score, 15, 25);
    }

    private void drawGameOver(Graphics graphics) {
        graphics.setColor(new Color(0, 0, 0, 170));
        graphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 36));
        String gameOverText = "Game Over";
        int gameOverWidth = graphics.getFontMetrics().stringWidth(gameOverText);
        graphics.drawString(gameOverText, (PANEL_WIDTH - gameOverWidth) / 2, PANEL_HEIGHT / 2 - 20);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 20));
        String restartText = "Press R to restart";
        int restartWidth = graphics.getFontMetrics().stringWidth(restartText);
        graphics.drawString(restartText, (PANEL_WIDTH - restartWidth) / 2, PANEL_HEIGHT / 2 + 20);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (running) {
            moveSnake();
            checkFood();
            checkCollisions();
        }
        repaint();
    }

    private void moveSnake() {
        direction = nextDirection;

        Point head = snake.get(0);
        Point newHead = new Point(head);

        switch (direction) {
            case UP -> newHead.y--;
            case DOWN -> newHead.y++;
            case LEFT -> newHead.x--;
            case RIGHT -> newHead.x++;
        }

        snake.add(0, newHead);
        if (!newHead.equals(food)) {
            snake.remove(snake.size() - 1);
        }
    }

    private void checkFood() {
        if (snake.get(0).equals(food)) {
            score++;
            spawnFood();
        }
    }

    private void checkCollisions() {
        Point head = snake.get(0);

        if (head.x < 0 || head.x >= GRID_WIDTH || head.y < 0 || head.y >= GRID_HEIGHT) {
            endGame();
            return;
        }

        // Check against the rest of the body after the head has moved.
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                endGame();
                return;
            }
        }
    }

    private void endGame() {
        running = false;
        timer.stop();
    }

    private class SnakeKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_UP -> updateDirection(Direction.UP);
                case KeyEvent.VK_DOWN -> updateDirection(Direction.DOWN);
                case KeyEvent.VK_LEFT -> updateDirection(Direction.LEFT);
                case KeyEvent.VK_RIGHT -> updateDirection(Direction.RIGHT);
                case KeyEvent.VK_R -> {
                    if (!running) {
                        startGame();
                    }
                }
                default -> {
                }
            }
        }
    }

    private void updateDirection(Direction candidate) {
        if (!candidate.isOpposite(direction)) {
            nextDirection = candidate;
        }
    }

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        private boolean isOpposite(Direction other) {
            return (this == UP && other == DOWN)
                    || (this == DOWN && other == UP)
                    || (this == LEFT && other == RIGHT)
                    || (this == RIGHT && other == LEFT);
        }
    }
}
