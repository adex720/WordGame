package com.adex.wordgame;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        WordGame game = WordGame.create(ThreadLocalRandom.current());

        Scanner scanner = new Scanner(System.in);
        loop:
        while (true) {
            System.out.println(game);

            String in = scanner.nextLine();
            if (!in.isEmpty()) switch (in.charAt(0)) {
                case 'a' -> game.moveLeft();
                case 'd' -> game.moveRight();
                case 's' -> {if (!game.dropDownByOne()) break loop;}
                case 'x' -> {if (!game.dropDownFull()) break loop;}
            }

            if (!game.tick()) break;
        }
    }
}
