package map.cells;

import utils.Constants;

import java.awt.*;


//То же самое, что и MapCell, но наследуется от Point
public class Cell extends Point{

    public Cell(int x, int y) {
        super(x / Constants.SQUARE_SIZE, y / Constants.SQUARE_SIZE);
    }

    public Cell(Point point) {
        super((int) (point.getX() / Constants.SQUARE_SIZE), (int) (point.getY() / Constants.SQUARE_SIZE));
    }
}