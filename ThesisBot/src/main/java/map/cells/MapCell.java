package map.cells;

import java.awt.*;


//То же самое, что и Cell, но наследуется от Rectangle
public class MapCell extends Rectangle {
    public MapCell(int x, int y) {
        super(x, y, 20, 20);
    }
}