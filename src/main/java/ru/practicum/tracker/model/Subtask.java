package ru.practicum.tracker.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int id, int epicId) {
        super(name, description, id);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    // Новый метод для создания копии объекта
    @Override
    public Subtask copy() {
        Subtask copy = new Subtask(this.name, this.description, this.id, this.epicId);
        copy.setStatus(this.status);
        return copy;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                '}';
    }
}
