package ru.practicum.tracker.model;

import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description, int id) {
        super(name, description, id);
        this.subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }
}