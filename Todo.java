public class Todo {
    private String id;
    private String content;
    private String subject;
    private String learnedNote;
    private boolean completed;

    public Todo(String id, String content, String subject, String learnedNote, boolean completed) {
        this.id = id;
        this.content = content;
        this.subject = subject;
        this.learnedNote = learnedNote;
        this.completed = completed;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public String getSubject() { return subject; }
    public String getLearnedNote() { return learnedNote; }
    public boolean isCompleted() { return completed; }

    public void setLearnedNote(String learnedNote) { this.learnedNote = learnedNote; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}