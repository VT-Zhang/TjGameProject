package TicTacToe;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class TicTacToePanel extends JPanel implements ActionListener {
    private static final int BOARD_SIZE = 3;
    private static final int CELL_SIZE = 140;
    private static final int BOARD_PIXELS = BOARD_SIZE * CELL_SIZE;
    private static final int HEADER_HEIGHT = 76;
    private static final int FOOTER_HEIGHT = 72;
    private static final int PANEL_WIDTH = BOARD_PIXELS;
    private static final int PANEL_HEIGHT = HEADER_HEIGHT + BOARD_PIXELS + FOOTER_HEIGHT;
    private static final int TIMER_DELAY = 16;
    private static final float MARK_ANIMATION_SPEED = 0.12f;
    private static final float LINE_ANIMATION_SPEED = 0.08f;

    private final CellState[][] board = new CellState[BOARD_SIZE][BOARD_SIZE];
    private final float[][] markProgress = new float[BOARD_SIZE][BOARD_SIZE];
    private final List<AnimatedCell> animatedCells = new ArrayList<>();
    private final Timer timer = new Timer(TIMER_DELAY, this);
    private final BoardCanvas boardCanvas = new BoardCanvas();
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);

    private CellState currentPlayer = CellState.X;
    private boolean gameOver = false;
    private float winLineProgress = 0f;
    private int[][] winningCells;

    public TicTacToePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(15, 23, 35));
        setLayout(new BorderLayout());

        configureStatusLabel();
        configureBoardCanvas();
        configureControls();
        resetGame();

        timer.start();
    }

    private void configureStatusLabel() {
        statusLabel.setPreferredSize(new Dimension(PANEL_WIDTH, HEADER_HEIGHT));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        statusLabel.setForeground(new Color(232, 240, 255));
        add(statusLabel, BorderLayout.NORTH);
    }

    private void configureBoardCanvas() {
        Dimension boardSize = new Dimension(BOARD_PIXELS, BOARD_PIXELS);
        boardCanvas.setPreferredSize(boardSize);
        boardCanvas.setMinimumSize(boardSize);
        boardCanvas.setMaximumSize(boardSize);
        boardCanvas.setBackground(new Color(24, 34, 50));
        boardCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                handleBoardClick(event.getPoint());
            }
        });
        add(boardCanvas, BorderLayout.CENTER);
    }

    private void configureControls() {
        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        controls.setBorder(BorderFactory.createEmptyBorder(14, 22, 18, 22));

        JButton restartButton = new JButton("New Round");
        restartButton.setFocusable(false);
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        restartButton.addActionListener(event -> resetGame());
        controls.add(restartButton, BorderLayout.EAST);

        JLabel hintLabel = new JLabel("Click a square to place a mark");
        hintLabel.setForeground(new Color(162, 176, 198));
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        controls.add(hintLabel, BorderLayout.WEST);

        controls.setPreferredSize(new Dimension(PANEL_WIDTH, FOOTER_HEIGHT));
        add(controls, BorderLayout.SOUTH);
    }

    private void handleBoardClick(Point point) {
        if (gameOver) {
            return;
        }

        int col = point.x / CELL_SIZE;
        int row = point.y / CELL_SIZE;
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return;
        }

        if (board[row][col] != CellState.EMPTY) {
            return;
        }

        board[row][col] = currentPlayer;
        markProgress[row][col] = 0f;
        animatedCells.add(new AnimatedCell(row, col));

        int[][] winner = findWinningCells();
        if (winner != null) {
            winningCells = winner;
            gameOver = true;
            winLineProgress = 0f;
            statusLabel.setText(currentPlayer.displayName() + " wins");
        } else if (isBoardFull()) {
            gameOver = true;
            statusLabel.setText("Draw");
        } else {
            currentPlayer = currentPlayer.next();
            statusLabel.setText(currentPlayer.displayName() + "'s turn");
        }

        boardCanvas.repaint();
    }

    private void resetGame() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                board[row][col] = CellState.EMPTY;
                markProgress[row][col] = 0f;
            }
        }
        animatedCells.clear();
        currentPlayer = CellState.X;
        gameOver = false;
        winLineProgress = 0f;
        winningCells = null;
        statusLabel.setText(currentPlayer.displayName() + "'s turn");
        boardCanvas.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        boolean needsRepaint = false;

        for (int index = animatedCells.size() - 1; index >= 0; index--) {
            AnimatedCell animatedCell = animatedCells.get(index);
            float progress = markProgress[animatedCell.row][animatedCell.col];
            progress = Math.min(1f, progress + MARK_ANIMATION_SPEED);
            markProgress[animatedCell.row][animatedCell.col] = progress;
            needsRepaint = true;
            if (progress >= 1f) {
                animatedCells.remove(index);
            }
        }

        if (winningCells != null && winLineProgress < 1f) {
            winLineProgress = Math.min(1f, winLineProgress + LINE_ANIMATION_SPEED);
            needsRepaint = true;
        }

        if (needsRepaint) {
            boardCanvas.repaint();
        }
    }

    private int[][] findWinningCells() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (board[row][0] != CellState.EMPTY
                    && board[row][0] == board[row][1]
                    && board[row][1] == board[row][2]) {
                return new int[][]{{row, 0}, {row, 1}, {row, 2}};
            }
        }

        for (int col = 0; col < BOARD_SIZE; col++) {
            if (board[0][col] != CellState.EMPTY
                    && board[0][col] == board[1][col]
                    && board[1][col] == board[2][col]) {
                return new int[][]{{0, col}, {1, col}, {2, col}};
            }
        }

        if (board[0][0] != CellState.EMPTY
                && board[0][0] == board[1][1]
                && board[1][1] == board[2][2]) {
            return new int[][]{{0, 0}, {1, 1}, {2, 2}};
        }

        if (board[0][2] != CellState.EMPTY
                && board[0][2] == board[1][1]
                && board[1][1] == board[2][0]) {
            return new int[][]{{0, 2}, {1, 1}, {2, 0}};
        }

        return null;
    }

    private boolean isBoardFull() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == CellState.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private class BoardCanvas extends JPanel {
        BoardCanvas() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            paintBoardBackground(graphics2D);
            paintGrid(graphics2D);
            paintMarks(graphics2D);
            paintWinningLine(graphics2D);

            graphics2D.dispose();
        }

        private void paintBoardBackground(Graphics2D graphics2D) {
            graphics2D.setColor(new Color(20, 31, 46));
            graphics2D.fillRoundRect(0, 0, BOARD_PIXELS, BOARD_PIXELS, 18, 18);
        }

        private void paintGrid(Graphics2D graphics2D) {
            graphics2D.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics2D.setColor(new Color(68, 86, 112));

            for (int index = 1; index < BOARD_SIZE; index++) {
                int offset = index * CELL_SIZE;
                graphics2D.drawLine(offset, 18, offset, BOARD_PIXELS - 18);
                graphics2D.drawLine(18, offset, BOARD_PIXELS - 18, offset);
            }
        }

        private void paintMarks(Graphics2D graphics2D) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    CellState cellState = board[row][col];
                    if (cellState == CellState.EMPTY) {
                        continue;
                    }

                    float progress = animatedCellExists(row, col) ? markProgress[row][col] : 1f;

                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;
                    if (cellState == CellState.X) {
                        drawAnimatedX(graphics2D, x, y, progress);
                    } else {
                        drawAnimatedO(graphics2D, x, y, progress);
                    }
                }
            }
        }

        private boolean animatedCellExists(int row, int col) {
            for (AnimatedCell animatedCell : animatedCells) {
                if (animatedCell.row == row && animatedCell.col == col) {
                    return true;
                }
            }
            return false;
        }

        private void drawAnimatedX(Graphics2D graphics2D, int x, int y, float progress) {
            int padding = 28;
            int x1 = x + padding;
            int y1 = y + padding;
            int x2 = x + CELL_SIZE - padding;
            int y2 = y + CELL_SIZE - padding;

            graphics2D.setColor(new Color(255, 114, 118));
            graphics2D.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float firstSegment = Math.min(progress * 2f, 1f);
            if (firstSegment > 0f) {
                graphics2D.draw(new Line2D.Float(
                        x1,
                        y1,
                        x1 + (x2 - x1) * firstSegment,
                        y1 + (y2 - y1) * firstSegment
                ));
            }

            if (progress > 0.5f) {
                float secondSegment = Math.min((progress - 0.5f) * 2f, 1f);
                graphics2D.draw(new Line2D.Float(
                        x2,
                        y1,
                        x2 - (x2 - x1) * secondSegment,
                        y1 + (y2 - y1) * secondSegment
                ));
            }
        }

        private void drawAnimatedO(Graphics2D graphics2D, int x, int y, float progress) {
            int padding = 26;
            int diameter = CELL_SIZE - (padding * 2);
            int startAngle = 90;
            int arcAngle = Math.min(-1, Math.round(-360 * progress));

            graphics2D.setColor(new Color(88, 193, 255));
            graphics2D.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics2D.drawArc(x + padding, y + padding, diameter, diameter, startAngle, arcAngle);
        }

        private void paintWinningLine(Graphics2D graphics2D) {
            if (winningCells == null) {
                return;
            }

            Point start = centerOf(winningCells[0][0], winningCells[0][1]);
            Point end = centerOf(winningCells[2][0], winningCells[2][1]);

            graphics2D.setColor(new Color(255, 225, 120));
            graphics2D.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int animatedX = Math.round(start.x + (end.x - start.x) * winLineProgress);
            int animatedY = Math.round(start.y + (end.y - start.y) * winLineProgress);
            graphics2D.drawLine(start.x, start.y, animatedX, animatedY);
        }

        private Point centerOf(int row, int col) {
            return new Point(col * CELL_SIZE + CELL_SIZE / 2, row * CELL_SIZE + CELL_SIZE / 2);
        }
    }

    private static class AnimatedCell {
        private final int row;
        private final int col;

        private AnimatedCell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private enum CellState {
        EMPTY(""),
        X("X"),
        O("O");

        private final String displayName;

        CellState(String displayName) {
            this.displayName = displayName;
        }

        private String displayName() {
            return displayName;
        }

        private CellState next() {
            return this == X ? O : X;
        }
    }
}
