package edu.geekhub.homework.task3.utils;

public interface TrivialStringStack {
    void push(String input);
    void push(String[] input);
    String pop();
    String peak();
    boolean isEmpty();
    boolean hasNext();
    int size();
}
