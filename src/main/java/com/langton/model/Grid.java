package com.langton.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Grid {
    // Sparse storage: Array of maps (one per region) to store cell colors (0=WHITE, 1=BLACK).
    // Each map is protected by its corresponding region lock.
    private final Map<Long, Byte>[] regionCells;
    private final int width;
    private final int height;
    
    private final int regionSize;
    private final int numRegionsX;
    private final int numRegionsY;
    private final ReentrantLock[] regionLocks;

    @SuppressWarnings("unchecked")
    public Grid(int width, int height, int regionSize) {
        this.width = width;
        this.height = height;
        
        this.regionSize = regionSize;
        this.numRegionsX = (int) Math.ceil((double) width / regionSize);
        this.numRegionsY = (int) Math.ceil((double) height / regionSize);
        
        int totalRegions = numRegionsX * numRegionsY;
        this.regionCells = new HashMap[totalRegions];
        this.regionLocks = new ReentrantLock[totalRegions];
        for (int i = 0; i < totalRegions; i++) {
            regionCells[i] = new HashMap<>();
            regionLocks[i] = new ReentrantLock();
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Maps global (x,y) to a specific regional partition.
     */
    public int getRegionId(int x, int y) {
        int rx = Math.max(0, Math.min(x / regionSize, numRegionsX - 1));
        int ry = Math.max(0, Math.min(y / regionSize, numRegionsY - 1));
        return ry * numRegionsX + rx;
    }

    public ReentrantLock getLockForRegion(int regionId) {
        return regionLocks[regionId];
    }

    /**
     * Packs (x,y) into a 64-bit key for sparse storage.
     */
    private long getHashKey(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    /**
     * Retrieves color. Caller must hold the region lock.
     */
    public CellColor getColor(int x, int y) {
        int rid = getRegionId(x, y);
        Byte color = regionCells[rid].get(getHashKey(x, y));
        return (color == null || color == 0) ? CellColor.WHITE : CellColor.BLACK;
    }

    /**
     * Flips color. Caller must hold the region lock.
     */
    public void flipColor(int x, int y) {
        int rid = getRegionId(x, y);
        long key = getHashKey(x, y);
        regionCells[rid].compute(key, (k, v) -> (v == null || v == 0) ? (byte) 1 : (byte) 0);
    }
    
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getActiveCellCount() {
        int count = 0;
        for (Map<Long, Byte> map : regionCells) {
            count += map.size();
        }
        return count;
    }

    public Map<Long, Byte>[] getRegionCells() {
        return regionCells;
    }

    public static int getXFromKey(long key) {
        return (int) (key >> 32);
    }

    public static int getYFromKey(long key) {
        return (int) key;
    }
}
