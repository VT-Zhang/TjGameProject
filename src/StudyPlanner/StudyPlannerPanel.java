package StudyPlanner;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StudyPlannerPanel extends JPanel {
    private static final int PANEL_WIDTH = 920;
    private static final int PANEL_HEIGHT = 620;
    private static final Path STORAGE_PATH = Path.of("studyplanner_tasks.txt");

    private final DefaultListModel<StudyTask> listModel = new DefaultListModel<>();
    private final JList<StudyTask> taskList = new JList<>(listModel);
    private final JComboBox<SortOption> sortBox = new JComboBox<>(SortOption.values());
    private final JLabel summaryLabel = new JLabel();

    public StudyPlannerPanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(new Color(236, 240, 245));

        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setFixedCellHeight(56);
        taskList.setCellRenderer(new TaskCellRenderer());

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        loadTasks();
        if (listModel.isEmpty()) {
            seedSampleTasks();
        }
        sortTasks();
        updateSummary();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Study Planner");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel subtitleLabel = new JLabel("Track assignments, due dates, and progress.");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(75, 85, 99));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlsPanel.setOpaque(false);
        controlsPanel.add(new JLabel("Sort by:"));
        sortBox.addActionListener(event -> sortTasks());
        controlsPanel.add(sortBox);

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(controlsPanel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane buildCenterPanel() {
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(205, 213, 224)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        summaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add Task");
        JButton editButton = new JButton("Edit");
        JButton toggleButton = new JButton("Mark Complete");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(event -> addTask());
        editButton.addActionListener(event -> editTask());
        toggleButton.addActionListener(event -> toggleCompleted());
        deleteButton.addActionListener(event -> deleteTask());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(deleteButton);

        panel.add(summaryLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private void addTask() {
        TaskDialog dialog = new TaskDialog(SwingUtilities.getWindowAncestor(this), "Add Task", null);
        dialog.setVisible(true);

        StudyTask task = dialog.getResult();
        if (task != null) {
            listModel.addElement(task);
            afterTaskDataChanged();
        }
    }

    private void editTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex < 0) {
            showSelectionMessage("Select a task to edit.");
            return;
        }

        StudyTask selectedTask = listModel.getElementAt(selectedIndex);
        TaskDialog dialog = new TaskDialog(SwingUtilities.getWindowAncestor(this), "Edit Task", selectedTask);
        dialog.setVisible(true);

        StudyTask updatedTask = dialog.getResult();
        if (updatedTask != null) {
            listModel.set(selectedIndex, updatedTask);
            afterTaskDataChanged();
        }
    }

    private void toggleCompleted() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex < 0) {
            showSelectionMessage("Select a task to update.");
            return;
        }

        StudyTask task = listModel.getElementAt(selectedIndex);
        task.setCompleted(!task.isCompleted());
        taskList.repaint();
        afterTaskDataChanged();
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex < 0) {
            showSelectionMessage("Select a task to delete.");
            return;
        }

        StudyTask task = listModel.getElementAt(selectedIndex);
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + task.getTitle() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            listModel.remove(selectedIndex);
            afterTaskDataChanged();
        }
    }

    private void afterTaskDataChanged() {
        sortTasks();
        saveTasks();
        updateSummary();
    }

    private void sortTasks() {
        List<StudyTask> tasks = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            tasks.add(listModel.get(i));
        }

        SortOption selectedOption = (SortOption) sortBox.getSelectedItem();
        if (selectedOption == null) {
            selectedOption = SortOption.DUE_DATE;
        }

        tasks.sort(comparatorFor(selectedOption));

        listModel.clear();
        for (StudyTask task : tasks) {
            listModel.addElement(task);
        }
    }

    private Comparator<StudyTask> comparatorFor(SortOption option) {
        Comparator<StudyTask> baseComparator = switch (option) {
            case SUBJECT -> Comparator.comparing(StudyTask::getSubject, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(StudyTask::getDueDate)
                    .thenComparing(StudyTask::getTitle, String.CASE_INSENSITIVE_ORDER);
            case PRIORITY -> Comparator.comparing(StudyTask::getPriority)
                    .thenComparing(StudyTask::getDueDate)
                    .thenComparing(StudyTask::getTitle, String.CASE_INSENSITIVE_ORDER);
            case STATUS -> Comparator.comparing(StudyTask::isCompleted)
                    .thenComparing(StudyTask::getDueDate)
                    .thenComparing(StudyTask::getTitle, String.CASE_INSENSITIVE_ORDER);
            case DUE_DATE -> Comparator.comparing(StudyTask::getDueDate)
                    .thenComparing(StudyTask::getPriority)
                    .thenComparing(StudyTask::getTitle, String.CASE_INSENSITIVE_ORDER);
        };

        return Comparator.comparing(StudyTask::isCompleted)
                .thenComparing(baseComparator);
    }

    private void updateSummary() {
        int total = listModel.size();
        int completed = 0;
        int overdue = 0;
        for (int i = 0; i < listModel.size(); i++) {
            StudyTask task = listModel.get(i);
            if (task.isCompleted()) {
                completed++;
            }
            if (task.isOverdue()) {
                overdue++;
            }
        }
        summaryLabel.setText("Tasks: " + total + "   Completed: " + completed + "   Overdue: " + overdue);
    }

    private void saveTasks() {
        try (BufferedWriter writer = Files.newBufferedWriter(STORAGE_PATH)) {
            for (int i = 0; i < listModel.size(); i++) {
                writer.write(listModel.get(i).toStorageString());
                writer.newLine();
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, "Could not save tasks.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        if (!Files.exists(STORAGE_PATH)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(STORAGE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    listModel.addElement(StudyTask.fromStorageString(line));
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, "Could not load saved tasks.", "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seedSampleTasks() {
        listModel.addElement(new StudyTask("Finish algebra worksheet", "Math", LocalDate.now().plusDays(1), Priority.HIGH, false));
        listModel.addElement(new StudyTask("Read chapter 4", "History", LocalDate.now().plusDays(3), Priority.MEDIUM, false));
        listModel.addElement(new StudyTask("Review lab notes", "Biology", LocalDate.now().minusDays(1), Priority.HIGH, false));
        saveTasks();
    }

    private void showSelectionMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof StudyTask task) {
                label.setText(buildTaskText(task));
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

                if (isSelected) {
                    label.setBackground(new Color(59, 130, 246));
                    label.setForeground(Color.WHITE);
                } else if (task.isCompleted()) {
                    label.setBackground(new Color(230, 244, 234));
                    label.setForeground(new Color(45, 93, 55));
                } else if (task.isOverdue()) {
                    label.setBackground(new Color(255, 235, 238));
                    label.setForeground(new Color(153, 27, 27));
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(33, 37, 41));
                }
                label.setOpaque(true);
            }

            return label;
        }

        private String buildTaskText(StudyTask task) {
            String status = task.isCompleted() ? "Completed" : (task.isOverdue() ? "Overdue" : "Open");
            return "<html><b>" + escapeHtml(task.getTitle()) + "</b> &nbsp; | &nbsp; "
                    + escapeHtml(task.getSubject()) + " &nbsp; | &nbsp; Due: "
                    + task.getDueDate() + " &nbsp; | &nbsp; Priority: "
                    + task.getPriority() + " &nbsp; | &nbsp; " + status + "</html>";
        }

        private String escapeHtml(String value) {
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
