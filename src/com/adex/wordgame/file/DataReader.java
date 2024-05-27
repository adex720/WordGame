package com.adex.wordgame.file;

import com.adex.wordgame.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DataReader {

    public static byte[] getFileContent(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    /**
     * Returns a pair containing <p>
     * - an ArrayList of pairs of letter scores and frequencies <p>
     * - array of words
     * <p>
     * I am aware that this is not a clean implementation, but it works.
     * I may change it to get the arrays as parameter and edit them in the future,
     * but it's not high on the priority list.
     */
    public static Pair<ArrayList<Pair<Integer, Integer>>, String[]> getLanguageWords(String language) throws IOException {
        String[] result = FileCompressor.decode(getFileContent("resources/languages/" + language + ".lng")).split(",");
        String[] words = new String[result.length - 1];
        System.arraycopy(result, 1, words, 0, words.length);

        String[] letters = result[0].split("\\.");
        ArrayList<Pair<Integer, Integer>> letterData = new ArrayList<>();
        for (String letter : letters) {
            String[] data = letter.split(":");
            letterData.add(new Pair<>(Integer.parseInt(data[0]), Integer.parseInt(data[1])));
        }

        return new Pair<>(letterData, words);
    }

}
