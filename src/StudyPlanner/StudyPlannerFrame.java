package StudyPlanner;

import javax.swing.*;

public class StudyPlannerFrame extends JFrame {
    public StudyPlannerFrame() {
        setTitle("Study Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new StudyPlannerPanel());
        pack();
        setLocationRelativeTo(null);
    }
}
