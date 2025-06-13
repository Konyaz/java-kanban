package ru.practicum.tracker.server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.service.ManagerConflictException;
import ru.practicum.tracker.service.TaskManager;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            switch (method) {
                case "GET":
                    if (pathParts.length == 2) {
                        handleGetAllSubtasks(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetSubtaskById(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateSubtask(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 2) {
                        handleDeleteAllSubtasks(exchange);
                    } else if (pathParts.length == 3) {
                        handleDeleteSubtask(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, GSON.toJson(manager.getAllSubtasks()));
    }

    private void handleGetSubtaskById(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        Subtask subtask = manager.getSubtask(id);
        if (subtask == null) {
            sendNotFound(exchange);
        } else {
            sendText(exchange, GSON.toJson(subtask));
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequest(exchange);
            Subtask subtask = GSON.fromJson(body, Subtask.class);

            if (subtask == null) {
                sendBadRequest(exchange, "Неверный формат подзадачи");
                return;
            }

            if (subtask.getId() == 0) {
                Subtask created = manager.createSubtask(subtask);
                if (created == null) {
                    sendNotFound(exchange);
                } else {
                    sendText(exchange, GSON.toJson(created), 201);
                }
            } else {
                manager.updateSubtask(subtask);
                sendText(exchange, GSON.toJson(subtask));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        } catch (ManagerConflictException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteSubtask(id);
        sendText(exchange, "{\"message\":\"Подзадача удалена\"}");
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        manager.deleteSubtasks();
        sendText(exchange, "{\"message\":\"Все подзадачи удалены\"}");
    }
}