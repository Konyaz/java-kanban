package ru.practicum.tracker.service;

import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int counterId = 0;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public Task createTask(Task task) {
        final int id = ++counterId;
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        final int id = ++counterId;
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return null;
        }
        final int id = ++counterId;
        subtask.setId(id);
        subtasks.put(id, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(epic.getId());
        }
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

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = this.epics.get(epicId);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                result.add(getSubtask(subId));
            }
        }
        return result;
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
        tasks.remove(id);
    }

    public void deleteSubtask(int id) {
        Subtask subtask = this.subtasks.get(id);
        if (subtask != null) {
            Epic epic = this.epics.get(subtask.getEpicId());
            if (epic != null) {
                epics.remove(id);
                updateEpicStatus(epic.getId());
            }
            subtasks.remove(id);
        }
    }

    public void deleteEpic(int id) {
        Epic epic = this.epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
        }
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = this.epics.get(epicId);
        if (epic == null) return;
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (int id : subtaskIds) {
            TaskStatus status = this.subtasks.get(id).getStatus();
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