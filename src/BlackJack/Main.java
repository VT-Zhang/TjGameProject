package BlackJack;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BlackjackFrame frame = new BlackjackFrame();
            frame.setVisible(true);
        });
    }
}
