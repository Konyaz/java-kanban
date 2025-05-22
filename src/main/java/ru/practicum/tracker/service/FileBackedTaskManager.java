package ru.practicum.tracker.service;

import ru.practicum.tracker.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    private void load() {
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length <= 1) {
                return;
            }

            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task != null) {
                    switch (task.getType()) {
                        case TASK:
                            createTask(task);
                            break;
                        case EPIC:
                            createEpic((Epic) task);
                            break;
                        case SUBTASK:
                            createSubtask((Subtask) task);
                            break;
                    }
                }
            }

            // Восстановление связей подзадач с эпиками
            for (Subtask subtask : getAllSubtasks()) {
                Epic epic = getEpic(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке файла", e);
        }
    }

    private String toString(Task task) {
        if (task == null) {
            return "";
        }

        String type = task.getType().name();
        String epicId = (task.getType() == TaskType.SUBTASK) ?
                String.valueOf(((Subtask) task).getEpicId()) : "";

        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
        );
    }

    private Task fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] parts = value.split(",");
        if (parts.length < 6) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        String epicId = parts.length > 5 ? parts[5] : "";

        switch (type) {
            case TASK:
                Task task = new Task(name, description, id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description, id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                if (epicId.isEmpty()) {
                    return null;
                }
                Subtask subtask = new Subtask(name, description, id, Integer.parseInt(epicId));
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }
}