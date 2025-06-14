package ru.practicum.tracker.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.tracker.service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetPrioritized(exchange);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        sendText(exchange, GSON.toJson(manager.getPrioritizedTasks()));
    }
}