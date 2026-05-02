package Game2048;

import javax.swing.*;

public class Game2048Frame extends JFrame {
    public Game2048Frame() {
        setTitle("2048");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new Game2048Panel());
        pack();
        setLocationRelativeTo(null);
    }
}
