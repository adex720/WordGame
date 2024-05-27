package com.adex.wordgame.file;

import com.adex.wordgame.util.Pair;
import com.adex.wordgame.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * TODO: explain compression
 */
public class FileCompressor {

    public static final double LN2 = Math.log(2d);

    /**
     * Encoded text should only consist of capital letters and words being separated by ",".
     * For non-English characters, see TODO: do this in the UI code
     *
     * @param words             String containing all words, separated by a comma
     * @param letterScores      Array of letter scores; range: 0—15
     * @param letterFrequencies Array of letter frequencies; range: 0—4095
     */
    public static byte[] encode(byte[] words, int[] letterScores, int[] letterFrequencies) {
        int length = words.length;
        int differentLetterCount = 1;
        int difference;
        for (int i = 0; i < length; i++) {
            difference = words[i] - 'A'; // when hitting a comma, this will be negative
            if (difference >= differentLetterCount)
                differentLetterCount = difference + 1;
        }

        if ((length & 1) == 1) {
            length += 1;
            byte[] b = new byte[words.length + 1];
            System.arraycopy(words, 0, b, 1, words.length);
            b[0] = (byte) ',';
            words = b;
        }

        LinkedList<Short> toCode = new LinkedList<>();

        int differentLettersSquared = differentLetterCount * differentLetterCount;
        int combiantionCount = differentLettersSquared + differentLetterCount * 2;
        int[] frequencies = new int[combiantionCount]; // accounting for commas
        int first, second;
        short id;
        for (int i = 0; i < length; i += 2) {
            first = words[i] - 'A';
            second = words[i + 1] - 'A';


            if (first < 0) { // comma
                id = (short) (differentLettersSquared + second);
            } else if (second < 0) { // comma
                id = (short) (differentLettersSquared + differentLetterCount + first);
            } else {
                id = (short) (differentLetterCount * first + second);
            }
            frequencies[id]++;
            toCode.add(id);
        }

        int nodeValueBitLength = (int) Math.ceil(Math.log(combiantionCount) / LN2 + 0.000001d); // log2 x = ln x / ln 2 , adding .000001 to account for rounding errors

        // Creating frequencyTree
        PriorityQueue<Pair<Integer, FrequencyTree.Node>> queue = new PriorityQueue<>(Comparator.comparingInt(e -> e.first));
        for (short i = 0; i < combiantionCount; i++) {
            if (frequencies[i] == 0) continue;
            queue.add(new Pair<>(frequencies[i], new FrequencyTree.Node(i)));
        }

        while (queue.size() > 1) {
            Pair<Integer, FrequencyTree.Node> left = queue.peek();
            queue.remove();
            Pair<Integer, FrequencyTree.Node> right = queue.peek();
            queue.remove();

            queue.add(new Pair<>(left.first + right.first, new FrequencyTree.Node(left.second, right.second)));
        }

        Pair<Integer, FrequencyTree.Node> top = queue.peek();
        queue.remove();
        FrequencyTree frequencyTree = new FrequencyTree(top.second, combiantionCount);

        ArrayList<Boolean> text = new ArrayList<>();

        while (toCode.size() > 0) {
            id = toCode.removeFirst();
            for (boolean step : frequencyTree.getPath(id)) text.add(step);
        }

        ArrayList<Byte> output = new ArrayList<>();
        // Setting the required size a bit higher than the largest possible size to save memory
        output.ensureCapacity(text.size() + differentLetterCount *
                2 + combiantionCount * nodeValueBitLength * nodeValueBitLength * nodeValueBitLength);

        // adding letter count and nodeValueBitLength to encoded
        output.add((byte) differentLetterCount);
        output.add((byte) ((nodeValueBitLength >> 8) & 0xFF));
        output.add((byte) (nodeValueBitLength & 0xFF));

        // Add letter scores and frequencies
        int score, freq;
        for (int i = 0; i < differentLetterCount - 1; i++) { // -1 because comma is included in letter count
            score = letterScores[i];
            freq = letterFrequencies[i];

            output.add((byte) (((score & 0xf) << 4) + ((freq >> 8) & 0xf)));
            output.add((byte) (freq & 0xff));
        }

        // add tree to encoded
        int i = 0;
        byte current = 0;
        for (boolean b : frequencyTree.getBits(nodeValueBitLength)) {
            if (b) current |= (1 << i);
            i++;

            if (i < 8) continue;

            output.add(current);
            current = 0;
            i = 0;
        }

        int textLength = text.size();
        // Calculating how many empty bits until end of last byte.
        // Letter count and nodeValueBitLength are 8 and 16 bits, both divisible by 8, so they don't need to be counted
        // Ie if encoded length is 642 bits, the last byte will have 2 bits used, therefore the value will be 8 - 2 = 6
        // range: [1, 8]
        int empty = 8 - (i + textLength) % 8;
        // The empty bits are stored between the tree and the text.
        // The last empty bit is 1 and the rest are 0

        if (i + empty < 8) { // The last empty bit is in the same byte but not the last
            i += empty;
            current |= (1 << (i - 1));
        } else if (i + empty == 8) { // The last empty bit is the last bit of the byte
            current |= (0x80);
            output.add(current);
            current = 0;
            i = 0;
        } else { // The last empty bit is in the next byte
            output.add(current);
            i += empty - 8;
            current = (byte) (1 << (i - 1));
        }

        // Adding text
        for (boolean b : text) {
            if (b) current |= (1 << i);
            i++;

            if (i < 8) continue;

            output.add(current);
            current = 0;
            i = 0;
        }

        if (i != 0) {
            System.out.println("Empty space filling was wrong length!");
        }

        return Util.convertByteArray(output.toArray(new Byte[0]));
    }

