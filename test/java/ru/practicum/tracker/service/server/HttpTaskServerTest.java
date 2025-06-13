package ru.practicum.tracker.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.service.InMemoryTaskManager;
import ru.practicum.tracker.service.TaskManager;
import ru.practicum.tracker.util.GsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private HttpTaskServer httpTaskServer;
    private TaskManager taskManager;
    private HttpClient httpClient;
    private final Gson gson = GsonUtils.getGson(); // Используем утилитарный класс для создания Gson

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        httpTaskServer.stop();
    }

    // Тест создания задачи и последующего получения её по ID
    @Test
    void testCreateAndGetTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(30));

        String taskJson = gson.toJson(task);
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        Task createdTask = gson.fromJson(createResponse.body(), Task.class);
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + createdTask.getId()))
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        Task receivedTask = gson.fromJson(getResponse.body(), Task.class);
        assertEquals(createdTask.getId(), receivedTask.getId());
        assertEquals("Test Task", receivedTask.getName());
    }

    // Тест обновления существующей задачи
    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Original", "Desc");
        taskManager.createTask(task);

        task.setName("Updated");
        task.setDescription("New Desc");

        String taskJson = gson.toJson(task);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, updateResponse.statusCode());

        Task updatedTask = taskManager.getTask(task.getId());
        assertEquals("Updated", updatedTask.getName());
        assertEquals("New Desc", updatedTask.getDescription());
    }

    // Тест удаления задачи по ID
    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("To delete", "Desc");
        taskManager.createTask(task);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());

        assertNull(taskManager.getTask(task.getId()));
    }

    // Тест получения истории просмотров задач
    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Desc");
        Task task2 = new Task("Task2", "Desc");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = httpClient.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        Task[] history = gson.fromJson(historyResponse.body(), Task[].class);
        assertEquals(2, history.length);
        assertEquals(task1.getId(), history[0].getId());
        assertEquals(task2.getId(), history[1].getId());
    }

    // Тест получения отсортированного списка задач по приоритету
    @Test
    void testGetPrioritized() throws IOException, InterruptedException {
        Task earlyTask = new Task("Early", "Task");
        earlyTask.setStartTime(LocalDateTime.now().minusHours(1));
        earlyTask.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(earlyTask);

        Task lateTask = new Task("Late", "Task");
        lateTask.setStartTime(LocalDateTime.now().plusHours(1));
        lateTask.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(lateTask);

        HttpRequest prioritizedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> prioritizedResponse = httpClient.send(prioritizedRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResponse.statusCode());

        Task[] prioritized = gson.fromJson(prioritizedResponse.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals(earlyTask.getId(), prioritized[0].getId());
        assertEquals(lateTask.getId(), prioritized[1].getId());
    }
}

// Тесты для работы с задачами (Task)
class HttpTaskServerTasksTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/tasks";
    private final Gson gson = GsonUtils.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // Тест успешного создания задачи
    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test", "Description");
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertFalse(manager.getAllTasks().isEmpty());
    }

    // Тест создания задачи с конфликтом времени выполнения
    @Test
    void testCreateTaskWithTimeConflict() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(task1.getStartTime().plusMinutes(15));
        task2.setDuration(Duration.ofMinutes(30));
        String json = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    // Тест попытки получения несуществующей задачи
    @Test
    void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    // Тест удаления конкретной задачи
    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("To delete", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getTask(task.getId()));
    }

    // Тест получения всех задач
    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task1", "Desc"));
        manager.createTask(new Task("Task2", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
    }

    // Тест удаления всех задач
    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task1", "Desc"));
        manager.createTask(new Task("Task2", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }
}

// Тесты для работы с подзадачами (Subtask)
class HttpTaskServerSubtasksTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private Epic epic;
    private final String baseUrl = "http://localhost:8080/subtasks";
    private final Gson gson = GsonUtils.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
        epic = manager.createEpic(new Epic("Epic", "Desc"));
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // Тест создания подзадачи с несуществующим эпиком
    @Test
    void testCreateSubtaskWithInvalidEpic() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Invalid", "Desc", 999);
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    // Тест получения всех подзадач эпика
    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(subtask.getName()));
    }

    // Тест успешного создания подзадачи
    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Valid", "Desc", epic.getId());
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertFalse(manager.getAllSubtasks().isEmpty());
    }

    // Тест получения подзадачи по ID
    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask received = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), received.getId());
    }

    // Тест удаления всех подзадач
    @Test
    void testDeleteAllSubtasks() throws IOException, InterruptedException {
        manager.createSubtask(new Subtask("Subtask1", "Desc", epic.getId()));
        manager.createSubtask(new Subtask("Subtask2", "Desc", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}

// Тесты для работы с эпиками (Epic)
class HttpTaskServerEpicsTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/epics";
    private final Gson gson = GsonUtils.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // Тест каскадного удаления эпика с его подзадачами
    @Test
    void testDeleteEpicWithSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpicSubtasks(epic.getId()).isEmpty());
    }

    // Тест создания эпика
    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertFalse(manager.getAllEpics().isEmpty());
    }

    // Тест получения эпика по ID
    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic received = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), received.getId());
    }

    // Тест получения всех эпиков
    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic1", "Desc"));
        manager.createEpic(new Epic("Epic2", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
    }
}

// Тесты для работы с историей просмотров
class HttpTaskServerHistoryTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/history";
    private final Gson gson = GsonUtils.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // Тест получения истории просмотров задач
    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(task.getName()));
    }

    // Тест получения пустой истории
    @Test
    void testEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length);
    }
}

// Тесты для работы с приоритезированными задачами
class HttpTaskServerPrioritizedTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/prioritized";
    private final Gson gson = GsonUtils.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // Тест получения приоритезированных задач
    @Test
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Early", "Task");
        task1.setStartTime(LocalDateTime.now().minusHours(1));
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(task1.getName()));
    }

    // Тест получения пустого списка приоритезированных задач
    @Test
    void testEmptyPrioritized() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, tasks.length);
    }
}

// Интеграционные тесты для сквозного сценария работы
class HttpTaskServerIntegrationTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private final Gson gson = GsonUtils.getGson();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // Комплексный тест сквозного сценария:
    // 1. Создание эпика
    // 2. Создание подзадачи для эпика
    // 3. Проверка истории просмотров
    @Test
    void testFullFlow() throws IOException, InterruptedException {
        // Создание эпика
        Epic epic = new Epic("Epic", "Desc");
        String epicJson = gson.toJson(epic);
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());

        // Создание подзадачи
        Subtask subtask = new Subtask("Subtask", "Desc", 1);
        String subtaskJson = gson.toJson(subtask);
        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode());

        // Проверка истории
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());
        assertTrue(historyResponse.body().contains("Subtask"));
    }
}