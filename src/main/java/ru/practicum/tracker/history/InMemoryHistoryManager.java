package ru.practicum.tracker.history;

import ru.practicum.tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;
    private int size = 0;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Удаляем существующую запись, если есть
        remove(task.getId());

        // Создаем копию задачи для истории
        Task taskCopy = task.copy();

        // Добавляем задачу в конец списка
        linkLast(taskCopy);

        // Сохраняем в мапе
        nodeMap.put(task.getId(), tail);
        size++;
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.get(id);
        if (node != null) {
            removeNode(node);
            nodeMap.remove(id);
            size--;
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>(size);
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(task, tail, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        final Node next = node.next;
        final Node prev = node.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }

        // Очищаем ссылки для GC
        node.prev = null;
        node.next = null;
        node.task = null;
    }
}