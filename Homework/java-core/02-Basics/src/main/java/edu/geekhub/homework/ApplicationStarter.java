package edu.geekhub.homework;

import static  java.lang.Math.PI;

public class ApplicationStarter {
    private static final double SECOND_POW = 2;
    private static final double ANGLE_OF_EQUILATERAL_TRIANGLE = 60;

    public static void main(String[] args) {
        int input = 6;
        double calculated = calculate(input);
        System.out.printf("For input value %d result is: %f", input, calculated);
    }

    private static double calculate(int n) {
        if (n % 2 == 0) {
            return calculateSquareAreaBySide(n);
        } else if (n % 3 == 0) {
            return calculateCircleAreaByRadius(n);
        } else {
            return calculateEquilateralTriangleAreaBySide(n);
        }
    }

    private static double calculateSquareAreaBySide(int side) {
        return Math.pow(side, SECOND_POW);
    }

    private static double calculateCircleAreaByRadius(int radius) {
        return PI * Math.pow(radius, SECOND_POW);
    }

    private static double calculateEquilateralTriangleAreaBySide(int side) {
        double h = side * Math.sin(Math.toRadians(ANGLE_OF_EQUILATERAL_TRIANGLE));
        return (side * h) / 2;
    }
}