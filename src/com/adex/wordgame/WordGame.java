package com.adex.wordgame;

import java.util.Arrays;
import java.util.Random;

public class WordGame {

    public static final int MIN_WORD_LENGTH = 4;
    public static final int MAX_WORD_LENGTH = 9;

    public static final int DEFAULT_GAME_WIDTH = 12;
    public static final int DEFAULT_GAME_HEIGHT = 10;

    private final Random random;
    private final WordList wordList;

    private int width;
    private int height;

    private int score;

    private char[][] tiles;
    private Tile tile;

    private WordGame(Random random, WordList wordList, int width, int height) {
        this.random = random;
        this.wordList = wordList;
        this.height = height;
        this.width = width;

        tiles = new char[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(tiles[y], 0, width, ' ');
        }

        score = 0;

        tile = null;
    }

    public static WordGame create(Random random, WordList wordList) {
        return create(DEFAULT_GAME_WIDTH, DEFAULT_GAME_HEIGHT, random, wordList);
    }

    public static WordGame create(Random random, WordList.Language language) {
        return create(random, WordList.get(language));
    }

    public static WordGame create(Random random) {
        return create(random, WordList.defaultLanguage());
    }

    public static WordGame create(int width, int height, Random random, WordList wordList) {
        return new WordGame(random, wordList, width, height);
    }

    public boolean tick() {
        if (tile == null) {
            createTile();
            return true;
        }

        if (canMoveDown()) {
            tile.y--;
            return true;
        } else {
            return placeTile();
        }
    }

    public void createTile() {
        addTile(Tile.create(random));
    }

    public void addTile(Tile tile) {
        if (this.tile != null) {
            System.out.println("Already a tile in game!");
            return;
        }

        this.tile = tile;
        tile.y = height + 1; // TODO: decrease by 1 and check validness on spawn
        tile.x = width / 2;
    }

    public boolean placeTile() {
        if (tile.y + tile.getMaxYOffset() >= height) {
            end();
            return false;
        }

        // add letters to board
        addTileToBoard();

        // add score
        score += calculateScore();

        tile = null; // remove tile
        return true;
    }

    public void end() {
        System.out.println("Score: " + score);
    }

    private void addTileToBoard() {
        if (tile == null) return;

        int[] offsets = tile.getOffSets();
        for (int i = 0; i < 4; i++) {
            int x = tile.x + offsets[2 * i];
            int y = tile.y + offsets[2 * i + 1];

            if (y >= height) continue;

            tiles[y][x] = tile.letters[i];
        }
    }