    public static String decode(final byte[] bytes) {

        int differentLetters = bytes[0];
        int nodeValueBitLength = (bytes[1] << 8) + bytes[2];
        int combinationCount = differentLetters * differentLetters + 2 * differentLetters;
        int lettersSquared = differentLetters * differentLetters;

        // Reading letter scores and frequencies

        StringBuilder sb = new StringBuilder();
        boolean notFirst = false;
        for (int i = 0; i < differentLetters - 1; i++) {
            int first = bytes[2 * i + 3];
            int second = bytes[2 * i + 4];

            if (notFirst) sb.append('.');
            notFirst = true;

            sb.append((first & 0xf0) >> 4).append(':').append(((0xf & first) << 8) + (0xff & second)); // score : frequency
        }

        sb.append(','); // Separating from rest

        // Creating DataFetcher
        final int[] i = new int[2]; // first is index at byte, second is byte index
        i[1] = 3 + 2 * differentLetters - 2;
        FrequencyTree.DataFetcher data = () -> {
            boolean value = (bytes[i[1]] & (1 << i[0])) >= 1;
            i[0]++;
            if (i[0] >= 8) {
                i[0] = 0;
                i[1]++;
            }
            return value;
        };

        FrequencyTree frequencyTree = new FrequencyTree(data, nodeValueBitLength, combinationCount); // Reading tree
        while (!data.getNext()) ; // Skipping empty bits

        // Reading text
        StringBuilder text = new StringBuilder(sb);
        char first, second;
        while (i[1] < bytes.length) {
            // Covert id to 2 letters
            int id = frequencyTree.getValue(data);
            if (id < lettersSquared) {
                first = (char) (id / differentLetters + 'A');
                second = (char) (id % differentLetters + 'A');
            } else if (id < lettersSquared + differentLetters) {
                first = ',';
                second = (char) (id - lettersSquared + 'A');
            } else {
                first = (char) (id - lettersSquared - differentLetters + 'A');
                second = ',';
            }

            // Add letters to text
            text.append(first).append(second);
        }

        if (text.charAt(0) == ',') sb.append(text.substring(1));
        else sb.append(text);

        return sb.toString();
    }
}
