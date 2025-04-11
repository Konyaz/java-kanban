package ru.practicum.tracker.history;

import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_LIMIT = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        Task cloned = cloneTask(task);
        history.addLast(cloned);
        if (history.size() > HISTORY_LIMIT) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    private Task cloneTask(Task task) {
        if (task instanceof Subtask) {
            Subtask sub = (Subtask) task;
            Subtask copy = new Subtask(sub.getName(), sub.getDescription(), sub.getEpicId());
            copy.setId(sub.getId());
            copy.setStatus(sub.getStatus());
            return copy;
        } else if (task instanceof Epic) {
            Epic epic = (Epic) task;
            Epic copy = new Epic(epic.getName(), epic.getDescription());
            copy.setId(epic.getId());
            copy.setStatus(epic.getStatus());
            copy.getSubtaskIds().addAll(epic.getSubtaskIds());
            return copy;
        } else {
            Task copy = new Task(task.getName(), task.getDescription());
            copy.setId(task.getId());
            copy.setStatus(task.getStatus());
            return copy;
        }
    }
}