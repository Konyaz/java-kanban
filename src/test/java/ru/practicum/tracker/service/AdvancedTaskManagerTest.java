package ru.practicum.tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.tracker.history.HistoryManager;
import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.model.TaskStatus;
import ru.practicum.tracker.util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    // Проверка равенства задач по id (должны быть равны, если id совпадает)
    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Another Description");
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    // Проверка равенства подзадач по id (должны быть равны, если id совпадает)
    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 1);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Another Description", 1);
        subtask2.setId(2);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    // Проверка, что эпик нельзя добавить как подзадачу к самому себе
    @Test
    void testEpicCannotBeSubtaskOfItself() {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        Subtask invalidSubtask = manager.createSubtask(new Subtask("Invalid", "Desc", epic.getId()));
        assertNull(invalidSubtask, "Эпик не может быть подзадачей самого себя");
    }

    // Проверка, что подзадача не может ссылаться на несуществующий эпик
    @Test
    void testSubtaskCannotReferenceNonExistentEpic() {
        Subtask subtask = manager.createSubtask(new Subtask("Invalid", "Desc", 999));
        assertNull(subtask, "Подзадача с несуществующим эпиком должна быть отклонена");
    }

    // Проверка, что утилитарный класс возвращает готовые экземпляры менеджеров
    @Test
    void testUtilityManagersReturnInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Менеджер задач должен быть проинициализирован");
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории должен быть проинициализирован");
    }

    // Проверка добавления задач и поиска по id
    @Test
    void testAddAndFindTasksById() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));
        assertNotNull(manager.getTask(task.getId()), "Задача не найдена по id");
        assertNotNull(manager.getEpic(epic.getId()), "Эпик не найден по id");
        assertNotNull(manager.getSubtask(subtask.getId()), "Подзадача не найдена по id");
    }

    // Проверка уникальности id для задач
    @Test
    void testUniqueIdsForTasks() {
        Task task1 = manager.createTask(new Task("Task 1", "Desc"));
        Task task2 = manager.createTask(new Task("Task 2", "Desc"));
        assertNotEquals(task1.getId(), task2.getId(), "ID задач должны быть уникальными");
    }

    // Проверка, что поля задачи не изменяются после добавления в менеджер
    @Test
    void testTaskImmutabilityAfterAddition() {
        Task original = new Task("Original Task", "Original Desc");
        Task added = manager.createTask(original);
        assertEquals(original.getName(), added.getName(), "Имя задачи изменилось");
        assertEquals(original.getDescription(), added.getDescription(), "Описание задачи изменилось");
        assertEquals(original.getStatus(), added.getStatus(), "Статус задачи изменился");
    }

    // Проверка, что история хранит снимок задачи (не изменяется при обновлении)
    @Test
    void testHistoryManagerStoresSnapshot() {
        Task task = manager.createTask(new Task("Task", "Original Desc"));
        manager.getTask(task.getId());
        task.setDescription("Modified Desc");
        manager.updateTask(task);
        List<Task> history = manager.getHistory();
        Task historicalTask = history.get(0);
        assertEquals("Original Desc", historicalTask.getDescription(),
                "История должна хранить первоначальную версию задачи");
    }

    // Проверка обновления статуса эпика при изменении подзадач
    @Test
    void testEpicStatusUpdatesOnSubtaskChange() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика должен быть NEW");
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).getStatus(),
                "Статус эпика должен быть DONE");
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId()));
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(),
                "Статус эпика должен быть IN_PROGRESS");
    }

    // Проверка удаления всех задач
    @Test
    void testDeleteAllTasks() {
        manager.createTask(new Task("Task 1", "Desc"));
        manager.createTask(new Task("Task 2", "Desc"));
        manager.deleteTasks();
        assertTrue(manager.getAllTasks().isEmpty(), "Список задач должен быть пуст");
    }

    // Проверка ограничения истории 10 элементами
    @Test
    void testHistoryLimit() {
        for (int i = 0; i < 12; i++) {
            Task task = manager.createTask(new Task("Task " + i, "Desc"));
            manager.getTask(task.getId());
        }
        assertEquals(10, manager.getHistory().size(), "История должна содержать не более 10 элементов");
    }

    // Проверка удаления подзадач при удалении эпика
    @Test
    void testEpicDeletionRemovesSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));
        manager.deleteEpic(epic.getId());
        assertNull(manager.getSubtask(subtask.getId()), "Подзадачи эпика должны быть удалены");
    }

    // Проверка статуса эпика без подзадач (должен быть NEW)
    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    // Проверка удаления задачи из истории при её удалении
    @Test
    void testHistoryClearedOnTaskDeletion() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());
        manager.deleteTask(task.getId());
        assertFalse(manager.getHistory().contains(task), "Задача должна быть удалена из истории");
    }
}
