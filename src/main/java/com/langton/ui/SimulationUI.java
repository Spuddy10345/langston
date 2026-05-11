package com.langton.ui;

import com.langton.model.Ant;
import com.langton.model.Grid;
import com.langton.simulation.ParallelEngine;
import com.langton.simulation.SimulationEngine;
import com.langton.simulation.SingleThreadedEngine;
import com.langton.model.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SimulationUI extends JFrame {
    private final int width = 500;
    private final int height = 500;
    private Grid grid;
    private List<Ant> ants;
    private SimulationEngine engine;
    private boolean running = false;
    private int stepsPerFrame = 100;

    private final JPanel canvas;
    private final JLabel statsLabel;

    public SimulationUI() {
        setTitle("Langton's Ant Parallel Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        grid = new Grid(width, height, 50);
        ants = new ArrayList<>();
        ants.add(new Ant(0, width / 2, height / 2, Direction.NORTH));
        engine = new ParallelEngine(grid, ants, 4);

        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        canvas.setPreferredSize(new Dimension(width, height));
        add(new JScrollPane(canvas), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton startBtn = new JButton("Start/Pause");
        startBtn.addActionListener(e -> running = !running);
        
        JButton addAntBtn = new JButton("Add Ant");
        addAntBtn.addActionListener(e -> {
            synchronized (ants) {
                ants.add(new Ant(ants.size(), (int)(Math.random() * width), (int)(Math.random() * height), Direction.NORTH));
            }
        });

        statsLabel = new JLabel("Ants: 1 | Steps: 0");
        
        controlPanel.add(startBtn);
        controlPanel.add(addAntBtn);
        controlPanel.add(statsLabel);
        add(controlPanel, BorderLayout.SOUTH);

        Timer timer = new Timer(16, this::update);
        timer.start();
    }

    private void update(ActionEvent e) {
        if (running) {
            engine.simulateSteps(stepsPerFrame);
            statsLabel.setText("Ants: " + ants.size());
            canvas.repaint();
        }
    }

    private void drawGrid(Graphics g) {
        int cellW = Math.max(1, canvas.getWidth() / width);
        int cellH = Math.max(1, canvas.getHeight() / height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid.getColor(x, y) == com.langton.model.CellColor.BLACK) {
                    g.setColor(Color.BLACK);
                    g.fillRect(x * cellW, y * cellH, cellW, cellH);
                }
            }
        }

        g.setColor(Color.RED);
        for (Ant ant : ants) {
            g.fillRect(ant.getX() * cellW, ant.getY() * cellH, cellW, cellH);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationUI ui = new SimulationUI();
            ui.setVisible(true);
        });
    }
}
