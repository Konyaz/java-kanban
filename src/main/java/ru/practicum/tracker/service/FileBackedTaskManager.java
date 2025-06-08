package ru.practicum.tracker.service;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            reader.readLine(); // пропускаем заголовок

            boolean isHistorySection = false;
            List<Integer> historyIds = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistorySection = true;
                    continue;
                }

                if (!isHistorySection) {
                    Task task = manager.fromString(line);
                    if (task != null) {
                        int id = task.getId();
                        switch (task.getType()) {
                            case TASK:
                                manager.tasks.put(id, task);
                                break;
                            case EPIC:
                                manager.epics.put(id, (Epic) task);
                                break;
                            case SUBTASK:
                                Subtask subtask = (Subtask) task;
                                manager.subtasks.put(id, subtask);
                                Epic epic = manager.epics.get(subtask.getEpicId());
                                if (epic != null) {
                                    epic.addSubtaskId(id);
                                }
                                break;
                        }
                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }
                        manager.counterId = Math.max(manager.counterId, id + 1);
                    }
                } else {
                    historyIds = historyFromString(line);
                }
            }

            // Обновляем время и статус эпиков после загрузки всех задач
            manager.epics.values().forEach(epic -> {
                manager.updateEpicTime(epic);
                manager.updateEpicStatus(epic);
            });

            // Восстанавливаем историю
            for (int id : historyIds) {
                if (manager.tasks.containsKey(id)) {
                    manager.historyManager.add(manager.tasks.get(id));
                } else if (manager.epics.containsKey(id)) {
                    manager.historyManager.add(manager.epics.get(id));
                } else if (manager.subtasks.containsKey(id)) {
                    manager.historyManager.add(manager.subtasks.get(id));
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + file.getName(), e);
        }

        return manager;
    }

    public void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.println("id,type,name,status,description,epic,duration,startTime");

            for (Task task : tasks.values()) {
                writer.println(toString(task));
            }
            for (Epic epic : epics.values()) {
                writer.println(toString(epic));
            }
            for (Subtask subtask : subtasks.values()) {
                writer.println(toString(subtask));
            }

            writer.println();
            writer.println(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + file.getName(), e);
        }
    }

    protected String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(escapeCommas(task.getName())).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(escapeCommas(task.getDescription())).append(",");

        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }
        sb.append(",");

        if (task.getDuration() != null) {
            sb.append(task.getDuration().toMinutes());
        }
        sb.append(",");

        if (task.getStartTime() != null) {
            sb.append(task.getStartTime().format(DATE_TIME_FORMATTER));
        }

        return sb.toString();
    }

    protected Task fromString(String value) {
        String[] parts = value.split(",", 8);
        if (parts.length < 5) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = unescapeCommas(parts[2]);
            TaskStatus status = TaskStatus.valueOf(parts[3]);
            String description = unescapeCommas(parts[4]);

            Duration duration = null;
            if (parts.length > 6 && !parts[6].isEmpty()) {
                duration = Duration.ofMinutes(Long.parseLong(parts[6]));
            }

            LocalDateTime startTime = null;
            if (parts.length > 7 && !parts[7].isEmpty()) {
                startTime = LocalDateTime.parse(parts[7], DATE_TIME_FORMATTER);
            }

            switch (type) {
                case TASK:
                    Task task = new Task(name, description, status);
                    task.setId(id);
                    task.setDuration(duration);
                    task.setStartTime(startTime);
                    return task;
                case EPIC:
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setStatus(status);
                    epic.setDuration(duration);
                    epic.setStartTime(startTime);
                    return epic;
                case SUBTASK:
                    if (parts.length < 6 || parts[5].isEmpty()) {
                        return null;
                    }
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description, status, epicId);
                    subtask.setId(id);
                    subtask.setDuration(duration);
                    subtask.setStartTime(startTime);
                    return subtask;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    protected static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        List<String> ids = new ArrayList<>();
        for (Task task : history) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    protected static List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return history;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                history.add(Integer.parseInt(part));
            } catch (NumberFormatException ignored) {
            }
        }
        return history;
    }

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        save();
        return created;
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
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    private String escapeCommas(String input) {
        if (input == null) {
            return "";
        }
        return input.replace(",", "\\,");
    }

    private String unescapeCommas(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\,", ",");
    }
}