package com.langton.simulation;

import com.langton.model.Ant;
import com.langton.model.Direction;
import com.langton.model.Grid;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelEngine implements SimulationEngine {
    private final Grid grid;
    private final List<Ant> ants;
    private final ExecutorService executor;
    private final int numThreads;

    public ParallelEngine(Grid grid, List<Ant> ants, int numThreads) {
        this.grid = grid;
        this.ants = ants;
        this.numThreads = Math.min(numThreads, ants.size());
        this.executor = Executors.newFixedThreadPool(Math.max(1, this.numThreads));
    }

    @Override
    public void simulateSteps(int numSteps) {
        if (ants.isEmpty()) return;

        int actualThreads = 0;
        int antsPerThread = (int) Math.ceil((double) ants.size() / numThreads);
        for (int t = 0; t < numThreads; t++) {
            if (t * antsPerThread < ants.size()) actualThreads++;
        }

        Phaser phaser = new Phaser(actualThreads);
        CountDownLatch completionLatch = new CountDownLatch(actualThreads);

        for (int t = 0; t < numThreads; t++) {
            final int startIdx = t * antsPerThread;
            final int endIdx = Math.min(startIdx + antsPerThread, ants.size());
            
            if (startIdx >= ants.size()) break;

            executor.submit(() -> {
                try {
                    for (int s = 0; s < numSteps; s++) {
                        for (int i = startIdx; i < endIdx; i++) {
                            performSafeStep(ants.get(i));
                        }
                        phaser.arriveAndAwaitAdvance();
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void performSafeStep(Ant ant) {
        int x = ant.getX();
        int y = ant.getY();
        Direction dir = ant.getDirection();
        int nextX = x + dir.dx;
        int nextY = y + dir.dy;

        int r1 = grid.getRegionId(x, y);
        int r2 = grid.isValid(nextX, nextY) ? grid.getRegionId(nextX, nextY) : r1;

        if (r1 == r2) {
            ReentrantLock lock = grid.getLockForRegion(r1);
            lock.lock();
            try {
                ant.step(grid);
            } finally {
                lock.unlock();
            }
        } else {
            int first = Math.min(r1, r2);
            int second = Math.max(r1, r2);
            
            ReentrantLock lock1 = grid.getLockForRegion(first);
            ReentrantLock lock2 = grid.getLockForRegion(second);
            
            lock1.lock();
            try {
                lock2.lock();
                try {
                    ant.step(grid);
                } finally {
                    lock2.unlock();
                }
            } finally {
                lock1.unlock();
            }
        }
    }

    @Override
    public Grid getGrid() { return grid; }

    @Override
    public List<Ant> getAnts() { return ants; }
    
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
