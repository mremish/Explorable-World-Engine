package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class World implements Serializable {
    private transient TETile[][] world;
    private int width;
    private int height;
    private int numRooms;
    private Random r;
    private ArrayList<Room> rooms;
    private int[] player;
    private List<int[]> avatars;
    private List<Character> actions;
    private long seed;
    private int[] controlledAvatar;

    public World(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        r = new Random(seed);
        rooms = new ArrayList<>();
        avatars = new ArrayList<>();
        actions = new ArrayList<>();
        player = new int[2];

        initWorld();
        makeRooms();
        makeHallways();
        addWallsAround();
        addAvatars(20);
    }

    private void initWorld() {
        world = new TETile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }
    public void makeRooms() {
        int maxRoom = 20;
        int minRoom = 10;
        numRooms = r.nextInt(maxRoom - minRoom) + minRoom;
        for (int i = 0; i < numRooms; i++) {
            int maxSize = 10;
            int rX = r.nextInt(width - maxSize - 3) + 2;
            int rY = r.nextInt(height - maxSize - 3) + 2;
            int minSize = 4;
            int rH = r.nextInt(maxSize - minSize - 1) + minSize + 1;
            int rW = r.nextInt(maxSize - minSize - 1) + minSize + 1;
            Room room = new Room(rX, rY, rH, rW);
            genRoom(room);
            rooms.add(room);
        }
    }
    public void genRoom(Room room) {
        for (int x = room.x(); x < room.x() + room.width(); x++) {
            for (int y = room.y(); y < room.y() + room.height(); y++) {
                world[x][y] = Tileset.FLOWER;
            }
        }
    }

    public TETile[][] getTiles() {
        return world;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public void makeHallways() {
        for (int i = 0; i < rooms.size() - 1; i++) {
            int xCoord1 = rooms.get(i).x();
            int yCoord1 = rooms.get(i).y();
            int height1 = rooms.get(i).height();
            int width1 = rooms.get(i).width();

            //random point within this room
            int currx = r.nextInt(width1 - 2) + xCoord1 + 1;
            int curry = r.nextInt(height1 - 2) + yCoord1 + 1;

            int xCoord2 = rooms.get(i + 1).x();
            int yCoord2 = rooms.get(i + 1).y();
            int height2 = rooms.get(i + 1).height();
            int width2 = rooms.get(i + 1).width();

            int nextx = r.nextInt(width2 - 2) + xCoord2 + 1;
            int nexty = r.nextInt(height2 - 2) + yCoord2 + 1;

            while (currx != nextx || curry != nexty) {
                if (currx < nextx) {
                    currx++;
                } else if (currx > nextx) {
                    currx--;
                } else if (curry < nexty) {
                    curry++;
                } else if (curry > nexty) {
                    curry--;
                }

                if (isValid(currx, curry)) {
                    world[currx][curry] = Tileset.FLOWER;
                }
            }
        }
    }

    public boolean isValid(int x, int y) {
        return (x >= 0 && y >= 0 && x < width && y < height);
    }

    public void addWallsAround() {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (world[x][y] == Tileset.FLOWER) {
                    // check the 8 surrounding tiles
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            // don't do center tile duh
                            if (i == 0 && j == 0) {
                                continue;
                            }
                            int checkX = x + i;
                            int checkY = y + j;

                            if (isValid(checkX, checkY) && world[checkX][checkY] == Tileset.NOTHING) {
                                world[checkX][checkY] = Tileset.SAND;
                            }
                        }
                    }
                }
            }
        }
    }

    public void setControlledAvatar(int[] controlledAvatar) {
        this.controlledAvatar = controlledAvatar;
    }

    public void addAvatar() {
        int flowerTiles = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (world[x][y] == Tileset.FLOWER) {
                    flowerTiles++;
                }
            }
        }

        boolean avatarPlaced = false;

        while (!avatarPlaced) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);

            if (world[x][y] == Tileset.FLOWER && isAccessible(x, y)) {
                world[x][y] = Tileset.AVATAR;
                player[0] = x;
                player[1] = y;
                avatarPlaced = true;
            }
        }
    }

    private boolean isAccessible(int x, int y) {
        return world[x][y] != Tileset.SAND;
    }

    public void addAvatars(int numAvatars) {
        for (int i = 0; i < numAvatars; i++) {
            boolean avatarPlaced = false;
            while (!avatarPlaced) {
                int x = r.nextInt(width);
                int y = r.nextInt(height);

                if (world[x][y] == Tileset.FLOWER) {
                    world[x][y] = Tileset.AVATAR;
                    int[] avatarPosition = {x, y};
                    avatars.add(avatarPosition);
                    avatarPlaced = true;
                }
            }
        }
    }

    public void move(char key) {
        int[] delta = getDelta(key);
        int newX = player[0] + delta[0];
        int newY = player[1] + delta[1];

        if (isValidMove(newX, newY)) {
            for (int[] avatarPosition : avatars) {
                if (newX == avatarPosition[0] && newY == avatarPosition[1]) {
                    // Eat the avatar
                    avatars.remove(avatarPosition);
                    break;
                }
            }

            world[player[0]][player[1]] = Tileset.FLOWER;
            player[0] = newX;
            player[1] = newY;
            world[newX][newY] = Tileset.AVATAR;

            recordAction(key);
        }
    }

    private int[] getDelta(char key) {
        int[] delta = {0, 0};

        if (key == 'w') {
            delta[1] = 1;
        } else if (key == 'a') {
            delta[0] = -1;
        } else if (key == 's') {
            delta[1] = -1;
        } else if (key == 'd') {
            delta[0] = 1;
        }

        return delta;
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && world[x][y] != Tileset.SAND;
    }

    public boolean handleQuit(char key) {
        if (key == 'Q' || key == 'q') {
            saveWorld();
            System.out.println("Game saved and quitting.");
            System.exit(0);
            return true;
        }
        return false;
    }

    public void saveWorld() {
        try (FileOutputStream fileOut = new FileOutputStream("world_state.txt");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveActions();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(width);
        oos.writeInt(height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                oos.writeChar(world[i][j].character());
                oos.writeUTF(world[i][j].description());
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        int w = ois.readInt();
        int h = ois.readInt();
        this.width = w;
        this.height = h;
        this.world = new TETile[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                char c = ois.readChar();
                String description = ois.readUTF();
                world[i][j] = getTETileFromCharAndDescription(c, description);
            }
        }
    }


    private TETile getTETileFromCharAndDescription(char c, String description) {
        switch (c) {
            case '@':
                if ("you".equals(description)) {
                    return Tileset.AVATAR;
                }
                break;
            case '·':
                if ("floor".equals(description)) {
                    return Tileset.FLOWER;
                }
                break;
            case ' ':
                if ("nothing".equals(description)) {
                    return Tileset.NOTHING;
                }
                if ("sand".equals(description)) {
                    return Tileset.SAND;
                }
                break;
            default:
                return Tileset.NOTHING;
        }
        return Tileset.NOTHING; //fallback if nothing found
    }

    public void recordAction(char action) {
        actions.add(action);
    }

    public List<Character> getRecordedActions() {
        return actions;
    }

    public void saveActions() {
        try (FileOutputStream fileOut = new FileOutputStream("actions.txt");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(actions);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Character> loadActions() {
        try (FileInputStream fileIn = new FileInputStream("actions.txt");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (List<Character>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveSeed() {
        try (FileOutputStream fileOut = new FileOutputStream("seed.txt");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeLong(seed);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
