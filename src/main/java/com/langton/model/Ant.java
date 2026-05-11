package com.langton.model;

public class Ant {
    private int x;
    private int y;
    private Direction direction;
    private final int id;

    public Ant(int id, int x, int y, Direction direction) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getId() { return id; }
    public Direction getDirection() { return direction; }

    public void step(Grid grid) {
        // This method will be called by the simulation engine
        // which must handle the locking based on the grid's regions.
        CellColor currentColor = grid.getColor(x, y);
        if (currentColor == CellColor.WHITE) {
            direction = direction.turnClockwise();
        } else {
            direction = direction.turnCounterClockwise();
        }
        
        grid.flipColor(x, y);
        
        int nextX = x + direction.dx;
        int nextY = y + direction.dy;
        
        // Boundary check
        if (grid.isValid(nextX, nextY)) {
            x = nextX;
            y = nextY;
        } else {
            // Bounce or wrap? Brief doesn't specify. Let's just stop or stay.
            // For now, let's just stay at the same spot but maybe flip direction.
            direction = direction.turnClockwise().turnClockwise();
        }
    }
}
