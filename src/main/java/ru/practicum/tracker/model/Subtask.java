package ru.practicum.tracker.model;

public class Subtask extends Task {
    private int epicId;

    // Конструктор без статуса, статус по умолчанию NEW
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    // Новый конструктор с передачей статуса
    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public Subtask copy() {
        Subtask copy = new Subtask(this.getName(), this.getDescription(), this.getStatus(), this.epicId);
        copy.setId(this.getId());
        return copy;
    }
}
