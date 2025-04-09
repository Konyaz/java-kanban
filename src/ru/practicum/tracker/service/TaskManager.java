package ru.practicum.tracker.service;

import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {


    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public Task createTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return null;
        }
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получить задачу по ID
    public Task getTask(int id) {
        return tasks.get(id);
    }

    // Получить эпик по ID
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    // Получить подзадачу по ID
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    public void deleteTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            tasks.remove(id);
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id); // Используем метод getSubtask
        if (subtask != null) {
            Epic epic = getEpic(subtask.getEpicId()); // Используем метод getEpic
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
            subtasks.remove(id);
        }
    }

    public void deleteEpic(int id) {
        Epic epic = getEpic(id); // Используем метод getEpic
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                deleteSubtask(subtaskId); // Удаляем все подзадачи эпика
            }
            epics.remove(id);
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = getEpic(epicId); // Используем метод getEpic
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subtaskIds) {
            TaskStatus status = getSubtask(id).getStatus(); // Используем getSubtask
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}