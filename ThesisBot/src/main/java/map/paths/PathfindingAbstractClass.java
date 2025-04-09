package map.paths;

import utils.map.MapCreator;

import java.awt.*;
import java.util.LinkedList;

public abstract class PathfindingAbstractClass implements PathfindingInterface {
    protected final int[][] matrix;
    protected final int rows;
    protected final int cols;
    protected final int[][] distances;
    protected LinkedList<Point> path;
    protected MapCreator mapCreator;

    public PathfindingAbstractClass(MapCreator mapCreator) {
        this.mapCreator = mapCreator;
        this.matrix = mapCreator.generateMapMatrix();
        this.rows = matrix.length;
        this.cols = matrix[0].length;
        this.distances = new int[rows][cols];
        this.path = new LinkedList<>();
    }

    protected boolean isValid(Point p) {
        return isInBounds(p.x, p.y) && matrix[p.y][p.x] != -1;
    }

    protected boolean isInBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public int[][] getDistances() {
        return distances;
    }

    public LinkedList<Point> getPath() {
        return path;
    }
}
