package ru.practicum.tracker.service;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.model.TaskStatus;
import ru.practicum.tracker.model.TaskType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    // Добавлен геттер для поля file
    public File getFile() {
        return file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,duration,startTime,epic\n");

            // Записываем задачи
            for (Task task : tasks.values()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask) + "\n");
            }

            // Записываем историю
            writer.write("\n");
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage());
        }
    }

    private String toString(Task task) {
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().format(FORMATTER) : "";
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                duration,
                startTime,
                epicId);
    }

    private static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        if (history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Task task : history) {
            sb.append(task.getId()).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Пропускаем заголовок
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break; // Пустая строка отделяет задачи от истории
                }
                Task task = fromString(line);
                if (task != null) {
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                    if (task.getStartTime() != null) {
                        manager.prioritizedTasks.add(task);
                    }
                }
            }

            // Читаем историю
            String historyLine = reader.readLine();
            if (historyLine != null && !historyLine.isEmpty()) {
                List<Integer> historyIds = historyFromString(historyLine);
                for (Integer id : historyIds) {
                    Task task = manager.tasks.get(id);
                    if (task == null) {
                        task = manager.epics.get(id);
                    }
                    if (task == null) {
                        task = manager.subtasks.get(id);
                    }
                    if (task != null) {
                        manager.historyManager.add(task);
                    }
                }
            }

            // Обновляем счетчик ID
            int maxId = Math.max(
                    manager.tasks.keySet().stream().mapToInt(Integer::intValue).max().orElse(0),
                    Math.max(
                            manager.epics.keySet().stream().mapToInt(Integer::intValue).max().orElse(0),
                            manager.subtasks.keySet().stream().mapToInt(Integer::intValue).max().orElse(0)
                    )
            );
            manager.counterId = maxId + 1;

            // Обновляем статусы и время эпиков
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic);
                manager.updateEpicTime(epic);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + e.getMessage());
        }
        return manager;
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parts[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[5]));
        LocalDateTime startTime = parts[6].isEmpty() ? null : LocalDateTime.parse(parts[6], FORMATTER);

        Task task;
        switch (type) {
            case TASK:
                task = new Task(name, description, status, duration, startTime);
                break;
            case EPIC:
                task = new Epic(name, description);
                ((Epic) task).setStatus(status);
                ((Epic) task).setDuration(duration);
                ((Epic) task).setStartTime(startTime);
                ((Epic) task).setEndTime(startTime != null && duration != null ? startTime.plus(duration) : null);
                break;
            case SUBTASK:
                if (parts.length < 8) {
                    return null;
                }
                int epicId = Integer.parseInt(parts[7]);
                task = new Subtask(name, description, status, epicId, duration, startTime);
                break;
            default:
                return null;
        }
        task.setId(id);
        return task;
    }

    private static List<Integer> historyFromString(String line) {
        List<Integer> historyIds = new ArrayList<>();
        if (line.isEmpty()) {
            return historyIds;
        }
        for (String id : line.split(",")) {
            historyIds.add(Integer.parseInt(id));
        }
        return historyIds;
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
        if (createdSubtask != null) {
            save();
        }
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

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }
}