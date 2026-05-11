# Langton's Ant Parallel Simulation

This project implements a parallelized version of Langton's Ant simulation in Java, utilizing multithreading and region-based synchronization.

## Architecture

The simulation follows the **Semaphore-Based Design** with **Lexicographical Resource Ordering** as described in the architectural analysis report.

### Key Components

- **Grid**: A 10,000x10,000 grid represented as a flattened `byte[]` for memory efficiency. The grid is divided into regions (sectors).
- **Ant**: Each ant tracks its position and direction. It interacts with the grid by flipping cell colors and moving according to Langton's rules.
- **ParallelEngine**: Uses an `ExecutorService` and a `Phaser` to synchronize ant movements. Each ant's step is protected by region-based locks. If an ant crosses a boundary, it acquires locks for both regions in a consistent lexicographical order to prevent deadlocks.
- **SimulationUI**: A Swing-based graphical interface for real-time visualization.
- **Benchmark**: A tool to compare the performance of single-threaded vs. parallel execution.

## Requirements

- Java 8 or higher (Tested with OpenJDK 25)
- No external dependencies (uses standard Java libraries)

## Running the Project

### Compilation

```bash
mkdir -p bin
javac -d bin src/main/java/com/langton/model/*.java src/main/java/com/langton/simulation/*.java src/main/java/com/langton/ui/*.java src/main/java/com/langton/*.java
```

### Benchmarking

```bash
java -cp bin com.langton.Benchmark
```

### Visualization

```bash
java -cp bin com.langton.ui.SimulationUI
```

## Performance Findings

The benchmark results show that the parallel implementation has synchronization overhead due to the fine-grained nature of Langton's Ant (one lock/unlock and one barrier per ant per step). Speedup increases with the number of ants, but for small populations, the single-threaded version is faster.

| Ants | Single-Threaded | Parallel (11 threads) | Speedup |
|------|-----------------|-----------------------|---------|
| 100  | 19 ms           | 315 ms                | 0.06x   |
| 1000 | 116 ms          | 680 ms                | 0.17x   |
| 5000 | 546 ms          | 2125 ms               | 0.26x   |

*Results from 10,000 steps on 1000x1000 grid.*

## Implementation Details

- **Thread Safety**: Boundary crossings are handled by locking both the current and destination regions. Lexicographical ordering (locking the region with the smaller ID first) ensures no circular wait occurs, effectively preventing deadlocks.
- **Scalability**: The grid can be scaled to 10,000x10,000 (100 million cells) within ~100MB of RAM.
