package FlappyBird;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlappyBirdFrame frame = new FlappyBirdFrame();
            frame.setVisible(true);
        });
    }
}
