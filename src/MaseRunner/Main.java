package MaseRunner;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeRunnerFrame frame = new MazeRunnerFrame();
            frame.setVisible(true);
        });
    }
}
