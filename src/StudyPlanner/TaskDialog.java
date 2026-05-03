package StudyPlanner;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TaskDialog extends JDialog {
    private final JTextField titleField = new JTextField(20);
    private final JTextField subjectField = new JTextField(20);
    private final JTextField dueDateField = new JTextField(20);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JCheckBox completedBox = new JCheckBox("Completed");

    private StudyTask result;

    public TaskDialog(Window owner, String title, StudyTask existingTask) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        buildUi(existingTask);
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi(StudyTask existingTask) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        formPanel.setBackground(new Color(245, 247, 250));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        addField(formPanel, constraints, 0, "Task Title", titleField);
        addField(formPanel, constraints, 1, "Subject", subjectField);
        addField(formPanel, constraints, 2, "Due Date (YYYY-MM-DD)", dueDateField);
        addField(formPanel, constraints, 3, "Priority", priorityBox);

        constraints.gridx = 1;
        constraints.gridy = 4;
        formPanel.add(completedBox, constraints);

        if (existingTask != null) {
            titleField.setText(existingTask.getTitle());
            subjectField.setText(existingTask.getSubject());
            dueDateField.setText(existingTask.getDueDate().toString());
            priorityBox.setSelectedItem(existingTask.getPriority());
            completedBox.setSelected(existingTask.isCompleted());
        }

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(event -> saveTask());
        cancelButton.addActionListener(event -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 10, 8));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(saveButton);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row, String label, JComponent field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(label + ":"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        panel.add(field, constraints);
    }

    private void saveTask() {
        String title = titleField.getText().trim();
        String subject = subjectField.getText().trim();
        String dueDateText = dueDateField.getText().trim();
        Priority priority = (Priority) priorityBox.getSelectedItem();

        if (title.isEmpty() || subject.isEmpty() || dueDateText.isEmpty() || priority == null) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateText);
        } catch (DateTimeParseException exception) {
            JOptionPane.showMessageDialog(this, "Use date format YYYY-MM-DD.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        result = new StudyTask(title, subject, dueDate, priority, completedBox.isSelected());
        dispose();
    }

    public StudyTask getResult() {
        return result;
    }
}
