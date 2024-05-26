package com.adex.wordgame;

import com.adex.wordgame.file.DataReader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

public class WordList {

    private static HashSet<String> WORDS;

    private Language language;

    public WordList(Language language) {
        setLanguage(language);
    }

    public void setLanguage(Language language) {
        this.language = language;
        loadWords();
    }

    private void loadWords() {
        try {
            String[] words = DataReader.getLanguageWords(language.path);
            WORDS = new HashSet<>(words.length, 1);
            WORDS.addAll(Arrays.asList(words));

        } catch (Exception e) {
            System.out.println("Failed to load language: " + language.name + ":\n" + e.getMessage() + "\n"
                    + Arrays.toString(e.getStackTrace()));
        }
    }

    public static WordList defaultLanguage() {
        return new WordList(Language.ENGLISH);
    }

    public static WordList get(Language language) {
        return new WordList(language);
    }

    public char[] get4Letters(Random r) {
        return new char[]{getLetter(r), getLetter(r), getLetter(r), getLetter(r)};
    }

    public char getLetter(Random random) {
        return new char[]{'q', 'u', 'e'}[random.nextInt(3)];
        //return (char) ('A' + random.nextInt(26));
    }

    public int getScore(String word) {
        return isWord(word) ? getWordScore(word) : 0;
    }

    public boolean isWord(String word) {
        if (WORDS.contains(word)) System.out.println(word);
        return WORDS.contains(word);
    }

    public int getWordScore(String word) {
        int score = 0;
        for (int c : word.chars().toArray()) {
            score += getLetterScore((char) c);
        }

        return score;
    }

    public int getLetterScore(char letter) {
        return 1;
    }

    public enum Language {
        ENGLISH("English");

        public String name;
        public String path;

        Language(String name) {
            this.name = name;
            this.path = name.toLowerCase(Locale.ROOT);
        }

        Language(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }
}
