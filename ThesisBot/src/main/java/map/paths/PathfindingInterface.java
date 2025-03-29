package map.paths;

import java.awt.*;
import java.util.LinkedList;

public interface PathfindingInterface {
    void findPath(Point start, Point end);
    LinkedList<Point> getPath();
    int[][] getDistances();
}