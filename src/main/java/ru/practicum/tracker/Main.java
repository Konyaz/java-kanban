package ru.practicum.tracker;

import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.model.TaskStatus;
import ru.practicum.tracker.service.TaskManager;
import ru.practicum.tracker.service.FileBackedTaskManager;
import ru.practicum.tracker.util.Managers;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Инициализация менеджера с файловым хранилищем
        TaskManager manager = Managers.getDefault();
        File storageFile = ((FileBackedTaskManager) manager).getFile();

        System.out.println("=== Файл хранилища: " + storageFile.getAbsolutePath() + " ===\n");

        // Создание задач
        Task task1 = manager.createTask(new Task("Купить продукты", "Молоко, хлеб, яйца"));
        Task task2 = manager.createTask(new Task("Позвонить маме", "Обсудить планы на выходные"));

        // Создание эпика и подзадач
        Epic epic = manager.createEpic(new Epic("Переезд", "Организация переезда в новый офис"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Упаковать вещи", "Коробки, скотч, маркеры", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Нанять грузчиков", "Найти через агрегатор", epic.getId()));

        // Формирование истории просмотров
        System.out.println("=== Формируем историю просмотров ===");
        manager.getTask(task1.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task2.getId());
        manager.getSubtask(subtask2.getId());

        // Вывод истории
        System.out.println("\n=== История просмотров ===");
        manager.getHistory().forEach(System.out::println);

        // Проверка статуса эпика
        System.out.println("\n=== Статус эпика ===");
        System.out.println(manager.getEpic(epic.getId()).getStatus());

        // Изменение статуса подзадачи и проверка эпика
        System.out.println("\n=== Обновляем статус подзадачи ===");
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        System.out.println("Новый статус эпика: " + manager.getEpic(epic.getId()).getStatus());

        // Демонстрация работы с файлом
        System.out.println("\n=== Работа с файловым хранилищем ===");
        System.out.println("Размер файла: " + storageFile.length() + " байт");

        // Загрузка данных из файла в новый менеджер
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(storageFile);
        System.out.println("\n=== Загруженные данные из файла ===");
        System.out.println("Задачи: " + loadedManager.getAllTasks().size());
        System.out.println("Эпики: " + loadedManager.getAllEpics().size());
        System.out.println("Подзадачи: " + loadedManager.getAllSubtasks().size());
        System.out.println("История: " + loadedManager.getHistory().size());
    }
}