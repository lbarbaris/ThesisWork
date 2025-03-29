package map.cells;

import java.awt.*;


//То же самое, что и MapCell, но наследуется от Point
public class Cell extends Point{

    public Cell(int x, int y) {
        super(x / 20, y / 20);
    }

    public Cell(Point point) {
        super((int) (point.getX() / 20), (int) (point.getY() / 20));
    }
}