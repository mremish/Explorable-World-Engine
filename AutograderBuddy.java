package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.io.*;

public class AutograderBuddy {

    /**
     * Simulates a game, but doesn't render anything or call any StdDraw
     * methods. Instead, returns the world that would result if the input string
     * had been typed on the keyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quit and
     * save. To "quit" in this method, save the game to a file, then just return
     * the TETile[][]. Do not call System.exit(0) in this method.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] getWorldFromInput(String input) {
        input = input.toUpperCase();
        World world = null;



        switch (input.charAt(0)) {
            case 'N':
                world = createNewWorld(input);
                break;
            case 'L':
                world = loadWorldFromSave();
                break;
            default:
                throw new IllegalArgumentException("Input must start with 'N' or 'L'");
        }

        processMoves(input.substring(1), world);

        return world.getTiles();
    }

    private static World createNewWorld(String input) {
        int endSeed = input.indexOf("S");
        if (endSeed == -1) {
            throw new IllegalArgumentException("Invalid input format for creating a new world.");
        }

        long seed = Long.parseLong(input.substring(1, endSeed));
        World world = new World(50, 50, seed);
        world.makeRooms();
        world.makeHallways();
        world.addWallsAround();
        world.addAvatars(20);

        return world;
    }

    private static World loadWorldFromSave() {
        World world = Main.loadWorld();
        if (world == null) {
            throw new RuntimeException("No previous save found.");
        }
        return world;
    }

    private static void processMoves(String moves, World world) {
        for (int i = 0; i < moves.length(); i++) {
            char move = moves.charAt(i);

            if ("WASD".indexOf(move) >= 0) {
                world.move(move);
            } else if (move == ':' && i < moves.length() - 1 && moves.charAt(i + 1) == 'Q') {
                world.saveWorld();
                break;
            }
        }
    }






    /**
     * Used to tell the autograder which tiles are the floor/ground (including
     * any lights/items resting on the ground). Change this
     * method if you add additional tiles.
     */
    public static boolean isGroundTile(TETile t) {
        return t.character() == Tileset.NOTHING.character()
                || t.character() == Tileset.FLOWER.character();
    }

    /**
     * Used to tell the autograder while tiles are the walls/boundaries. Change
     * this method if you add additional tiles.
     */
    public static boolean isBoundaryTile(TETile t) {
        return t.character() == Tileset.SAND.character()
                || t.character() == Tileset.LOCKED_DOOR.character()
                || t.character() == Tileset.UNLOCKED_DOOR.character();
    }
}
