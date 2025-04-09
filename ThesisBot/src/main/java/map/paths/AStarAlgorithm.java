package map.paths;

import map.cells.Cell;
import utils.map.MapCreator;
import map.paths.PathfindingAbstractClass;

import java.awt.*;
import java.util.*;

public class AStarAlgorithm extends PathfindingAbstractClass {
    private final Point[][] parents;

    public AStarAlgorithm(MapCreator mapCreator) {
        super(mapCreator);
        this.parents = new Point[rows][cols];
    }

    @Override
    public void findPath(Cell start, Cell end) {
        if (!isValid(start) || !isValid(end)) {
            throw new IllegalArgumentException("Invalid start or end point");
        }

        for (int i = 0; i < rows; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }

        PriorityQueue<Point> openSet = new PriorityQueue<>(Comparator.comparingInt(p -> distances[p.y][p.x] + heuristic(p, end)));
        distances[start.y][start.x] = 0;
        openSet.add(start);

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!openSet.isEmpty()) {
            Point current = openSet.poll();

            if (current.equals(end)) {
                path = reconstructPath(start, end);
                return;
            }

            int curDist = distances[current.y][current.x];

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];

                if (isInBounds(newX, newY) && matrix[newY][newX] != -1) {
                    int newDist = curDist + 1;

                    if (newDist < distances[newY][newX]) {
                        distances[newY][newX] = newDist;
                        parents[newY][newX] = current;
                        openSet.add(new Point(newX, newY));
                    }
                }
            }
        }

        path = new LinkedList<>();
    }

    private int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Манхэттенское расстояние
    }

    private LinkedList<Point> reconstructPath(Point start, Point end) {
        LinkedList<Point> path = new LinkedList<>();
        Point current = end;

        while (current != null && !current.equals(start)) {
            path.addFirst(current);
            current = parents[current.y][current.x];
        }

        if (current != null) {
            path.addFirst(start);
        }

        return path;
    }
}