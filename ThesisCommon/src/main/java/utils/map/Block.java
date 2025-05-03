package utils.map;

import utils.Constants;

import java.awt.*;

public class Block extends Rectangle {
    private final int id;


    public Block(int x, int y, int id, boolean isAir) {
        super(x, y, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE);
        this.id = id;
        this.isAir = isAir;
    }

    public boolean isAir() {
        return isAir;
    }

    private final boolean isAir;
    public int getId() {
        return id;
    }
}
