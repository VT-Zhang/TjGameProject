package MaseRunner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MazeRunnerPanel extends JPanel {
    private static final int TILE_SIZE = 24;
    private static final int ROWS = 21;
    private static final int COLS = 21;
    private static final int PANEL_WIDTH = COLS * TILE_SIZE;
    private static final int PANEL_HEIGHT = ROWS * TILE_SIZE;

    private final MazeGenerator generator = new MazeGenerator();
    private final JLabel statusLabel = new JLabel("", JLabel.CENTER);
    private final MazeCanvas canvas = new MazeCanvas();

    private boolean[][] maze;
    private Point player;
    private Point goal;
    private int moves;
    private boolean won;

    public MazeRunnerPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(233, 229, 219));

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(statusLabel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        setFocusable(true);
        addKeyListener(new MazeKeyAdapter());

        startNewGame();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, PANEL_HEIGHT + 40);
    }

    private void startNewGame() {
        maze = generator.generate(ROWS, COLS);
        player = new Point(1, 1);
        goal = new Point(COLS - 2, ROWS - 2);
        moves = 0;
        won = false;
        refreshStatus();
        repaint();
        requestFocusInWindow();
    }

    private void refreshStatus() {
        if (won) {
            statusLabel.setText("You escaped in " + moves + " moves. Press R for a new maze.");
        } else {
            statusLabel.setText("Arrow keys move. Reach the blue goal. Moves: " + moves + "   Press R to restart.");
        }
    }

    private void movePlayer(int dx, int dy) {
        if (won) {
            return;
        }

        int nextCol = player.x + dx;
        int nextRow = player.y + dy;

        if (nextRow < 0 || nextRow >= ROWS || nextCol < 0 || nextCol >= COLS || maze[nextRow][nextCol]) {
            return;
        }

        player.translate(dx, dy);
        moves++;

        if (player.equals(goal)) {
            won = true;
        }

        refreshStatus();
        repaint();
    }

    private class MazeCanvas extends JPanel {
        MazeCanvas() {
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(new Color(248, 245, 238));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (maze[row][col]) {
                        g2.setColor(new Color(50, 60, 75));
                    } else {
                        g2.setColor(new Color(244, 241, 232));
                    }
                    g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }

            g2.setColor(new Color(82, 143, 214));
            g2.fillRoundRect(goal.x * TILE_SIZE, goal.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, 8, 8);

            g2.setColor(new Color(223, 122, 65));
            g2.fillOval(player.x * TILE_SIZE + 3, player.y * TILE_SIZE + 3, TILE_SIZE - 6, TILE_SIZE - 6);

            if (won) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 34));
                String text = "Maze Complete";
                int width = g2.getFontMetrics().stringWidth(text);
                g2.drawString(text, (PANEL_WIDTH - width) / 2, PANEL_HEIGHT / 2);
            }
        }
    }

    private class MazeKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_UP -> movePlayer(0, -1);
                case KeyEvent.VK_DOWN -> movePlayer(0, 1);
                case KeyEvent.VK_LEFT -> movePlayer(-1, 0);
                case KeyEvent.VK_RIGHT -> movePlayer(1, 0);
                case KeyEvent.VK_R -> startNewGame();
                default -> {
                }
            }
        }
    }
}
