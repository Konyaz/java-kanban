package ru.practicum.tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int id) {
        if (!subtaskIds.contains(id)) {
            subtaskIds.add(id);
        }
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove((Integer) id);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(this.getName(), this.getDescription());
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        copy.subtaskIds.addAll(this.subtaskIds);
        copy.setDuration(this.getDuration());
        copy.setStartTime(this.getStartTime());
        copy.setEndTime(this.getEndTime());
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) &&
                Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, endTime);
    }
}