    /**
     * Adds empty tiles to current position of the tile after they have temporarily been added.
     */
    private void removeTileToBoard() {
        if (tile == null) return;

        int[] offsets = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = tile.x + offsets[i];
            int y = tile.y + offsets[i + 1];

            if (y >= height) continue;

            tiles[y][x] = ' ';
        }
    }

    public boolean dropDownByOne() {
        if (tile == null) return true;

        if (canMoveDown()) {
            tile.y--;
            return true;
        } else {
            return placeTile();
        }
    }

    public boolean dropDownFull() {
        if (tile == null) return true;

        if (!canMoveDown()) {
            return placeTile();
        }

        do tile.y--; while (canMoveDown());
        return true;
    }

    public int calculateScore() {
        if (tile == null) return 0;

        // Find min and max offsets of the tile
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;

        int[] offsets = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = offsets[i];
            int y = offsets[i + 1];

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // Change offset to coordinate
        minX += tile.x;
        maxX += tile.x;
        minY += tile.y;
        maxY += tile.y;

        int score = 0;

        // calculate score of each column
        for (int x = minX; x <= maxX; x++) {

            // Find min and max of the tile in current column
            int tileStart = maxY;
            int tileEnd = minY;

            for (int i = 0; i < 8; i += 2) {
                int x2 = tile.x + offsets[i];
                int y2 = tile.y + offsets[i + 1];

                if (x2 != x) continue;

                if (y2 < tileStart) tileStart = y2;
                if (y2 > tileEnd) tileEnd = y2;
            }

            // Find mix and max of adjacent tiles in the current column
            int startY = tileStart;
            while (startY > 0 && tiles[startY - 1][x] != ' ') startY--;
            int endY = tileEnd;
            while (endY < height - 1 && tiles[endY + 1][x] != ' ') endY++;

            // Add score
            score += calculateColumnScore(x, startY, endY, tileStart, tileEnd);
        }

        // calculate score of each row
        for (int y = minY; y <= maxY; y++) {

            // Find min and max of the tile in current row
            int tileStart = maxX;
            int tileEnd = minX;

            for (int i = 0; i < 8; i += 2) {
                int x2 = tile.x + offsets[i];
                int y2 = tile.y + offsets[i + 1];

                if (y2 != y) continue;

                if (x2 < tileStart) tileStart = x2;
                if (x2 > tileEnd) tileEnd = x2;
            }

            // Find mix and max of adjacent tiles in the current column
            int startX = tileStart;
            while (startX > 0 && tiles[y][startX - 1] != ' ') startX--;
            int endX = tileEnd;
            while (endX < width - 1 && tiles[y][endX + 1] != ' ') endX++;

            // Add score
            score += calculateRowScore(y, startX, endX, tileStart, tileEnd);
        }

        return score;
    }

    /**
     * Calculates the score the tile gets on the row.
     * Letters must be added to {@link this.tiles} beforehand.
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
                score += wordList.getScore(word);
            }
        }

        return score;
    }

    /**
     * Calculates the score the tile gets on the column.
     * Letters must be added to {@link this.tiles} beforehand.
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
        int firstPossibleWordStart = Math.min(endY, tileEnd + MAX_WORD_LENGTH - 1);
        // Last index where a valid word can start
        int lastPossibleStart = Math.max(tileStart, startY + MIN_WORD_LENGTH - 1);

        for (int start = firstPossibleWordStart; start >= lastPossibleStart; start--) {
            String word = "";

            // Index at which the words contain a letter from the current tile and is long enough
            int firstPossibleStart = Math.min(tileStart, start - MIN_WORD_LENGTH + 1);
            for (int i = start; i > firstPossibleStart; i--) {
                word += tiles[i][x];
            }

            // Last index where a valid word can reach
            int lastPossibleEnd = Math.max(startY, start - MAX_WORD_LENGTH + 1);
            for (int end = firstPossibleStart; end >= lastPossibleEnd; end--) {
                word += tiles[end][x];
                score += wordList.getScore(word);
            }
        }

        return score;
    }

    public boolean canMoveDown() {
        int[] coords = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = tile.x + coords[i];
            int y = tile.y + coords[i + 1];

            if (y > height) continue;

            if (y < 1 || tiles[y - 1][x] != ' ') return false;
        }

        return true;
    }

    /**
     * Moves the tile one space to left
     * Checks if the tile can move and if not, does nothing
     */
    public void moveLeft() {
        if (canMoveLeft()) tile.x--;
    }

    /**
     * Checks if the tile can move to the left
     */
    public boolean canMoveLeft() {
        if (tile == null) return false;

        int[] offsets = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = tile.x + offsets[i] - 1;
            int y = tile.y + offsets[i + 1];

            if (x < 0) return false; // already at left border
            if (y >= height) continue; // not yet in board

            if (tiles[y][x] != ' ') return false; // tile occupied
        }

        return true;
    }

    /**
     * Moves the tile one space to right
     * Checks if the tile can move and if not, does nothing
     */
    public void moveRight() {
        if (canMoveRight()) tile.x++;
    }

    /**
     * Checks if the tile can move to the right
     */
    public boolean canMoveRight() {
        if (tile == null) return false;

        int[] offsets = tile.getOffSets();
        for (int i = 0; i < 8; i += 2) {
            int x = tile.x + offsets[i] + 1;
            int y = tile.y + offsets[i + 1];

            if (x >= width) return false; // already at right border
            if (y >= height) continue; // not yet in board

            if (tiles[y][x] != ' ') return false; // tile occupied
        }

        return true;
    }

    public void rotateClockwise() {
        if (canMoveClockwise()) tile.rotateClockwise();
    }

    public boolean canMoveClockwise() {
        if (tile == null) return false;

        int[] offsets = Tile.getOffSets(tile.shape, tile.getClockwiseRotation());
        for (int i = 0; i < 8; i += 2) {
            int x = tile.x + offsets[i];
            int y = tile.y + offsets[i + 1];

            if (x >= width || x < 0 || y < 0) return false; // outside board
            if (y >= height) continue; // not in board

            if (tiles[y][x] != ' ') return false; // tile occupied
        }

        return true;
    }

    public void rotateCounterClockwise() {
        if (canMoveCounterClockwise()) tile.rotateCounterClockwise();
    }

    public boolean canMoveCounterClockwise() {
        if (tile == null) return false;

        int[] offsets = Tile.getOffSets(tile.shape, tile.getCounterClockwiseRotation());
        for (int i = 0; i < 8; i += 2) {
            int x = tile.x + offsets[i];
            int y = tile.y + offsets[i + 1];

            if (x >= width || x < 0 || y < 0) return false; // outside board
            if (y >= height) continue; // not in board

            if (tiles[y][x] != ' ') return false; // tile occupied
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
        addTileToBoard(); // Adding tiles temporarily to the board for quicker formatting

        StringBuilder sb = new StringBuilder();
        boolean newLine = false;

        for (int y = height - 1; y >= 0; y--) {
            if (newLine) {
                sb.append('\n');
            }
            newLine = true;

            sb.append('|');
            for (char c : tiles[y]) {
                sb.append(c);
            }
            sb.append('|');
        }

        sb.append('\n').append((char) 0).append("—".repeat(width)).append((char) 0);

        removeTileToBoard();
        return sb.toString();
    }
}
