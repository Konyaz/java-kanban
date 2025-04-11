package ru.practicum.tracker.util;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.TaskManager;
import ru.practicum.tracker.service.InMemoryTaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}