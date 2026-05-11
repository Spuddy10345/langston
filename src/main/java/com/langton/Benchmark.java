package com.langton;

import com.langton.model.Ant;
import com.langton.model.Direction;
import com.langton.model.Grid;
import com.langton.simulation.ParallelEngine;
import com.langton.simulation.SingleThreadedEngine;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {
    public static void main(String[] args) {
        int width = 2000;
        int height = 2000;
        int steps = 20000;
        int[] antCounts = {1000, 10000, 30000};
        int numThreads = Runtime.getRuntime().availableProcessors();

        System.out.println("Benchmarking Langton's Ant Simulation");
        System.out.println("Grid Size: " + width + "x" + height);
        System.out.println("Steps: " + steps);
        System.out.println("Threads: " + numThreads);
        System.out.println("--------------------------------------------------");

        for (int count : antCounts) {
            runBenchmark(width, height, steps, count, numThreads);
        }
    }

    private static void runBenchmark(int width, int height, int steps, int numAnts, int numThreads) {
        List<Ant> ants1 = createAnts(numAnts, width, height);
        List<Ant> ants2 = createAnts(numAnts, width, height);
        Grid grid1 = new Grid(width, height, 50);
        Grid grid2 = new Grid(width, height, 50);

        SingleThreadedEngine stEngine = new SingleThreadedEngine(grid1, ants1);
        ParallelEngine pEngine = new ParallelEngine(grid2, ants2, numThreads);

        long startST = System.currentTimeMillis();
        stEngine.simulateSteps(steps);
        long endST = System.currentTimeMillis();
        long durationST = endST - startST;

        long startP = System.currentTimeMillis();
        pEngine.simulateSteps(steps);
        long endP = System.currentTimeMillis();
        long durationP = endP - startP;
        
        pEngine.shutdown();

        double speedup = (double) durationST / durationP;
        double efficiency = speedup / numThreads;

        System.out.printf("Ants: %d | ST: %d ms | Parallel: %d ms | Speedup: %.2fx | Efficiency: %.2f%n",
                numAnts, durationST, durationP, speedup, efficiency);
    }

    private static List<Ant> createAnts(int count, int width, int height) {
        List<Ant> ants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ants.add(new Ant(i, width / 2, height / 2, Direction.NORTH));
        }
        return ants;
    }
}
