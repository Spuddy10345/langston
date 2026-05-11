package com.langton.model;

public enum Direction {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction turnClockwise() {
        return values()[(ordinal() + 1) % 4];
    }

    public Direction turnCounterClockwise() {
        return values()[(ordinal() + 3) % 4];
    }
}
