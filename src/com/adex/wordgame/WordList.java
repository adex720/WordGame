package com.adex.wordgame;

import com.adex.wordgame.file.DataReader;
import com.adex.wordgame.util.Pair;

import java.util.*;

public class WordList {

    private int letterCount;

    private HashSet<String> words;
    private int[] scores;
    private int[] frequencies;
    private int frequenciesSum;

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
            Pair<ArrayList<Pair<Integer, Integer>>, String[]> result = DataReader.getLanguageWords(language.path);
            String[] words = result.second;
            this.words = new HashSet<>(words.length, 1);
            this.words.addAll(Arrays.asList(words));

            letterCount = result.first.size();
            scores = new int[letterCount];
            frequencies = new int[letterCount];

            frequenciesSum = 0;
            ArrayList<Pair<Integer, Integer>> data = result.first;
            int frequency;
            for (int i = 0; i < letterCount; i++) {
                scores[i] = data.get(i).first;
                frequency = data.get(i).second;
                frequencies[i] = frequency;
                frequenciesSum += frequency;
            }

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
        int x = random.nextInt(frequenciesSum);
        for (int i = 0; i < letterCount; i++) {
            x -= frequencies[i];
            if (x < 0) return (char) (i + 'A');
        }

        return (char) ('A' + letterCount - 1);
    }

    public int getScore(String word) {
        return isWord(word) ? getWordScore(word) : 0;
    }

    public boolean isWord(String word) {
        return words.contains(word);
    }

    public int getWordScore(String word) {
        int score = 1;
        for (int c : word.chars().toArray()) {
            score *= getLetterScore((char) c);
        }

        return score;
    }

    public int getLetterScore(char letter) {
        return scores[letter - 'A'];
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
