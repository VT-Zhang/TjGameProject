package StudyPlanner;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudyPlannerFrame frame = new StudyPlannerFrame();
            frame.setVisible(true);
        });
    }
}
