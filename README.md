# Langton's Ant Parallel Simulation

A high-performance, parallelized implementation of Langton's Ant cellular automaton in Java.

## Features

- **Parallelized Simulation:** Utilizes multi-threading to efficiently simulate thousands of ants on a single grid.
- **Sparse Data Structure:** Implements a memory-efficient sparse grid, allowing for massive simulation environments (e.g., 10,000x10,000 and beyond) without high RAM overhead.
- **Thread-Safe Region Locking:** Uses spatial partitioning and region-based `ReentrantLocks` to ensure safe concurrent updates to shared grid data.
- **Graphical User Interface (Swing):** 
  - Real-time visualization of ant movement and grid states.
  - Interactive controls: Start/Pause, Add Ants, Speed Control.
  - Configurable grid dimensions and initial ant counts.
- **Performance Benchmarking:** Includes a dedicated benchmark suite to measure speedup and efficiency across different ant counts and thread configurations.

## Requirements Met

1.  **Simulation Logic:** Fully implements Langton's Ant rules (white -> turn CW/flip black, black -> turn CCW/flip white).
2.  **Configuration:** Users can configure grid size, simulation steps, and the number of ants through the UI.
3.  **Parallelization:** Uses `ExecutorService` and a fixed thread pool to divide processing.
4.  **Concurrently Safe:** Handles boundary crossings and shared data updates using synchronized region locking and a `Phaser` for step-level coordination.
5.  **Efficiency:** Uses sparse `HashMap` storage for grid cells to avoid large array allocations.
6.  **Visualization:** Provides a Swing-based UI with play/pause and speed adjustment.
7.  **Performance Metrics:** Benchmark tool reports ST vs. Parallel execution times, speedup, and efficiency.

## Architecture

### Grid Management
The grid is divided into `N x M` regions. Each region contains a sparse `HashMap` of its active (black) cells. This approach provides:
- **Low Memory Footprint:** Only modified cells consume memory.
- **High Concurrency:** Ants in different regions can move simultaneously without locking contention.

### Parallel Engine
Ants are distributed among worker threads. Before each step:
1. Each thread determines which grid regions its ants will interact with.
2. It acquires the necessary region locks (in a consistent order to prevent deadlocks).
3. It performs the ant's step logic.
4. It releases the locks.
5. A `Phaser` ensures all ants complete the current step before any thread proceeds to the next.

## Getting Started

### Compilation
Compile all source files into the `bin` directory:
```bash
javac -d bin src/main/java/com/langton/model/*.java src/main/java/com/langton/simulation/*.java src/main/java/com/langton/ui/*.java src/main/java/com/langton/Benchmark.java
```

### Running the UI
Launch the graphical simulation:
```bash
java -cp bin com.langton.ui.SimulationUI
```

### Running the Benchmark
Execute the performance test suite:
```bash
java -cp bin com.langton.Benchmark
```

## Performance Results (Example)
On a typical 11-thread system with 10,000 ants:
- **Single-Threaded:** ~27,000 ms
- **Parallel:** ~25,000 ms
- **Speedup:** ~1.08x (Scales with workload density and hardware capacity)
