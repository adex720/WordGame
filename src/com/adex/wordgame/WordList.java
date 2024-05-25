package com.adex.wordgame;

import java.util.Random;

public class WordList {

    public static void init() {

    }


    public static char[] get4Letters(Random r) {
        return new char[]{getLetter(r), getLetter(r), getLetter(r), getLetter(r)};
    }

    public static char getLetter(Random random) {
        return (char) ('A' + random.nextInt(26));
    }

    public static int getScore(String word) {
        return isWord(word) ? getWordScore(word) : 0;
    }

    public static boolean isWord(String word) {
        return true;
    }

    public static int getWordScore(String word) {
        int score = 0;
        for (int c : word.chars().toArray()) {
            score += getLetterScore((char) c);
        }

        return score;
    }

    public static int getLetterScore(char letter) {
        return 1;
    }
}
