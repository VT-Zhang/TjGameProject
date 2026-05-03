package GameOfLife;

import javax.swing.JFrame;

public class GameOfLifeFrame extends JFrame {
    public GameOfLifeFrame() {
        setTitle("Conway's Game of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GameOfLifePanel());
        pack();
        setLocationRelativeTo(null);
    }
}
