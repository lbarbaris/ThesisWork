package map.paths;

import map.cells.Cell;

import java.awt.*;
import java.util.LinkedList;

public interface PathfindingInterface {
    void findPath(Cell start, Cell end);
    LinkedList<Point> getPath();
    int[][] getDistances();
}