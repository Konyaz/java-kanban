package ru.practicum.tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.tracker.model.*;
import ru.practicum.tracker.util.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = Files.createTempFile("tasks", ".csv").toFile();
            return Managers.getFileBackedManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @Test
    void testSaveAndLoadEmptyManager() {
        ((FileBackedTaskManager) manager).save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty(), "Список задач должен быть пуст");
        assertTrue(loaded.getAllEpics().isEmpty(), "Список эпиков должен быть пуст");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Список подзадач должен быть пуст");
    }

    @Test
    void testSaveAndLoadAllTaskTypes() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        task.setStartTime(LocalDateTime.of(2025, 6, 8, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));
        subtask.setStartTime(LocalDateTime.of(2025, 6, 8, 11, 0));
        subtask.setDuration(Duration.ofMinutes(45));

        ((FileBackedTaskManager) manager).save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllTasks().size(), "Должна быть одна задача");
        assertEquals(1, loaded.getAllEpics().size(), "Должен быть один эпик");
        assertEquals(1, loaded.getAllSubtasks().size(), "Должна быть одна подзадача");
        assertEquals(Duration.ofMinutes(45), loaded.getEpic(epic.getId()).getDuration(), "Продолжительность эпика должна совпадать");
        assertEquals(LocalDateTime.of(2025, 6, 8, 11, 0), loaded.getEpic(epic.getId()).getStartTime(),
                "Время начала эпика должно совпадать");
        assertEquals(LocalDateTime.of(2025, 6, 8, 11, 45), loaded.getEpic(epic.getId()).getEndTime(),
                "Время окончания эпика должно совпадать");
    }

    @Test
    void testSaveAndLoadHistory() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());

        ((FileBackedTaskManager) manager).save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getHistory().size(), "История должна содержать одну задачу");
        assertEquals(task.getId(), loaded.getHistory().get(0).getId(), "ID задачи в истории должен совпадать");
    }

    @Test
    void testFileSaveThrowsException() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(invalidFile);
        assertThrows(ManagerSaveException.class, invalidManager::save,
                "Сохранение в некорректный файл должно вызывать исключение");
    }

    @Test
    void testFileLoadThrowsException() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(invalidFile),
                "Загрузка из некорректного файла должна вызывать исключение");
    }

    @Test
    void testFileSaveDoesNotThrow() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        assertDoesNotThrow(((FileBackedTaskManager) manager)::save,
                "Сохранение в корректный файл не должно вызывать исключение");
    }
}