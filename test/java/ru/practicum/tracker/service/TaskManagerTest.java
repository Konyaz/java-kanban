package ru.practicum.tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.model.*;
import ru.practicum.tracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    protected HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        manager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Another Description");
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 1);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Another Description", 1);
        subtask2.setId(2);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void testSubtaskCannotReferenceItselfAsEpic() {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        Subtask subtask = new Subtask("Invalid Subtask", "Desc", epic.getId());
        subtask.setId(epic.getId()); // Симулируем некорректный случай
        assertNull(manager.createSubtask(subtask), "Подзадача не может ссылаться на себя как на эпик");
    }

    @Test
    void testSubtaskCannotReferenceNonExistentEpic() {
        Subtask subtask = new Subtask("Invalid", "Desc", 999);
        assertNull(manager.createSubtask(subtask), "Подзадача с несуществующим эпиком должна быть отклонена");
    }

    @Test
    void testAddAndFindTasksById() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));
        assertNotNull(manager.getTask(task.getId()), "Задача не найдена по id");
        assertNotNull(manager.getEpic(epic.getId()), "Эпик не найден по id");
        assertNotNull(manager.getSubtask(subtask.getId()), "Подзадача не найдена по id");
    }

    @Test
    void testUniqueIdsForTasks() {
        Task task1 = manager.createTask(new Task("Task 1", "Desc"));
        Task task2 = manager.createTask(new Task("Task 2", "Desc"));
        assertNotEquals(task1.getId(), task2.getId(), "ID задач должны быть уникальными");
    }

    @Test
    void testTaskImmutabilityAfterAddition() {
        Task original = new Task("Original Task", "Original Desc");
        Task added = manager.createTask(original);
        assertEquals(original.getName(), added.getName(), "Имя задачи изменилось");
        assertEquals(original.getDescription(), added.getDescription(), "Описание задачи изменилось");
        assertEquals(original.getStatus(), added.getStatus(), "Статус задачи изменился");
    }

    @Test
    void testHistoryManagerStoresSnapshot() {
        Task task = manager.createTask(new Task("Task", "Original Desc"));
        manager.getTask(task.getId());
        task.setDescription("Modified Desc");
        manager.updateTask(task);
        List<Task> history = manager.getHistory();
        assertEquals("Original Desc", history.get(0).getDescription(),
                "История должна хранить первоначальную версию задачи");
    }

    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void testEpicStatusAllNewSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Subtask 2", "Desc", TaskStatus.NEW, epic.getId()));
        assertEquals(TaskStatus.NEW, manager.getEpic(epic.getId()).getStatus(),
                "Статус эпика с подзадачами NEW должен быть NEW");
    }

    @Test
    void testEpicStatusAllDoneSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Subtask 1", "Desc", TaskStatus.DONE, epic.getId()));
        manager.createSubtask(new Subtask("Subtask 2", "Desc", TaskStatus.DONE, epic.getId()));
        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).getStatus(),
                "Статус эпика с подзадачами DONE должен быть DONE");
    }

    @Test
    void testEpicStatusMixedNewAndDoneSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Subtask 2", "Desc", TaskStatus.DONE, epic.getId()));
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(),
                "Статус эпика с подзадачами NEW и DONE должен быть IN_PROGRESS");
    }

    @Test
    void testEpicStatusInProgressSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Subtask 1", "Desc", TaskStatus.IN_PROGRESS, epic.getId()));
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(),
                "Статус эпика с подзадачами IN_PROGRESS должен быть IN_PROGRESS");
    }

    @Test
    void testEpicTimeCalculations() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        LocalDateTime start1 = LocalDateTime.of(2025, 6, 8, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2025, 6, 8, 11, 0);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        subtask1.setStartTime(start1);
        subtask1.setDuration(Duration.ofMinutes(30));
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        subtask2.setStartTime(start2);
        subtask2.setDuration(Duration.ofMinutes(45));
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(start1, epic.getStartTime(), "Время начала эпика должно быть равно времени начала самой ранней подзадачи");
        assertEquals(Duration.ofMinutes(75), epic.getDuration(), "Продолжительность эпика должна быть суммой продолжительностей подзадач");
        assertEquals(start2.plusMinutes(45), epic.getEndTime(), "Время окончания эпика должно быть равно времени окончания самой поздней подзадачи");
    }

    @Test
    void testPrioritizedTasksOrder() {
        Task task1 = new Task("Task 1", "Desc");
        task1.setStartTime(LocalDateTime.of(2025, 6, 8, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));
        Task task2 = new Task("Task 2", "Desc");
        task2.setStartTime(LocalDateTime.of(2025, 6, 8, 9, 0));
        task2.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size(), "Список приоритетных задач должен содержать обе задачи");
        assertEquals(task2.getId(), prioritized.get(0).getId(), "Задача с более ранним временем должна быть первой");
        assertEquals(task1.getId(), prioritized.get(1).getId(), "Задача с более поздним временем должна быть второй");
    }

    @Test
    void testTasksWithNullStartTimeExcludedFromPrioritized() {
        Task task1 = new Task("Task 1", "Desc");
        task1.setStartTime(LocalDateTime.of(2025, 6, 8, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));
        Task task2 = new Task("Task 2", "Desc"); // Без startTime
        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(1, prioritized.size(), "В списке приоритетных задач должна быть только одна задача");
        assertEquals(task1.getId(), prioritized.get(0).getId(), "В списке должна быть только задача с заданным startTime");
    }

    @Test
    void testTimeConflictDetection() {
        Task task1 = new Task("Task 1", "Desc");
        task1.setStartTime(LocalDateTime.of(2025, 6, 8, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc");
        task2.setStartTime(LocalDateTime.of(2025, 6, 8, 10, 30)); // Пересекается с task1
        task2.setDuration(Duration.ofMinutes(30));
        assertThrows(ManagerConflictException.class, () -> manager.createTask(task2),
                "Задача с пересекающимся временем должна вызывать исключение");

        Task task3 = new Task("Task 3", "Desc");
        task3.setStartTime(LocalDateTime.of(2025, 6, 8, 11, 0)); // Начинается после task1
        task3.setDuration(Duration.ofMinutes(30));
        assertNotNull(manager.createTask(task3), "Задача без пересечения времени должна быть создана");
    }

    @Test
    void testHistoryEmpty() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой при инициализации");
    }

    @Test
    void testHistoryNoDuplicates() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());
        manager.getTask(task.getId());
        assertEquals(1, manager.getHistory().size(), "История не должна содержать дубликаты");
    }

    @Test
    void testHistoryDeletionFromStart() {
        Task task1 = manager.createTask(new Task("Task 1", "Desc"));
        Task task2 = manager.createTask(new Task("Task 2", "Desc"));
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.deleteTask(task1.getId());
        assertEquals(1, manager.getHistory().size(), "В истории должна остаться одна задача после удаления");
        assertEquals(task2.getId(), manager.getHistory().get(0).getId(), "Оставшаяся задача должна быть task2");
    }

    @Test
    void testHistoryDeletionFromMiddle() {
        Task task1 = manager.createTask(new Task("Task 1", "Desc"));
        Task task2 = manager.createTask(new Task("Task 2", "Desc"));
        Task task3 = manager.createTask(new Task("Task 3", "Desc"));
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());
        manager.deleteTask(task2.getId());
        assertEquals(2, manager.getHistory().size(), "В истории должны остаться две задачи после удаления");
        assertEquals(task1.getId(), manager.getHistory().get(0).getId(), "Первая задача должна быть task1");
        assertEquals(task3.getId(), manager.getHistory().get(1).getId(), "Вторая задача должна быть task3");
    }

    @Test
    void testHistoryDeletionFromEnd() {
        Task task1 = manager.createTask(new Task("Task 1", "Desc"));
        Task task2 = manager.createTask(new Task("Task 2", "Desc"));
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.deleteTask(task2.getId());
        assertEquals(1, manager.getHistory().size(), "В истории должна остаться одна задача после удаления");
        assertEquals(task1.getId(), manager.getHistory().get(0).getId(), "Оставшаяся задача должна быть task1");
    }
}