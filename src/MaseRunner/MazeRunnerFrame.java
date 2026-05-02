package MaseRunner;

import javax.swing.*;

public class MazeRunnerFrame extends JFrame {
    public MazeRunnerFrame() {
        setTitle("Maze Runner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new MazeRunnerPanel());
        pack();
        setLocationRelativeTo(null);
    }
}
