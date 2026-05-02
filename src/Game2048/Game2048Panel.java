package Game2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048Panel extends JPanel {
    private static final int GRID_SIZE = 4;
    private static final int TILE_SIZE = 96;
    private static final int TILE_GAP = 14;
    private static final int BOARD_SIZE = TILE_SIZE * GRID_SIZE + TILE_GAP * (GRID_SIZE + 1);
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 700;
    private static final int ANIMATION_DURATION_MS = 110;
    private static final int ANIMATION_STEP_MS = 16;

    private final Random random = new Random();
    private final int[][] board = new int[GRID_SIZE][GRID_SIZE];
    private final Timer animationTimer = new Timer(ANIMATION_STEP_MS, this::advanceAnimation);
    private final JButton restartButton = new JButton("New Game");

    private int[][] animationBoard;
    private int[][] pendingBoard;
    private List<TileMotion> activeMotions = new ArrayList<>();
    private long animationStartTime;
    private double animationProgress;
    private boolean animating;

    private int score;
    private int bestScore;
    private boolean won;
    private boolean gameOver;

    public Game2048Panel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(245, 239, 230));
        setLayout(new BorderLayout());

        JPanel controls = new JPanel(new GridLayout(1, 1));
        controls.setOpaque(false);
        controls.add(restartButton);
        add(controls, BorderLayout.SOUTH);

        styleButton();
        bindKeys();

        restartButton.addActionListener(event -> startGame());

        startGame();
    }

    private void styleButton() {
        restartButton.setFocusable(false);
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        restartButton.setBackground(new Color(103, 80, 164));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBorderPainted(false);
        restartButton.setPreferredSize(new Dimension(160, 54));
    }

    private void bindKeys() {
        bindKey("UP", Direction.UP);
        bindKey("DOWN", Direction.DOWN);
        bindKey("LEFT", Direction.LEFT);
        bindKey("RIGHT", Direction.RIGHT);
        bindKey("W", Direction.UP);
        bindKey("S", Direction.DOWN);
        bindKey("A", Direction.LEFT);
        bindKey("D", Direction.RIGHT);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "restart");
        getActionMap().put("restart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                startGame();
            }
        });
    }

    private void bindKey(String key, Direction direction) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                move(direction);
            }
        });
    }

    private void startGame() {
        animationTimer.stop();
        animating = false;
        activeMotions = new ArrayList<>();
        animationBoard = null;
        pendingBoard = null;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                board[row][column] = 0;
            }
        }

        score = 0;
        won = false;
        gameOver = false;
        spawnTile();
        spawnTile();
        repaint();
    }

    private void move(Direction direction) {
        if (gameOver || animating) {
            return;
        }

        MoveResult result = buildMoveResult(direction);
        if (!result.changed()) {
            return;
        }

        score += result.scoreGain();
        bestScore = Math.max(bestScore, score);
        pendingBoard = result.board();
        animationBoard = buildAnimationBoard(result.board(), result.motions());
        activeMotions = result.motions();
        animationProgress = 0.0;
        animating = true;
        animationStartTime = System.currentTimeMillis();
        animationTimer.start();
        repaint();
    }

    private void advanceAnimation(ActionEvent event) {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        animationProgress = Math.min(1.0, elapsed / (double) ANIMATION_DURATION_MS);

        if (animationProgress >= 1.0) {
            animationTimer.stop();
            copyBoard(pendingBoard, board);
            pendingBoard = null;
            animationBoard = null;
            activeMotions = new ArrayList<>();
            animating = false;

            spawnTile();
            won = hasValue(2048);
            gameOver = won || (!canMove() && !hasEmptyCell());
        }

        repaint();
    }

    private MoveResult buildMoveResult(Direction direction) {
        int[][] nextBoard = new int[GRID_SIZE][GRID_SIZE];
        List<TileMotion> motions = new ArrayList<>();
        int scoreGain = 0;

        for (int line = 0; line < GRID_SIZE; line++) {
            List<TileData> tiles = new ArrayList<>();
            for (int index = 0; index < GRID_SIZE; index++) {
                Cell cell = cellFor(direction, line, index);
                int value = board[cell.row()][cell.column()];
                if (value != 0) {
                    tiles.add(new TileData(value, cell.row(), cell.column()));
                }
            }

            int destinationIndex = 0;
            int tileIndex = 0;
            while (tileIndex < tiles.size()) {
                TileData current = tiles.get(tileIndex);
                Cell target = cellFor(direction, line, destinationIndex);

                if (tileIndex + 1 < tiles.size() && tiles.get(tileIndex + 1).value() == current.value()) {
                    TileData next = tiles.get(tileIndex + 1);
                    int mergedValue = current.value() * 2;
                    nextBoard[target.row()][target.column()] = mergedValue;
                    motions.add(new TileMotion(current.value(), current.row(), current.column(), target.row(), target.column()));
                    motions.add(new TileMotion(next.value(), next.row(), next.column(), target.row(), target.column()));
                    scoreGain += mergedValue;
                    tileIndex += 2;
                } else {
                    nextBoard[target.row()][target.column()] = current.value();
                    if (current.row() != target.row() || current.column() != target.column()) {
                        motions.add(new TileMotion(current.value(), current.row(), current.column(), target.row(), target.column()));
                    }
                    tileIndex++;
                }

                destinationIndex++;
            }
        }

        return new MoveResult(nextBoard, motions, scoreGain, !sameBoard(board, nextBoard));
    }

    private int[][] buildAnimationBoard(int[][] nextBoard, List<TileMotion> motions) {
        int[][] staticBoard = copyBoard(nextBoard);
        for (TileMotion motion : motions) {
            staticBoard[motion.toRow()][motion.toColumn()] = 0;
        }
        return staticBoard;
    }

    private Cell cellFor(Direction direction, int line, int index) {
        return switch (direction) {
            case LEFT -> new Cell(line, index);
            case RIGHT -> new Cell(line, GRID_SIZE - 1 - index);
            case UP -> new Cell(index, line);
            case DOWN -> new Cell(GRID_SIZE - 1 - index, line);
        };
    }

    private void spawnTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                if (board[row][column] == 0) {
                    emptyCells.add(new int[]{row, column});
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return;
        }

        int[] position = emptyCells.get(random.nextInt(emptyCells.size()));
        board[position[0]][position[1]] = random.nextDouble() < 0.9 ? 2 : 4;
    }

    private boolean hasEmptyCell() {
        for (int[] row : board) {
            for (int value : row) {
                if (value == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasValue(int target) {
        for (int[] row : board) {
            for (int value : row) {
                if (value == target) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canMove() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                int value = board[row][column];
                if (row + 1 < GRID_SIZE && board[row + 1][column] == value) {
                    return true;
                }
                if (column + 1 < GRID_SIZE && board[row][column + 1] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private int[][] copyBoard(int[][] source) {
        int[][] result = new int[GRID_SIZE][GRID_SIZE];
        copyBoard(source, result);
        return result;
    }

    private void copyBoard(int[][] source, int[][] destination) {
        for (int row = 0; row < GRID_SIZE; row++) {
            System.arraycopy(source[row], 0, destination[row], 0, GRID_SIZE);
        }
    }

    private boolean sameBoard(int[][] first, int[][] second) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                if (first[row][column] != second[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHeader(g2);
        drawInstructions(g2);
        drawBoard(g2);

        if (won || gameOver) {
            drawOverlay(g2);
        }

        g2.dispose();
    }

    private void drawHeader(Graphics2D graphics) {
        graphics.setColor(new Color(72, 62, 117));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 54));
        graphics.drawString("2048", 34, 78);

        drawScoreCard(graphics, "SCORE", score, 300, 26, new Color(255, 149, 128));
        drawScoreCard(graphics, "BEST", bestScore, 404, 26, new Color(122, 196, 121));
    }

    private void drawScoreCard(Graphics2D graphics, String label, int value, int x, int y, Color color) {
        graphics.setColor(color);
        graphics.fillRoundRect(x, y, 88, 70, 20, 20);

        graphics.setColor(new Color(255, 255, 255, 235));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
        centerText(graphics, label, x, y + 22, 88);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        centerText(graphics, String.valueOf(value), x, y + 52, 88);
    }

    private void drawInstructions(Graphics2D graphics) {
        graphics.setColor(new Color(110, 97, 140));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 18));
        graphics.drawString("Use arrow keys or WASD to combine tiles.", 34, 122);
        graphics.drawString("Press R or click New Game to restart.", 34, 147);
    }

    private void drawBoard(Graphics2D graphics) {
        int boardX = (PANEL_WIDTH - BOARD_SIZE) / 2;
        int boardY = 182;

        graphics.setColor(new Color(188, 173, 160));
        graphics.fillRoundRect(boardX, boardY, BOARD_SIZE, BOARD_SIZE, 26, 26);
        graphics.setStroke(new BasicStroke(2f));

        int[][] boardToDraw = hasAnimatedMotion() ? animationBoard : board;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                int x = cellX(column);
                int y = cellY(row);
                drawTile(graphics, boardToDraw[row][column], x, y);
            }
        }

        if (hasAnimatedMotion()) {
            drawAnimatedTiles(graphics);
        }
    }

    private void drawAnimatedTiles(Graphics2D graphics) {
        double progress = easedProgress();
        for (TileMotion motion : activeMotions) {
            int x = interpolate(cellX(motion.fromColumn()), cellX(motion.toColumn()), progress);
            int y = interpolate(cellY(motion.fromRow()), cellY(motion.toRow()), progress);
            drawTile(graphics, motion.value(), x, y);
        }
    }

    private boolean hasAnimatedMotion() {
        return animating && animationBoard != null && !activeMotions.isEmpty();
    }

    private double easedProgress() {
        double inverse = 1.0 - animationProgress;
        return 1.0 - inverse * inverse * inverse;
    }

    private int cellX(int column) {
        int boardX = (PANEL_WIDTH - BOARD_SIZE) / 2;
        return boardX + TILE_GAP + column * (TILE_SIZE + TILE_GAP);
    }

    private int cellY(int row) {
        int boardY = 182;
        return boardY + TILE_GAP + row * (TILE_SIZE + TILE_GAP);
    }

    private int interpolate(int start, int end, double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }

    private void drawTile(Graphics2D graphics, int value, int x, int y) {
        graphics.setColor(tileColor(value));
        graphics.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 20, 20);

        if (value == 0) {
            return;
        }

        graphics.setColor(textColor(value));
        graphics.setFont(new Font("SansSerif", Font.BOLD, fontSize(value)));
        FontMetrics metrics = graphics.getFontMetrics();
        String text = String.valueOf(value);
        int textX = x + (TILE_SIZE - metrics.stringWidth(text)) / 2;
        int textY = y + (TILE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics.drawString(text, textX, textY);
    }

    private void drawOverlay(Graphics2D graphics) {
        int boardX = (PANEL_WIDTH - BOARD_SIZE) / 2;
        int boardY = 182;

        graphics.setColor(new Color(57, 45, 79, 168));
        graphics.fillRoundRect(boardX, boardY, BOARD_SIZE, BOARD_SIZE, 26, 26);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 42));
        String headline = won ? "You Win!" : "Game Over";
        centerText(graphics, headline, boardX, boardY + 170, BOARD_SIZE);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 22));
        centerText(graphics, "Press R to play again", boardX, boardY + 215, BOARD_SIZE);
    }

    private void centerText(Graphics2D graphics, String text, int x, int baselineY, int width) {
        FontMetrics metrics = graphics.getFontMetrics();
        int drawX = x + (width - metrics.stringWidth(text)) / 2;
        graphics.drawString(text, drawX, baselineY);
    }

    private int fontSize(int value) {
        if (value < 100) {
            return 34;
        }
        if (value < 1000) {
            return 30;
        }
        return 24;
    }

    private Color tileColor(int value) {
        return switch (value) {
            case 0 -> new Color(206, 193, 181);
            case 2 -> new Color(255, 244, 214);
            case 4 -> new Color(255, 226, 153);
            case 8 -> new Color(255, 176, 122);
            case 16 -> new Color(255, 138, 101);
            case 32 -> new Color(244, 114, 182);
            case 64 -> new Color(236, 72, 153);
            case 128 -> new Color(129, 140, 248);
            case 256 -> new Color(96, 165, 250);
            case 512 -> new Color(45, 212, 191);
            case 1024 -> new Color(74, 222, 128);
            case 2048 -> new Color(250, 204, 21);
            default -> new Color(168, 85, 247);
        };
    }

    private Color textColor(int value) {
        return value <= 4 ? new Color(103, 93, 78) : Color.WHITE;
    }

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private record Cell(int row, int column) {
    }

    private record TileData(int value, int row, int column) {
    }

    private record TileMotion(int value, int fromRow, int fromColumn, int toRow, int toColumn) {
    }

    private record MoveResult(int[][] board, List<TileMotion> motions, int scoreGain, boolean changed) {
    }
}
