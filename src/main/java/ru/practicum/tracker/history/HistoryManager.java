package ru.practicum.tracker.history;

import ru.practicum.tracker.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}