package com.adex.wordgame;

import java.util.Arrays;

public class WordGame {
    private int width;
    private int height;

    private int score;

    private char[][] tiles;
    private Tile tile;

    private WordGame(int width, int height) {
        this.height = height;
        this.width = width;

        score = 0;

        tile = null;
    }

    public static WordGame create() {
        return create(12, 10);
    }

    public static WordGame create(int width, int height) {
        return new WordGame(width, height);
    }

    public void tick() {
        if (canMoveDown()) {
            tile.y--;
        } else {
            calculateScore();
        }
    }

    public void addTile(Tile tile) {
        if (this.tile != null) {
            System.out.println("Already a tile in game!");
            return;
        }

        this.tile = tile;
        tile.y = height - 1;
        tile.x = width / 2;
    }

    private void calculateScore() {


        tile = null;
    }

    public boolean canMoveDown() {
        return true;
    }

    public int getWidth() {
        return width;
    }

    public void increaseWidth(int increase) {
        char[][] newBoard = new char[height][];
        for (int y = 0; y < height; y++) {
            System.arraycopy(tiles[y], 0, newBoard[y], increase / 2, width);
            Arrays.fill(newBoard[y], 0, increase / 2, ' ');
            Arrays.fill(newBoard[y], width + increase / 2, width + increase, ' ');
        }

        tile.x += increase / 2;
        width += increase;
        tiles = newBoard;
    }

    public void increaseWidthBy2() {
        char[][] newBoard = new char[height][];
        for (int y = 0; y < height; y++) {
            System.arraycopy(tiles[y], 0, newBoard[y], 1, width);
            newBoard[y][0] = ' ';
            newBoard[y][width + 1] = ' ';
        }

        tile.x++;
        width += 2;
        tiles = newBoard;
    }

    public int getHeight() {
        return height;
    }

    public int getScore() {
        return score;
    }
}
