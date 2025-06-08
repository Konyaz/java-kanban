package ru.practicum.tracker.util;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.FileBackedTaskManager;
import ru.practicum.tracker.service.TaskManager;

import java.io.File;
import java.io.IOException;

public class Managers {
    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File("tasks.csv"), getDefaultHistory());
    }

    public static TaskManager getFileBackedManager(File file) {
        if (file == null) {
            file = new File("tasks.csv");
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create tasks file", e);
        }
        return new FileBackedTaskManager(file, getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}