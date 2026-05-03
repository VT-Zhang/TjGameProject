package StudyPlanner;

public enum SortOption {
    DUE_DATE("Due Date"),
    SUBJECT("Subject"),
    PRIORITY("Priority"),
    STATUS("Status");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
