package edu.geekhub.homework.task3.utils;

import java.util.Arrays;

public class TrivialStackImp implements TrivialStringStack {
    private String[] data = new String[100];
    private int pointer = -1;
    private final float EXTENSION_PERCENTAGE = 1.5f;
    private final IllegalArgumentException STACK_IS_EMPTY = new IllegalArgumentException("Stack is empty");

    @Override
    public void push(String input) {
        pointer += 1;
        if(pointer == data.length) {
            data = Arrays.copyOf(data, (int) (data.length * EXTENSION_PERCENTAGE + 1));
        }
        data[pointer] = input;
    }

    @Override
    public String pop() {
        if(pointer == -1) {
            throw STACK_IS_EMPTY;
        }

        String value = data[pointer];
        data[pointer] = null;
        pointer -= 1;

        return value;
    }

    @Override
    public String peak() {
        if(pointer == -1) {
            throw new IllegalArgumentException("Stack is empty");
        }

        String value = data[pointer];

        return value;
    }

    @Override
    public int size() {
        if(pointer == -1) {
            throw STACK_IS_EMPTY;
        }

        return pointer + 1;
    }
}