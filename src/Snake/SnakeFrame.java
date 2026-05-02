package Snake;

import javax.swing.*;

public class SnakeFrame extends JFrame {
    public SnakeFrame() {
        setTitle("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new SnakePanel());
        pack();
        setLocationRelativeTo(null);
    }
}
