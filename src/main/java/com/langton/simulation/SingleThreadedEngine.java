package com.langton.simulation;

import com.langton.model.Ant;
import com.langton.model.Grid;
import java.util.List;

public class SingleThreadedEngine implements SimulationEngine {
    private final Grid grid;
    private final List<Ant> ants;

    public SingleThreadedEngine(Grid grid, List<Ant> ants) {
        this.grid = grid;
        this.ants = ants;
    }

    @Override
    public void simulateSteps(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            for (Ant ant : ants) {
                ant.step(grid);
            }
        }
    }

    @Override
    public Grid getGrid() { return grid; }

    @Override
    public List<Ant> getAnts() { return ants; }
}
