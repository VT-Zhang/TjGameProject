package TicTacToe;

import javax.swing.JFrame;

public class TicTacToeFrame extends JFrame {
    public TicTacToeFrame() {
        setTitle("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new TicTacToePanel());
        pack();
        setLocationRelativeTo(null);
    }
}
