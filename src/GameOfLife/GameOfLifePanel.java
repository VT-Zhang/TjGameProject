package GameOfLife;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class GameOfLifePanel extends JPanel implements ActionListener {
    private static final int CELL_SIZE = 20;
    private static final int BOARD_PIXEL_SIZE = GameOfLifeBoard.ROWS * CELL_SIZE;
    private static final int DEFAULT_DELAY = 160;
    private static final int MIN_DELAY = 60;
    private static final int MAX_DELAY = 400;

    private final GameOfLifeBoard board = new GameOfLifeBoard();
    private final LifeRules rules = new LifeRules();
    private final CellPalette palette = new CellPalette();
    private final Random random = new Random();
    private final Timer timer = new Timer(DEFAULT_DELAY, this);

    private final BoardCanvas boardCanvas = new BoardCanvas();
    private final JLabel statusLabel = new JLabel("Paused", SwingConstants.LEFT);

    private boolean running;
    private boolean dragPaintAlive = true;
    private int generation;

    public GameOfLifePanel() {
        setLayout(new BorderLayout());
        setBackground(palette.getPanelBackground());

        add(createControlBar(), BorderLayout.NORTH);
        add(boardCanvas, BorderLayout.CENTER);

        randomizeBoard();
        updateStatus();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE + 88);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) {
            return;
        }

        stepSimulation();
    }

    private JPanel createControlBar() {
        JPanel controlBar = new JPanel();
        controlBar.setBackground(palette.getControlBackground());
        controlBar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        controlBar.setLayout(new BoxLayout(controlBar, BoxLayout.X_AXIS));

        JButton startButton = createButton("Start", event -> startSimulation());
        JButton pauseButton = createButton("Pause", event -> pauseSimulation());
        JButton stepButton = createButton("Step", event -> stepOnce());
        JButton clearButton = createButton("Clear", event -> clearBoard());
        JButton randomizeButton = createButton("Randomize", event -> randomizeBoard());

        JSlider speedSlider = new JSlider(1, 10, 6);
        speedSlider.setBackground(palette.getControlBackground());
        speedSlider.addChangeListener(event -> {
            int value = speedSlider.getValue();
            int delay = MAX_DELAY - ((value - 1) * (MAX_DELAY - MIN_DELAY) / 9);
            timer.setDelay(delay);
        });

        JLabel speedLabel = new JLabel("Speed");
        speedLabel.setForeground(palette.getTextColor());
        statusLabel.setForeground(palette.getTextColor());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 13f));

        controlBar.add(startButton);
        controlBar.add(Box.createHorizontalStrut(8));
        controlBar.add(pauseButton);
        controlBar.add(Box.createHorizontalStrut(8));
        controlBar.add(stepButton);
        controlBar.add(Box.createHorizontalStrut(8));
        controlBar.add(clearButton);
        controlBar.add(Box.createHorizontalStrut(8));
        controlBar.add(randomizeButton);
        controlBar.add(Box.createHorizontalStrut(16));
        controlBar.add(speedLabel);
        controlBar.add(Box.createHorizontalStrut(6));
        controlBar.add(speedSlider);
        controlBar.add(Box.createHorizontalStrut(16));
        controlBar.add(statusLabel);

        return controlBar;
    }

    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setFocusable(false);
        return button;
    }

    private void startSimulation() {
        running = true;
        timer.start();
        updateStatus();
    }

    private void pauseSimulation() {
        running = false;
        timer.stop();
        updateStatus();
    }

    private void stepOnce() {
        pauseSimulation();
        stepSimulation();
    }

    private void clearBoard() {
        pauseSimulation();
        board.clear();
        generation = 0;
        boardCanvas.repaint();
        updateStatus();
    }

    private void randomizeBoard() {
        pauseSimulation();
        board.randomize(random);
        generation = 0;
        boardCanvas.repaint();
        updateStatus();
    }

    private void stepSimulation() {
        rules.step(board);
        generation++;
        boardCanvas.repaint();
        updateStatus();
    }

    private void updateStatus() {
        int aliveCells = countAliveCells();
        String mode = running ? "Running" : "Paused";
        statusLabel.setText(mode + "  |  Generation: " + generation + "  |  Alive: " + aliveCells);
    }

    private int countAliveCells() {
        int count = 0;
        for (int row = 0; row < GameOfLifeBoard.ROWS; row++) {
            for (int col = 0; col < GameOfLifeBoard.COLS; col++) {
                if (board.isAlive(row, col)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void applyCellChange(MouseEvent event, boolean allowToggle) {
        Point cell = boardCanvas.toCell(event.getPoint());
        if (cell == null) {
            return;
        }

        boolean currentlyAlive = board.isAlive(cell.y, cell.x);
        if (allowToggle) {
            dragPaintAlive = !currentlyAlive;
        }

        board.setAlive(cell.y, cell.x, dragPaintAlive);
        boardCanvas.repaint();
        updateStatus();
    }

    private class BoardCanvas extends JPanel {
        private BoardCanvas() {
            setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
            setBackground(palette.getBoardBackground());

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    applyCellChange(e, true);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    applyCellChange(e, false);
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private Point toCell(Point point) {
            int col = point.x / CELL_SIZE;
            int row = point.y / CELL_SIZE;

            if (row < 0 || row >= GameOfLifeBoard.ROWS || col < 0 || col >= GameOfLifeBoard.COLS) {
                return null;
            }

            return new Point(col, row);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(palette.getBoardBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            paintCells(g2);
            paintGrid(g2);
            paintBoardLabel(g2);

            g2.dispose();
        }

        private void paintCells(Graphics2D g2) {
            for (int row = 0; row < GameOfLifeBoard.ROWS; row++) {
                for (int col = 0; col < GameOfLifeBoard.COLS; col++) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;

                    if (board.isAlive(row, col)) {
                        g2.setColor(palette.getAliveColor(row, col, board.getCellAge(row, col)));
                        g2.fillRoundRect(x + 2, y + 2, CELL_SIZE - 3, CELL_SIZE - 3, 8, 8);
                    } else {
                        g2.setColor(palette.getDeadCellOverlay(row, col));
                        g2.fillRect(x + 1, y + 1, CELL_SIZE - 1, CELL_SIZE - 1);
                    }
                }
            }
        }

        private void paintGrid(Graphics2D g2) {
            g2.setColor(palette.getGridColor());
            g2.setStroke(new BasicStroke(1f));

            for (int index = 0; index <= GameOfLifeBoard.ROWS; index++) {
                int position = index * CELL_SIZE;
                g2.drawLine(0, position, BOARD_PIXEL_SIZE, position);
                g2.drawLine(position, 0, position, BOARD_PIXEL_SIZE);
            }
        }

        private void paintBoardLabel(Graphics2D g2) {
            String text = "Wrap-around 32 x 32 board";
            g2.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics metrics = g2.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            int x = BOARD_PIXEL_SIZE - textWidth - 12;
            int y = BOARD_PIXEL_SIZE - 14;

            g2.setColor(new java.awt.Color(8, 10, 18, 160));
            g2.fillRoundRect(x - 8, y - metrics.getAscent(), textWidth + 16, metrics.getHeight(), 10, 10);
            g2.setColor(palette.getTextColor());
            g2.drawString(text, x, y);
        }
    }
}
