package utils.map;

import utils.Constants;

import java.awt.*;

public class Block extends Rectangle {
    public Block(int x, int y) {
        super(x, y, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE);
    }
}
