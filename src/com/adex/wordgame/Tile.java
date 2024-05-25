package com.adex.wordgame;

import java.util.Random;

public class Tile {

    public static int[][] OFFSET_I = {{0, 0, 1, 0, 2, 0, 3, 0}, {0, 3, 0, 2, 0, 1, 0, 0}, {3, 0, 2, 0, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 2, 0, 3}};
    public static int[][] OFFSET_J = {{0, 1, 0, 0, 1, 0, 2, 0}, {1, 0, 0, 0, 0, -1, 0, -2}, {0, -1, 0, 0, -1, 0, -2, 0}, {-1, 0, 0, 0, 0, 1, 0, 2}};
    public static int[][] OFFSET_L = {{0, -1, 0, 0, 1, 0, 2, 0}, {-1, 0, 0, 0, 0, -1, 0, -2}, {0, 1, 0, 0, -1, 0, -2, 0}, {1, 0, 0, 0, 0, 1, 0, 2}};
    public static int[][] OFFSET_S = {{-1, 0, 0, 0, 0, 1, 1, 1}, {0, 1, 0, 0, 1, 0, 1, -1}, {1, 0, 0, 0, 0, -1, -1, -1}, {0, -1, 0, 0, -1, 0, -1, 1}};
    public static int[][] OFFSET_Z = {{1, 0, 0, 0, 0, 1, -1, 1}, {0, -1, 0, 0, 1, 0, 1, 1}, {1, 0, 0, 0, 0, 1, -1, 1}, {0, 1, 0, 0, -1, 0, -1, -1}};
    public static int[][] OFFSET_T = {{-1, 0, 0, 0, 0, 1, 1, 0}, {0, 1, 0, 0, 1, 0, 0, -1}, {1, 0, 0, 0, 0, -1, -1, 0}, {0, -1, 0, 0, -1, 0, 0, 1}};
    public static int[][] OFFSET_O = {{0, 0, 1, 0, 1, 1, 0, 1}, {0, 1, 0, 0, 1, 0, 1, 1}, {1, 1, 0, 1, 0, 0, 1, 0}, {1, 0, 1, 1, 0, 1, 0, 0,}};

    public int x;
    public int y;

    public final char[] letters;

    public final Shape shape;
    public Rotation rotation;

    private int[] last_offest = {};

    public Tile(char[] letters, Shape shape, Rotation rotation) {
        this.letters = letters;

        this.shape = shape;
        this.rotation = rotation;

        x = -1;
        y = -1;
    }

    public static Tile create(char[] letters, Shape shape, Rotation rotation) {
        return new Tile(letters, shape, rotation);
    }

    public static Tile create(char[] letters, Shape shape) {
        return create(letters, shape, Rotation.NONE);
    }

    public static Tile create(char[] letters, int shape) {
        if (shape < 0 || shape > 6) {
            System.out.println("Invalid shape id: " + shape);
        }

        return create(letters, new Shape[]{Shape.I, Shape.J, Shape.L, Shape.S, Shape.Z, Shape.T, Shape.O}[shape]);
    }

    public static Tile create(Random random) {
        return create(WordList.get4Letters(random), random.nextInt(6));
    }

    public void rotateCounterClockwise() {
        rotation = rotation.previous;
        last_offest = new int[0];
    }

    public void rotateClockwise() {
        rotation = rotation.next;
        last_offest = new int[0];
    }

    public static int[] getOffSets(Shape shape, Rotation rotation) {
        int id = rotation == Rotation.NONE ? 1 : rotation == Rotation.RIGHT ? 2 : rotation == Rotation.HALF ? 3 : 4;

        return switch (shape) {
            case I -> OFFSET_I[id];
            case J -> OFFSET_J[id];
            case L -> OFFSET_L[id];
            case S -> OFFSET_S[id];
            case Z -> OFFSET_Z[id];
            case T -> OFFSET_T[id];
            case O -> OFFSET_O[id];
        };
    }


    public int[] getOffSets() {
        if (last_offest.length == 0)
            last_offest = getOffSets(shape, rotation);

        return last_offest;
    }

    public int getMaxYOffset() {
        int[] offsets = getOffSets();
        int maxYOffset = 0;
        for (int i = 1; i < 8; i += 2) {
            if (offsets[i] > maxYOffset) maxYOffset = offsets[i];
        }

        return maxYOffset;
    }

    public boolean isIn(int x, int y) {
        int[] offSet = getOffSets();
        for (int i = 0; i < 8; i += 2) {
            if (offSet[i] == x && offSet[i + 1] == y) return true;
        }

        return false;
    }

    public enum Shape {
        I, J, L, S, Z, T, O
    }

    public enum Rotation {
        NONE, RIGHT, HALF, LEFT;

        private Rotation previous;
        private Rotation next;

        static {
            NONE.next = RIGHT;
            NONE.previous = LEFT;
            RIGHT.next = HALF;
            RIGHT.previous = NONE;
            HALF.next = LEFT;
            HALF.previous = RIGHT;
            LEFT.next = NONE;
            LEFT.previous = HALF;
        }
    }
}
