package ru.practicum.tracker.server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.service.ManagerConflictException;
import ru.practicum.tracker.service.TaskManager;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
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
                        handleGetAllTasks(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetTaskById(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateTask(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 2) {
                        handleDeleteAllTasks(exchange);
                    } else if (pathParts.length == 3) {
                        handleDeleteTask(exchange, pathParts[2]);
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

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, GSON.toJson(manager.getAllTasks()));
    }

    private void handleGetTaskById(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        Task task = manager.getTask(id);
        if (task == null) {
            sendNotFound(exchange);
        } else {
            sendText(exchange, GSON.toJson(task));
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequest(exchange);
            Task task = GSON.fromJson(body, Task.class);

            if (task == null) {
                sendBadRequest(exchange, "Неверный формат задачи");
                return;
            }

            if (task.getId() == 0) {
                Task created = manager.createTask(task);
                sendText(exchange, GSON.toJson(created), 201);
            } else {
                manager.updateTask(task);
                sendText(exchange, GSON.toJson(task));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        } catch (ManagerConflictException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteTask(id);
        sendText(exchange, "{\"message\":\"Задача удалена\"}");
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        manager.deleteTasks();
        sendText(exchange, "{\"message\":\"Все задачи удалены\"}");
    }
}