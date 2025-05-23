package ru.practicum.tracker.model;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;

    // Конструктор без статуса, статус по умолчанию NEW
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    // Новый конструктор с передачей статуса
    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    // Геттеры и сеттеры

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    // Добавляем сеттер для name, если нужен (по желанию)
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    // Добавляем необходимый сеттер
    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Метод возвращает тип задачи (TASK)
    public TaskType getType() {
        return TaskType.TASK;
    }

    // Метод для копирования задачи
    public Task copy() {
        Task copy = new Task(this.name, this.description, this.status);
        copy.setId(this.id);
        return copy;
    }
}
