package ru.practicum.tracker;

import ru.practicum.tracker.model.*;
import ru.practicum.tracker.service.TaskManager;

public class Main {
    public static void main(String[] args) {
       TaskManager taskManager = new TaskManager();

        // Создание задач
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1", 1));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2", 2));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1", 3));
        Epic epic2 = taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2", 4));

        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", 5, 3));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", 6, 3));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", 7, 4));

        // Вывод списков
        System.out.println("Все задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task.getName() + ": " + task.getStatus());
        }

        System.out.println("\nВсе эпики:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic.getName() + ": " + epic.getStatus());
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask.getName() + ": " + subtask.getStatus());
        }

        // Обновление статусов
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.NEW);
        epic1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        taskManager.updateEpic(epic1);
        taskManager.getTask(1);

        // Вывод после обновлений
        System.out.println("\nПосле обновления статусов:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task.getName() + ": " + task.getStatus());
        }

        System.out.println("\nПосле обновления статусов эпиков:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic.getName() + ": " + epic.getStatus());
        }

        // Удаление задачи и эпика
        taskManager.deleteTask(1);
        taskManager.deleteEpic(3);

        System.out.println("\nПосле удаления задачи и эпика:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task.getName() + ": " + task.getStatus());
        }

        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic.getName() + ": " + epic.getStatus());
        }
    }
}