package com.langton.model;

import java.util.concurrent.locks.ReentrantLock;

public class Grid {
    private final byte[] cells;
    private final int width;
    private final int height;
    
    private final int regionSize;
    private final int numRegionsX;
    private final int numRegionsY;
    private final ReentrantLock[] regionLocks;

    public Grid(int width, int height, int regionSize) {
        this.width = width;
        this.height = height;
        this.cells = new byte[width * height];
        
        this.regionSize = regionSize;
        this.numRegionsX = (int) Math.ceil((double) width / regionSize);
        this.numRegionsY = (int) Math.ceil((double) height / regionSize);
        this.regionLocks = new ReentrantLock[numRegionsX * numRegionsY];
        for (int i = 0; i < regionLocks.length; i++) {
            regionLocks[i] = new ReentrantLock();
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getRegionId(int x, int y) {
        int rx = x / regionSize;
        int ry = y / regionSize;
        return ry * numRegionsX + rx;
    }

    public ReentrantLock getLockForRegion(int regionId) {
        return regionLocks[regionId];
    }

    public CellColor getColor(int x, int y) {
        return cells[y * width + x] == 0 ? CellColor.WHITE : CellColor.BLACK;
    }

    public void flipColor(int x, int y) {
        cells[y * width + x] = (byte) (1 - cells[y * width + x]);
    }
    
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
