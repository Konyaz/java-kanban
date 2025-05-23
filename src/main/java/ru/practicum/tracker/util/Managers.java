package ru.practicum.tracker.util;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.FileBackedTaskManager;
import ru.practicum.tracker.service.TaskManager;

import java.io.File;

public class Managers {
    private static final String DEFAULT_FILE_NAME = "tasks.csv";

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File(DEFAULT_FILE_NAME));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}