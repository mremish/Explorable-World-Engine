package core;

import tileengine.TERenderer;
import tileengine.TETile;
import edu.princeton.cs.algs4.StdDraw;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Scanner;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        setupStdDraw();
        String input = displayMainMenu(ter, WIDTH, HEIGHT);

        if (input == null) {
            // Handle the error case where input is null.
            System.out.println("Error: Invalid input received.");
            return;
        }

        World world;

        if (input.startsWith("n")) {
            String seedString = input.replaceAll("[^0-9]", "");
            if (!seedString.isEmpty()) {
                try {
                    long seed = Long.parseLong(seedString);
                    world = new World(WIDTH, HEIGHT, seed);
                    world.addAvatar();
                    world.saveSeed();
                } catch (NumberFormatException e) {
                    // Handle the error where parsing seed into a long fails.
                    System.out.println("Error: Failed to parse seed into a long.");
                    return;
                }
            } else {
                // Handle the case where the input doesn't contain a valid seed.
                System.out.println("Error: Input does not contain a valid seed.");
                return;
            }
        } else if (input.startsWith("l")) {
            world = loadWorld();
            if (world == null) {
                System.out.println("No previous save found.");
                return;
            }
        } else {
            return;
        }

        ter.initialize(WIDTH, HEIGHT);
        runGameLoop(ter, world, WIDTH, HEIGHT);
    }



    private static void setupStdDraw() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.enableDoubleBuffering();
    }

    private static String displayMainMenu(TERenderer ter, int width, int height) {
        setupStdDraw();

        StringBuilder input = new StringBuilder();
        StringBuilder seedDisplay = new StringBuilder();
        boolean waitingForInput = true;
        boolean loadGameMode = false;
        boolean seedInputMode = false;
        boolean replayGameSelected = false;
        boolean loadPreviousWorld = false;

        while (waitingForInput) {
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.setPenColor(StdDraw.WHITE);

            StdDraw.setFont(new Font("Monaco", Font.BOLD, 50));
            StdDraw.text(width / 2.0, height * 0.7, "61B: The Game");

            StdDraw.setFont(new Font("Monaco", Font.BOLD, 20));
            StdDraw.text(width / 2.0, height * 0.55, "New World (N)");
            StdDraw.text(width / 2.0, height * 0.5, "Load (L)");
            StdDraw.text(width / 2.0, height * 0.45, "Replay Game (R)");
            StdDraw.text(width / 2.0, height * 0.4, "Quit (Q)");

            if (seedInputMode) {
                StdDraw.text(width / 2.0, height * 0.35, "Enter Seed: " + seedDisplay);
            }

            StdDraw.show();

            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                input.append(key);

                if (key == 'n' || key == 'N') {
                    seedInputMode = true;
                    seedDisplay.setLength(0);
                } else if (seedInputMode && Character.isDigit(key)) {
                    seedDisplay.append(key);
                } else if (seedInputMode || loadGameMode || (key == 's' || key == 'S')) {
                    waitingForInput = false;
                } else if (!seedInputMode && (key == 'l' || key == 'L')) {
                    loadPreviousWorld = true;
                    waitingForInput = false;
                } else if (!seedInputMode && !loadGameMode && (key == 'r' || key == 'R')) {
                    replayGameSelected = true;
                    waitingForInput = false;
                } else if (!seedInputMode && !loadGameMode && (key == 'q' || key == 'Q')) {
                    System.exit(0);
                }
            }
        }

        if (replayGameSelected) {
            replayGame(ter, width, height);
            return "replay";
        } else if (loadPreviousWorld) {
            World loadedWorld = loadWorld();
            if (loadedWorld != null) {
                return "load";
            }
        } else if (seedInputMode) {
            return input.toString().toLowerCase();
        }

        // Return the input (new game) if none of the other conditions are met
        return input.toString().toLowerCase();
    }



    private static void runGameLoop(TERenderer ter, World world, int width, int height) {
        char prevKey = ' ';

        ter.initialize(width, height);
        StdDraw.enableDoubleBuffering();

        while (true) {
            StdDraw.clear(Color.BLACK);

            ter.renderFrame(world.getTiles());

            handleHUD(world, width, height);

            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();

                if (prevKey == ':' && (key == 'Q' || key == 'q')) {
                    world.saveWorld();
                    System.exit(0);
                }
                if (key != ':') {
                    world.move(key);
                }
                prevKey = key;
            }

            StdDraw.show();
            StdDraw.pause(100);
        }
    }


    private static void handleHUD(World world, int width, int height) {
        String hudText = "Tile: ";
        if (StdDraw.mouseX() >= 0 && StdDraw.mouseX() < width && StdDraw.mouseY() >= 0 && StdDraw.mouseY() < height) {
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            TETile tile = world.getTiles()[mouseX][mouseY];
            hudText += tile.description();
        }
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textLeft(1, height - 1, hudText);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        StdDraw.textRight(width - 1, height - 1, formattedDateTime);

        StdDraw.show();
    }

    public static World loadWorld() {
        try {
            FileInputStream fileIn = new FileInputStream("world_state.txt");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            World world = (World) in.readObject();
            in.close();
            fileIn.close();
            return world;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading world: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    private static void replayGame(TERenderer ter, int width, int height) {
        long originalSeed = loadSeed();
        if (originalSeed == -1) {
            System.out.println("Error: Seed not found for replay.");
            return;
        }
        List<Character> actions = loadActions();

        World replayWorld = new World(width, height, originalSeed);

        replayWorld.addAvatar();

        ter.initialize(width, height);


        for (char action : actions) {
            replayWorld.move(action);
            ter.renderFrame(replayWorld.getTiles());
            handleHUD(replayWorld, width, height);
            StdDraw.show();
            StdDraw.pause(100);
        }

        runGameLoop(ter, replayWorld, width, height);
    }

    private static long loadSeed() {
        try (FileInputStream fileIn = new FileInputStream("seed.txt");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return in.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static List<Character> loadActions() {
        try (FileInputStream fileIn = new FileInputStream("actions.txt");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (List<Character>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static long getOriginalSeed() {
        try {
            FileInputStream fileIn = new FileInputStream("seed.txt");
            Scanner scanner = new Scanner(fileIn);
            long seed = scanner.nextLong();
            scanner.close();
            fileIn.close();
            return seed;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
