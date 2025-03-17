package map;

import java.awt.*;

import static utils.Constants.SQUARE_SIZE;

public class Block extends Rectangle {
    public Block(int x, int y) {
        super(x, y, SQUARE_SIZE, SQUARE_SIZE);
    }
}
