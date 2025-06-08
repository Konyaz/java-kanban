package ru.practicum.tracker.util;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.FileBackedTaskManager;
import ru.practicum.tracker.service.InMemoryTaskManager;
import ru.practicum.tracker.service.TaskManager;

import java.io.File;
import java.io.IOException;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static TaskManager getFileBackedManager(File file) {
        if (file == null) {
            file = new File("tasks.csv");
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            return FileBackedTaskManager.loadFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create tasks file", e);
        }
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}