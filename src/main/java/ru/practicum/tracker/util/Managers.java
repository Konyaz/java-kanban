package ru.practicum.tracker.util;

import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.history.InMemoryHistoryManager;
import ru.practicum.tracker.service.TaskManager;
import ru.practicum.tracker.service.InMemoryTaskManager;

public class Managers {
    private Managers() {
    } // Закрывающая скобка перенесена на новую строку

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}