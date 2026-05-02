package FlappyBird;

import javax.swing.*;

public class FlappyBirdFrame extends JFrame {
    public FlappyBirdFrame() {
        setTitle("Flappy Bird");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new FlappyBirdPanel());
        pack();
        setLocationRelativeTo(null);
    }
}
