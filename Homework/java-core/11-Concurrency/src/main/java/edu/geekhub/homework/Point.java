package edu.geekhub.homework;

import static edu.geekhub.homework.Util.checkIndex;
import static java.lang.Math.max;

public class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point getExistingNeighborCoordinates(int x, int y, int size) {
        while (true) {
            Direction direction = Direction.getRandomDirection();
            int neighborX = Direction.moveX(x, direction);
            int neighborY = Direction.moveY(y, direction);
            if (checkIndex(neighborX, size) && checkIndex(neighborY, size)) {
                return new Point(neighborX, neighborY);
            }
        }
    }

    public static Point getNeighborCoordinates(int x, int y) {
        Direction direction = Direction.getRandomDirection();
        int neighborX = Direction.moveX(x, direction);
        int neighborY = Direction.moveY(y, direction);
        return new Point(neighborX, neighborY);
    }

    public static Point getExistingNeighborCoordinates(int x, int y, int size, int step) {
        while (true) {
            step = max(step, 1);
            Direction direction = Direction.getRandomDirection();
            int neighborX = x;
            int neighborY = y;
            for (int i = 0; i < step; i++) {
                neighborX = Direction.moveX(neighborX, direction);
                neighborY = Direction.moveY(neighborY, direction);
            }
            if (checkIndex(neighborX, size) && checkIndex(neighborY, size)) {
                return new Point(neighborX, neighborY);
            }
        }
    }
}
