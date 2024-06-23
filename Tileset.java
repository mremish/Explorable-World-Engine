package tileengine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.blue, "nothing");
    public static final TETile FLOWER =new TETile('·', new Color(128, 192, 128), Color.black,
            "floor");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile(' ', Color.gray, Color.gray, "sand");
    public static TETile AVATAR = new TETile
            ('@', Color.white, Color.black, "you");
    public static TETile AVATAR_HEART = new TETile
            ('❤', Color.red, Color.black, "for that special someone");
    public static TETile AVATAR_STAR = new TETile
            ('★', Color.yellow, Color.black, "special edition avatar");
    public static TETile AVATAR_MONEY = new TETile
            ('$', Color.green, Color.black, "you're rich!");
    public static TETile AVATAR_FLOWER = new TETile
            ('❀', Color.pink, Color.black, "you but flower");
}