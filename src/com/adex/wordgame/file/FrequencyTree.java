package com.adex.wordgame.file;

public class FrequencyTree {

    public final Node root;
    private final boolean[][] paths;

    public FrequencyTree(Node root, int size) {
        this.root = root;
        paths = generatePaths(size);
    }

    /**
     * @param dataFetcher Bits
     * @param n           Amount of bits used for storing of each value
     */
    public FrequencyTree(DataFetcher dataFetcher, int n, int size) {
        this.root = Node.fromBits(dataFetcher, n);
        paths = generatePaths(size);
    }

    private boolean[][] generatePaths(int size) {
        boolean[][] paths = new boolean[size][];
        root.addPath(paths, new boolean[0]);
        return paths;
    }

    public boolean[] getBits(int n) {
        return root.getBits(n);
    }

    public boolean[] getPath(short destination) {
        return paths[destination];
    }

    public short getValue(DataFetcher data) {
        return root.getValue(data);
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static class Node {
        public final boolean hasValue;
        public final short value;
        public final Node left;
        public final Node right;

        public Node(short value) {
            hasValue = true;
            this.value = value;
            left = null;
            right = null;
        }

        public Node(Node left, Node right) {
            hasValue = false;
            value = -1;
            this.left = left;
            this.right = right;
        }

        public static boolean[] getLastNBits(short s, int n) {
            boolean[] bits = new boolean[n];
            for (int i = 0; i < n; i++) {
                bits[i] = (s & (1 << i)) >= 1;
            }
            return bits;
        }

        public static Node fromBits(DataFetcher data, int n) {
            if (data.getNext()) return new Node(fromBits(data, n), fromBits(data, n));

            short s = 0;
            for (int i = 1; i < 1 << n; i <<= 1) {
                if (data.getNext()) s += i;
            }
            return new Node(s);
        }

        public boolean[] getBits(int n) {
            if (hasValue) {
                boolean[] end = getLastNBits(value, n);
                boolean[] bytes = new boolean[end.length + 1];
                System.arraycopy(end, 0, bytes, 1, end.length);
                return bytes;
            }

            boolean[] left = this.left.getBits(n);
            boolean[] right = this.right.getBits(n);
            boolean[] bytes = new boolean[left.length + right.length + 1];
            bytes[0] = true;
            System.arraycopy(left, 0, bytes, 1, left.length);
            System.arraycopy(right, 0, bytes, 1 + left.length, right.length);
            return bytes;
        }

        public void addPath(boolean[][] paths, boolean[] start) {
            if (hasValue) {
                paths[value] = start;
                return;
            }

            boolean[] pathLeft = new boolean[start.length + 1];
            System.arraycopy(start, 0, pathLeft, 0, start.length);
            boolean[] pathRight = pathLeft.clone();
            pathRight[start.length] = true;

            left.addPath(paths, pathLeft);
            right.addPath(paths, pathRight);
        }

        public short getValue(DataFetcher data) {
            if (hasValue) return value;

            return (data.getNext() ? right : left).getValue(data);
        }

        @Override
        public String toString() {
            if (hasValue) return value + "";
            return "[" + left + "," + right + "]";
        }
    }

    public interface DataFetcher {
        boolean getNext();
    }
}
