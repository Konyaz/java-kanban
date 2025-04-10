package ru.practicum.tracker;

import ru.practicum.tracker.model.*;
import ru.practicum.tracker.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // Создание задач
        Task task1 = taskManager.createTask(new Task("Купить продукты", "Описание задачи 1"));
        Task task2 = taskManager.createTask(new Task("Позвонить маме", "Описание задачи 2"));

        // Создание эпика с двумя подзадачами
        Epic epic1 = taskManager.createEpic(new Epic("Переезд", "Описание эпика 1"));
        Subtask sub1 = taskManager.createSubtask(new Subtask("Упаковать вещи", "Описание подзадачи 1", epic1.getId()));
        Subtask sub2 = taskManager.createSubtask(new Subtask("Найти грузчиков", "Описание подзадачи 2", epic1.getId()));

        // Создание эпика с одной подзадачей
        Epic epic2 = taskManager.createEpic(new Epic("Купить авто", "Описание эпика 2"));
        Subtask sub3 = taskManager.createSubtask(new Subtask("Посетить автосалоны", "Описание подзадачи 3", epic2.getId()));

        // Вывод списка всех задач
        System.out.println("Все задачи:");
        for (Task t : taskManager.getAllTasks()) {
            System.out.println(t.getName() + ": " + t.getStatus());
        }

        System.out.println("\nВсе эпики:");
        for (Epic e : taskManager.getAllEpics()) {
            System.out.println(e.getName() + ": " + e.getStatus());
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask s : taskManager.getAllSubtasks()) {
            System.out.println(s.getName() + ": " + s.getStatus());
        }

        // Обновление статусов
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);
        taskManager.updateSubtask(sub1);
        taskManager.updateSubtask(sub2);
        taskManager.updateEpic(epic1);

        System.out.println("\nПосле обновления статусов:");
        for (Task t : taskManager.getAllTasks()) {
            System.out.println(t.getName() + ": " + t.getStatus());
        }

        System.out.println("\nПосле обновления статусов эпиков:");
        for (Epic e : taskManager.getAllEpics()) {
            System.out.println(e.getName() + ": " + e.getStatus());
        }

        // Демонстрация удаления
        taskManager.deleteTask(task1.getId());
        taskManager.deleteEpic(epic1.getId());

        System.out.println("\nПосле удаления задачи и эпика:");
        System.out.println("Задачи: " + taskManager.getAllTasks());
        System.out.println("Эпики: " + taskManager.getAllEpics());
        System.out.println("Подзадачи: " + taskManager.getAllSubtasks());
    }
}