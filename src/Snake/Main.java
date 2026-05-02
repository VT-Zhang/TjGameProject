package Snake;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SnakeFrame frame = new SnakeFrame();
            frame.setVisible(true);
        });
    }
}
