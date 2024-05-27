package com.adex.wordgame.file;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Used for formatting word list from source format into a list of words.
 * Also filters out words containing invalid characters such as hyphens.
 */
public class LangaugeFormatter {

    public static void main(String[] args) throws IOException {
        formatWikipedia("resources/languages/raw/english2.txt", "resources/languages/raw/english.txt");
    }

    public static void formatWikipedia(String in, String out) throws IOException {
        String[] s1 = new String(DataReader.getFileContent(in)).split("\n");

        ArrayList<String> s2 = new ArrayList<>();
        for (String s : s1) {
            String s3 = s.split("\t")[1].toLowerCase(Locale.ROOT);
            if (!s3.matches("^[a-z]+$"))
                continue;

            s2.add("\n" + s3);
        }

        s2.set(0, s2.get(0).substring(1));

        FileWriter writer = new FileWriter(out);

        for (String s : s2) {
            writer.write(s);
        }

        writer.flush();
    }
}
