package ru.practicum.tracker.model;

import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description, int id) {
        super(name, description, id);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return "Эпик{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", подзадачи=" + subtaskIds +
                '}';
    }
}