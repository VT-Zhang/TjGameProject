package GameOfLife;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameOfLifeFrame frame = new GameOfLifeFrame();
            frame.setVisible(true);
        });
    }
}
