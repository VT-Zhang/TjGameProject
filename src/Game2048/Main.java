package Game2048;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game2048Frame frame = new Game2048Frame();
            frame.setVisible(true);
        });
    }
}
