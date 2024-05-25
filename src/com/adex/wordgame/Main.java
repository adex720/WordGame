package com.adex.wordgame;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        WordGame game = WordGame.create(ThreadLocalRandom.current());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String in = scanner.nextLine();

            if (game.tick()) {
                System.out.println(game);
            } else {
                System.out.println(game.getScore());
                break;
            }
        }
    }
}
