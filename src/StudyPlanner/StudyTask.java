package StudyPlanner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class StudyTask {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private String title;
    private String subject;
    private LocalDate dueDate;
    private Priority priority;
    private boolean completed;

    public StudyTask(String title, String subject, LocalDate dueDate, Priority priority, boolean completed) {
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isOverdue() {
        return !completed && dueDate.isBefore(LocalDate.now());
    }

    public String toStorageString() {
        return escape(title) + "\t"
                + escape(subject) + "\t"
                + dueDate + "\t"
                + priority.name() + "\t"
                + completed;
    }

    public static StudyTask fromStorageString(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid task record");
        }
        return new StudyTask(
                unescape(parts[0]),
                unescape(parts[1]),
                LocalDate.parse(parts[2]),
                Priority.valueOf(parts[3]),
                Boolean.parseBoolean(parts[4])
        );
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (escaping) {
                if (current == 't') {
                    builder.append('\t');
                } else if (current == 'n') {
                    builder.append('\n');
                } else {
                    builder.append(current);
                }
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else {
                builder.append(current);
            }
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        String status = completed ? "Done" : "Open";
        return title + " | " + subject + " | " + dueDate.format(DISPLAY_FORMAT) + " | " + priority + " | " + status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StudyTask task)) {
            return false;
        }
        return completed == task.completed
                && Objects.equals(title, task.title)
                && Objects.equals(subject, task.subject)
                && Objects.equals(dueDate, task.dueDate)
                && priority == task.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, subject, dueDate, priority, completed);
    }
}
