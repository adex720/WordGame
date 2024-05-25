package com.adex.wordgame;

import java.util.Arrays;

public class WordGame {

    public static final int MIN_WORD_LENGTH = 4;
    public static final int MAX_WORD_LENGTH = 9;

    private int width;
    private int height;

    private int score;

    private char[][] tiles;
    private Tile tile;

    private WordGame(int width, int height) {
        this.height = height;
        this.width = width;

        tiles = new char[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(tiles[y], 0, width, ' ');
        }

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
            score += calculateScore();
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

    public int calculateScore() {
        if (tile == null) return 0;

        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;

        int[] offsets = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = offsets[i];
            int y = offsets[i + 1];

            if (x > minX) minX = x;
            if (x < maxX) maxX = x;
            if (y > minY) minY = y;
            if (y < maxY) maxY = y;
        }

        int score = 0;


        tile = null;
        return score;
    }

    /**
     * Calculates the score the tile gets on the row.
     * Words must be from left to right.
     * Loops trough each possible word position to check the score.
     * <p>
     * O(n^2)
     *
     * @param y         Y coordinate of the row
     * @param startX    Smallest x coordinate of adjacent tiles on the row to the left of the tile
     * @param endX      Largest x coordinate of adjacent tiles on the row to the right of the tile
     * @param tileStart Smallest x coordinate of the tile on the row
     * @param tileEnd   Largest x coordinate of the tile on the row
     * @return score of the row
     */
    public int calculateRowScore(int y, int startX, int endX, int tileStart, int tileEnd) {
        int score = 0;

        // First index where a valid word can start
        int firstPossibleWordStart = Math.max(startX, tileStart - MAX_WORD_LENGTH + 1);
        // Last index where a valid word can start
        int lastPossibleStart = Math.min(tileEnd, endX - MIN_WORD_LENGTH + 1);

        for (int start = firstPossibleWordStart; start <= lastPossibleStart; start++) {
            String word = "";

            // Index at which the words contain a letter from the current tile and is long enough
            int firstPossibleStart = Math.max(tileStart, start + MIN_WORD_LENGTH - 1);
            for (int i = start; i < firstPossibleStart; i++) {
                word += tiles[y][i];
            }

            // Last index where a valid word can reach
            int lastPossibleEnd = Math.min(endX, start + MAX_WORD_LENGTH - 1);
            for (int end = firstPossibleStart; end <= lastPossibleEnd; end++) {
                word += tiles[y][end];
                score += WordList.getScore(word);
            }
        }

        return score;
    }

    /**
     * Calculates the score the tile gets on the column.
     * Words must be from up to down.
     * Loops trough each possible word position to check the score.
     * <p>
     * O(n^2)
     *
     * @param x         X coordinate of the column
     * @param startY    Smallest y coordinate of adjacent tiles on the column below the tile
     * @param endY      Largest y coordinate of adjacent tiles on the column above the tile
     * @param tileStart Smallest y coordinate of the tile on the column
     * @param tileEnd   Largest y coordinate of the tile on the column
     * @return score of the row
     */
    public int calculateColumnScore(int x, int startY, int endY, int tileStart, int tileEnd) {
        int score = 0;

        // First index where a valid word can start
        int firstPossibleWordStart = Math.max(endY, tileEnd + MAX_WORD_LENGTH - 1);
        // Last index where a valid word can start
        int lastPossibleStart = Math.min(tileStart, startY + MIN_WORD_LENGTH - 1);

        for (int start = firstPossibleWordStart; start >= lastPossibleStart; start--) {
            String word = "";

            // Index at which the words contain a letter from the current tile and is long enough
            int firstPossibleStart = Math.max(tileStart, start - MIN_WORD_LENGTH + 1);
            for (int i = start; i > firstPossibleStart - 1; i--) {
                word += tiles[i][x];
            }

            // Last index where a valid word can reach
            int lastPossibleEnd = Math.min(startY, start - MAX_WORD_LENGTH + 1);
            for (int end = firstPossibleStart; end >= lastPossibleEnd; end--) {
                word += tiles[end][x];
                score += WordList.getScore(word);
            }
        }

        return score;
    }

    public boolean canMoveDown() {
        int[] coords = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = coords[i];
            int y = coords[i + 1];

            if (tiles[y][x] != ' ') return false;
        }

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

    public void increaseHeight(int increase) {
        char[][] newBoard = new char[height + increase][width];

        System.arraycopy(tiles, 0, newBoard, 0, height);

        for (int y = height; y < height + increase; y++) {
            Arrays.fill(newBoard[y], 0, width, ' ');
        }

        height += increase;
        tiles = newBoard;
    }

    public int getHeight() {
        return height;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean newLine = false;

        for (int y = height - 1; y >= 0; y--) {
            if (newLine) {
                sb.append('\n');
            }
            newLine = true;

            for (char c : tiles[y]) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
