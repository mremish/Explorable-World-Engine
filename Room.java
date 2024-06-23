package core;

import java.io.Serializable;

public class Room implements Serializable {
    private long serialVersionUID = 1L;
    private int x;
    private int y;
    private int height;
    private int width;


    public Room(int x, int y, int height, int width) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int height() {
        return height;
    }
    public int width() {
        return width;
    }
}
