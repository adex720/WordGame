package com.adex.wordgame.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataReader {

    public static byte[] getFileContent(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static String[] getLanguageWords(String language) throws IOException {
        return new String(FileCompressor.encode(getFileContent("resources/languages/" + language + ".lng"))).split(",");
    }

}
