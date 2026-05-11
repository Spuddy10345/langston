package com.langton.simulation;

import com.langton.model.Ant;
import com.langton.model.Grid;
import java.util.List;

public interface SimulationEngine {
    void simulateSteps(int numSteps);
    Grid getGrid();
    List<Ant> getAnts();
}
