package TicTacToe;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TicTacToeFrame frame = new TicTacToeFrame();
            frame.setVisible(true);
        });
    }
}
