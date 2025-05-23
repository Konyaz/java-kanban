package ru.practicum.tracker;

import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.model.TaskStatus;
import ru.practicum.tracker.service.TaskManager;
import ru.practicum.tracker.util.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создание задач
        Task task1 = manager.createTask(new Task("Купить продукты", "Молоко, хлеб, яйца"));
        Task task2 = manager.createTask(new Task("Позвонить маме", "Обсудить планы на выходные"));

        // Создание эпика и подзадач
        Epic epic = manager.createEpic(new Epic("Переезд", "Организация переезда в новый офис"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Упаковать вещи", "Коробки, скотч, маркеры", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Нанять грузчиков", "Найти через агрегатор", epic.getId()));

        // Формирование истории просмотров
        manager.getTask(task1.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task2.getId());
        manager.getSubtask(subtask2.getId());

        // Вывод истории
        System.out.println("=== История просмотров ===");
        manager.getHistory().forEach(System.out::println);

        // Проверка статуса эпика
        System.out.println("\n=== Статус эпика ===");
        System.out.println(manager.getEpic(epic.getId()).getStatus()); // NEW

        // Изменение статуса подзадачи и проверка эпика
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        System.out.println("\n=== Статус эпика после изменения подзадачи ===");
        System.out.println(manager.getEpic(epic.getId()).getStatus()); // IN_PROGRESS
    }
}