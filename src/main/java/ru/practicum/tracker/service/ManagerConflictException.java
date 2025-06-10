package ru.practicum.tracker.service;

public class ManagerConflictException extends RuntimeException {
    public ManagerConflictException(String message) {
        super(message);
    }
}