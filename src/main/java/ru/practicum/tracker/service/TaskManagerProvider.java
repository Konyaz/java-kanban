package ru.practicum.tracker.service;

public class TaskManagerProvider {
    private static TaskManager manager;

    public static void setManager(TaskManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Manager cannot be null");
        }
        TaskManagerProvider.manager = manager;
    }

    public static TaskManager getManager() {
        if (manager == null) {
            throw new IllegalStateException("Manager has not been initialized");
        }
        return manager;
    }
}