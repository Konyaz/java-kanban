package ru.practicum.tracker.model;

import ru.practicum.tracker.service.TaskManagerProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;
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
        return subtaskIds;
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
        if (subtaskIds.isEmpty()) {
            return Duration.ZERO;
        }
        return Duration.ofMinutes(subtaskIds.stream()
                .mapToLong(id -> {
                    Task subtask = TaskManagerProvider.getManager().getSubtask(id);
                    return subtask != null && subtask.getDuration() != null ? subtask.getDuration().toMinutes() : 0;
                })
                .sum());
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtaskIds.isEmpty()) {
            return null;
        }
        return subtaskIds.stream()
                .map(id -> TaskManagerProvider.getManager().getSubtask(id))
                .filter(subtask -> subtask != null && subtask.getStartTime() != null)
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(this.getName(), this.getDescription());
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        copy.subtaskIds = new ArrayList<>(this.subtaskIds);
        copy.setDuration(this.getDuration());
        copy.setStartTime(this.getStartTime());
        copy.setEndTime(this.getEndTime());
        return copy;
    }
}