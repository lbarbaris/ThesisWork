package map.paths;

import java.awt.*;

public class Cell extends Point {

    // Конструктор с координатами x и y
    public Cell(int x, int y) {
        super(x / 20, y / 20);
    }

    // Конструктор с объектом Point
    public Cell(Point point) {
        super((int) (point.getX() / 20), (int) (point.getY() / 20));
    }
}
