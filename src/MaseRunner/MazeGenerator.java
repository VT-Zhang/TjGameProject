package MaseRunner;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeGenerator {
    private final Random random = new Random();

    public boolean[][] generate(int rows, int cols) {
        boolean[][] maze = new boolean[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                maze[row][col] = true;
            }
        }

        carvePassages(1, 1, maze);
        maze[1][1] = false;
        maze[rows - 2][cols - 2] = false;
        return maze;
    }

    private void carvePassages(int row, int col, boolean[][] maze) {
        maze[row][col] = false;

        List<Point> directions = new ArrayList<>();
        directions.add(new Point(0, -2));
        directions.add(new Point(2, 0));
        directions.add(new Point(0, 2));
        directions.add(new Point(-2, 0));
        Collections.shuffle(directions, random);

        // Jump two cells at a time so we keep walls between rooms, then remove
        // the wall in the middle when we connect to a new unvisited cell.
        for (Point direction : directions) {
            int nextRow = row + direction.x;
            int nextCol = col + direction.y;

            if (!isInside(nextRow, nextCol, maze) || !maze[nextRow][nextCol]) {
                continue;
            }

            maze[row + direction.x / 2][col + direction.y / 2] = false;
            carvePassages(nextRow, nextCol, maze);
        }
    }

    private boolean isInside(int row, int col, boolean[][] maze) {
        return row > 0 && row < maze.length - 1 && col > 0 && col < maze[0].length - 1;
    }
}
