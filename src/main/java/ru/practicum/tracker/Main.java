package ru.practicum.tracker;

import ru.practicum.tracker.service.TaskManager;
import ru.practicum.tracker.model.Epic;
import ru.practicum.tracker.model.Subtask;
import ru.practicum.tracker.model.Task;
import ru.practicum.tracker.util.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создание задач
        Task task1 = manager.createTask(new Task("Купить продукты", "Описание задачи 1"));
        Task task2 = manager.createTask(new Task("Позвонить маме", "Описание задачи 2"));

        // Создание эпика и подзадач
        Epic epic1 = manager.createEpic(new Epic("Переезд", "Описание эпика 1"));
        Subtask sub1 = manager.createSubtask(new Subtask("Упаковать вещи", "Описание подзадачи 1", epic1.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Найти грузчиков", "Описание подзадачи 2", epic1.getId()));

        // Формирование истории просмотров
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(sub1.getId());
        manager.getTask(task2.getId());
        manager.getSubtask(sub2.getId());

        // Вывод истории
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}