package BlackJack;

import javax.swing.*;

public class BlackjackFrame extends JFrame {
    public BlackjackFrame() {
        setTitle("Blackjack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new BlackjackPanel());
        pack();
        setLocationRelativeTo(null);
    }
}
