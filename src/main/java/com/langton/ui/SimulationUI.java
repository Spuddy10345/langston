package com.langton.ui;

import com.langton.model.Ant;
import com.langton.model.Grid;
import com.langton.simulation.ParallelEngine;
import com.langton.simulation.SimulationEngine;
import com.langton.model.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulationUI extends JFrame {
    private int gridWidth = 1000;
    private int gridHeight = 1000;
    private Grid grid;
    private List<Ant> ants;
    private SimulationEngine engine;
    private boolean running = false;
    private int stepsPerFrame = 100;
    private long totalSteps = 0;

    private final JPanel canvas;
    private final JLabel statsLabel;
    private final JTextField widthField;
    private final JTextField heightField;
    private final JTextField antsField;
    private final JSlider speedSlider;

    public SimulationUI() {
        setTitle("Langton's Ant Parallel Simulation");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Control Panel for Configuration
        JPanel configPanel = new JPanel();
        configPanel.add(new JLabel("Width:"));
        widthField = new JTextField("1000", 5);
        configPanel.add(widthField);
        
        configPanel.add(new JLabel("Height:"));
        heightField = new JTextField("1000", 5);
        configPanel.add(heightField);
        
        configPanel.add(new JLabel("Initial Ants:"));
        antsField = new JTextField("1", 3);
        configPanel.add(antsField);
        
        JButton applyBtn = new JButton("Apply & Reset");
        applyBtn.addActionListener(e -> resetSimulation());
        configPanel.add(applyBtn);
        
        add(configPanel, BorderLayout.NORTH);

        // Center Canvas
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        canvas.setBackground(Color.WHITE);
        add(new JScrollPane(canvas), BorderLayout.CENTER);

        // Bottom Control Panel
        JPanel controlPanel = new JPanel();
        JButton startBtn = new JButton("Start/Pause");
        startBtn.addActionListener(e -> running = !running);
        
        JButton addAntBtn = new JButton("Add Ant (Random)");
        addAntBtn.addActionListener(e -> {
            synchronized (ants) {
                ants.add(new Ant(ants.size(), (int)(Math.random() * grid.getWidth()), (int)(Math.random() * grid.getHeight()), Direction.NORTH));
            }
        });

        speedSlider = new JSlider(1, 1000, 100);
        speedSlider.addChangeListener(e -> stepsPerFrame = speedSlider.getValue());
        
        statsLabel = new JLabel("Ants: 0 | Steps: 0");
        
        controlPanel.add(startBtn);
        controlPanel.add(addAntBtn);
        controlPanel.add(new JLabel("Speed:"));
        controlPanel.add(speedSlider);
        controlPanel.add(statsLabel);
        add(controlPanel, BorderLayout.SOUTH);

        resetSimulation();

        Timer timer = new Timer(16, this::update);
        timer.start();
    }

    private void resetSimulation() {
        try {
            gridWidth = Integer.parseInt(widthField.getText());
            gridHeight = Integer.parseInt(heightField.getText());
            int initialAnts = Integer.parseInt(antsField.getText());
            
            grid = new Grid(gridWidth, gridHeight, 100);
            ants = new ArrayList<>();
            for (int i = 0; i < initialAnts; i++) {
                ants.add(new Ant(i, gridWidth / 2, gridHeight / 2, Direction.NORTH));
            }
            
            if (engine instanceof ParallelEngine) {
                ((ParallelEngine) engine).shutdown();
            }
            engine = new ParallelEngine(grid, ants, Runtime.getRuntime().availableProcessors());
            totalSteps = 0;
            running = false;
            canvas.repaint();
            updateStats();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers.");
        }
    }

    private void update(ActionEvent e) {
        if (running) {
            engine.simulateSteps(stepsPerFrame);
            totalSteps += stepsPerFrame;
            updateStats();
            canvas.repaint();
        }
    }

    private void updateStats() {
        statsLabel.setText(String.format("Ants: %d | Steps: %d | Active Cells: %d", 
            ants.size(), totalSteps, grid.getActiveCellCount()));
    }

    private void drawGrid(Graphics g) {
        if (grid == null) return;

        double cellW = (double) canvas.getWidth() / grid.getWidth();
        double cellH = (double) canvas.getHeight() / grid.getHeight();

        // Draw black cells from sparse maps (one per region)
        g.setColor(Color.BLACK);
        Map<Long, Byte>[] regionCells = grid.getRegionCells();
        for (Map<Long, Byte> cells : regionCells) {
            // Iterating over maps. Note: concurrent access might happen if running.
            // For visualization, we accept occasional minor glitches for performance.
            for (Map.Entry<Long, Byte> entry : cells.entrySet()) {
                if (entry.getValue() == 1) {
                    int x = Grid.getXFromKey(entry.getKey());
                    int y = Grid.getYFromKey(entry.getKey());
                    g.fillRect((int)(x * cellW), (int)(y * cellH), (int)Math.ceil(cellW), (int)Math.ceil(cellH));
                }
            }
        }

        // Draw ants
        g.setColor(Color.RED);
        synchronized (ants) {
            for (Ant ant : ants) {
                g.fillRect((int)(ant.getX() * cellW), (int)(ant.getY() * cellH), (int)Math.ceil(cellW), (int)Math.ceil(cellH));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationUI ui = new SimulationUI();
            ui.setVisible(true);
        });
    }
}
