package GameOfLife;

public class LifeRules {
    public int countLiveNeighbors(GameOfLifeBoard board, int row, int col) {
        int count = 0;

        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                if (rowOffset == 0 && colOffset == 0) {
                    continue;
                }

                int wrappedRow = board.getWrappedRow(row + rowOffset);
                int wrappedCol = board.getWrappedCol(col + colOffset);
                if (board.isAlive(wrappedRow, wrappedCol)) {
                    count++;
                }
            }
        }

        return count;
    }

    public boolean computeNextState(GameOfLifeBoard board, int row, int col) {
        int liveNeighbors = countLiveNeighbors(board, row, col);
        boolean alive = board.isAlive(row, col);

        if (alive) {
            return liveNeighbors == 2 || liveNeighbors == 3;
        }

        return liveNeighbors == 3;
    }

    public void step(GameOfLifeBoard board) {
        for (int row = 0; row < GameOfLifeBoard.ROWS; row++) {
            for (int col = 0; col < GameOfLifeBoard.COLS; col++) {
                boolean nextAlive = computeNextState(board, row, col);
                board.setNextCell(row, col, nextAlive);

                int nextAge = nextAlive ? board.getCellAge(row, col) + 1 : 0;
                if (nextAlive && !board.isAlive(row, col)) {
                    nextAge = 1;
                }

                board.setNextCellAge(row, col, nextAge);
            }
        }

        board.swapGenerations();
    }
}
