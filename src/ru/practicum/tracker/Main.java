package ru.practicum.tracker;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // 2 обычные задачи
        Task task1 = manager.createTask("Купить продукты", "Сходить в магазин");
        Task task2 = manager.createTask("Позвонить маме", "Уточнить про выходные");

        // Эпик 1 с 2 подзадачами
        Epic epic1 = manager.createEpic("Переезд", "Собраться и переехать");
        Subtask sub1 = manager.createSubtask("Собрать вещи", "Упаковать всё", epic1.getId());
        Subtask sub2 = manager.createSubtask("Найти грузчиков", "Позвонить и договориться", epic1.getId());

        // Эпик 2 с 1 подзадачей
        Epic epic2 = manager.createEpic("Купить авто", "Выбрать и купить машину");
        Subtask sub3 = manager.createSubtask("Посетить автосалоны", "Выбрать модель", epic2.getId());

        // Печать списков
        System.out.println("\nЗадачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nПодзадачи:");
        for (Subtask sub : manager.getAllSubtasks()) {
            System.out.println(sub);
        }

        // Обновление статуса
        sub1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        sub2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(sub2);

        System.out.println("\nПосле изменения статусов:");
        System.out.println(manager.getAllEpics());

        // Удаление задачи и эпика
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\nПосле удаления:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
    }
}
