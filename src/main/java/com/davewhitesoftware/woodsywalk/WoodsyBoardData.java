package com.davewhitesoftware.woodsywalk;

//  Object representing a game board for one player in Woodsy Walk.
//  This is the internal data representation, NOT the UI view object-- that's WoodsyBoardView.

import java.io.Serializable;

public class WoodsyBoardData implements Serializable {
    private int[][] board;
    private int height, width;

    WoodsyBoardData() {
        // board constructor: board starts out empty.
        this.board = new int[7][8];  // rows first, then columns
        this.height = 7;
        this.width = 8;
        for (int i = 0; i < this.height; i++)
            for (int j = 0; j < this.width; j++)
                this.board[i][j] = 0;
    }

    //-- Getters and setters
    public int getHeight() { return this.height; }
    public int getWidth() { return this.width; }
    public int getCell(int x, int y) {
        if (x < 0 || x >= this.width) return 0;
        if (y < 0 || y >= this.height) return 0;
        return this.board[y][x];
    }
    public void setCell(int x, int y, int p) {
        if (x < 0 || x >= this.width) return;
        if (y < 0 || y >= this.height) return;
        this.board[y][x] = p;
    }
    //-- Copy a board
    public void copyFrom(WoodsyBoardData b) {
        for (int i = 0; i < b.height; i++) {
            for (int j = 0; j < b.width; j++) {
                this.setCell(i,j,b.board[i][j]);
            }
        }
    }

}
