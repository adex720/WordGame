package com.adex.wordgame.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Used for filtering too short and long words out.
 * Compresses languages when a new one is added.
 */
public class LanguageCreator {

    public static void main(String[] args) {
        createLanguage("english", 4, 12);
    }

    public static void createLanguage(String filename, int minLength, int maxLength) {

        String[] rawWords;
        try {
            rawWords = new String(DataReader.getFileContent("resources/languages/raw/" + filename + ".txt")).split("\n");
        } catch (IOException e) {
            System.out.println("Failed to read source file:\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            return;
        }

        ArrayList<String> validWords = new ArrayList<>((int) (rawWords.length * 0.8f));

        for (String word : rawWords) {
            if (word.length() < minLength || word.length() > maxLength) continue;

            validWords.add(word);
        }

        StringBuilder sb = new StringBuilder();
        for (String word : validWords) sb.append(",").append(word);
        byte[] encoded = FileCompressor.encode(sb.substring(1).getBytes(StandardCharsets.UTF_8));

        try {
            writeFile(encoded, "resources/languages/" + filename + ".lng");
            System.out.println("Successfully wrote language: " + filename);
        } catch (IOException e) {
            System.out.println("Failed to write file:\n" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void writeFile(byte[] content, String path) throws IOException {
        FileOutputStream writer = new FileOutputStream(path);
        writer.write(content);
        writer.flush();
    }

}
