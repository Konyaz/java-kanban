package ru.practicum.tracker.server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.service.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicHandler(TaskManager manager) {
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
                        handleGetAllEpics(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetEpicById(exchange, pathParts[2]);
                    } else if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                        handleGetEpicSubtasks(exchange, pathParts[2]);
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateEpic(exchange);
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        handleDeleteEpic(exchange, pathParts[2]);
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

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, GSON.toJson(manager.getAllEpics()));
    }

    private void handleGetEpicById(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        Epic epic = manager.getEpic(id);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            sendText(exchange, GSON.toJson(epic));
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        Epic epic = manager.getEpic(id);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            List<Subtask> subtasks = manager.getEpicSubtasks(id);
            sendText(exchange, GSON.toJson(subtasks));
        }
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        try {
            String body = readRequest(exchange);
            Epic epic = GSON.fromJson(body, Epic.class);

            if (epic == null) {
                sendBadRequest(exchange, "Неверный формат эпика");
                return;
            }

            if (epic.getId() == 0) {
                Epic created = manager.createEpic(epic);
                sendText(exchange, GSON.toJson(created), 201);
            } else {
                manager.updateEpic(epic);
                sendText(exchange, GSON.toJson(epic));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, String idString) throws IOException {
        int id = parseId(idString);
        if (id == -1) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteEpic(id);
        sendText(exchange, "{\"message\":\"Эпик удален\"}");
    }
}