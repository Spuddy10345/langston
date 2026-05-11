package com.langton.model;

public enum CellColor {
    WHITE,
    BLACK;

    public CellColor flip() {
        return this == WHITE ? BLACK : WHITE;
    }
}
