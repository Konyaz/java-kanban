package ru.practicum.tracker.util;


import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.FileBackedTaskManager;
import ru.practicum.tracker.service.TaskManager;

import java.io.File;

public class Managers {
    private static final String DEFAULT_FILE_NAME = "tasks.csv";

    private Managers() {
        // Приватный конструктор, чтобы нельзя было создать экземпляр
    }

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File(DEFAULT_FILE_NAME));
    }

    public static TaskManager getFileBackedManager(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Файл не может быть null");
        }
        return new FileBackedTaskManager(file);
    }

    public static TaskManager getFileBackedManager(File file, HistoryManager historyManager) {
        if (file == null) {
            throw new IllegalArgumentException("Файл не может быть null");
        }
        if (historyManager == null) {
            throw new IllegalArgumentException("HistoryManager не может быть null");
        }
        return new FileBackedTaskManager(file, historyManager);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}