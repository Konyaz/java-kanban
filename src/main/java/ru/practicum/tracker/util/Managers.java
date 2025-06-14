package ru.practicum.tracker.util;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.FileBackedTaskManager;
import ru.practicum.tracker.service.InMemoryTaskManager;
import ru.practicum.tracker.service.TaskManager;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static TaskManager getFileBackedManager(File file) {
        return new FileBackedTaskManager(file);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}