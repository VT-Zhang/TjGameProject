package GameOfLife;

import java.util.Random;

public class GameOfLifeBoard {
    public static final int ROWS = 32;
    public static final int COLS = 32;

    private boolean[][] currentCells = new boolean[ROWS][COLS];
    private boolean[][] nextCells = new boolean[ROWS][COLS];
    private int[][] cellAges = new int[ROWS][COLS];
    private int[][] nextCellAges = new int[ROWS][COLS];

    public boolean isAlive(int row, int col) {
        return currentCells[row][col];
    }

    public void setAlive(int row, int col, boolean alive) {
        currentCells[row][col] = alive;
        cellAges[row][col] = alive ? 1 : 0;
    }

    public void setNextCell(int row, int col, boolean alive) {
        nextCells[row][col] = alive;
    }

    public void setNextCellAge(int row, int col, int age) {
        nextCellAges[row][col] = age;
    }

    public int getCellAge(int row, int col) {
        return cellAges[row][col];
    }

    public void clear() {
        currentCells = new boolean[ROWS][COLS];
        nextCells = new boolean[ROWS][COLS];
        cellAges = new int[ROWS][COLS];
        nextCellAges = new int[ROWS][COLS];
    }

    public void randomize(Random random) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                boolean alive = random.nextDouble() < 0.32;
                currentCells[row][col] = alive;
                nextCells[row][col] = false;
                cellAges[row][col] = alive ? random.nextInt(6) + 1 : 0;
                nextCellAges[row][col] = 0;
            }
        }
    }

    public void swapGenerations() {
        boolean[][] currentSwap = currentCells;
        currentCells = nextCells;
        nextCells = currentSwap;

        int[][] ageSwap = cellAges;
        cellAges = nextCellAges;
        nextCellAges = ageSwap;
    }

    public int getWrappedRow(int row) {
        return (row + ROWS) % ROWS;
    }

    public int getWrappedCol(int col) {
        return (col + COLS) % COLS;
    }
}
