package map.paths;

import map.cells.Cell;
import utils.map.MapCreator;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class WaveAlgorithm extends PathfindingAbstractClass {
    public WaveAlgorithm(MapCreator mapCreator) {
        super(mapCreator);
    }

    @Override
    public void findPath(Cell start, Cell end) {
        if (!isValid(start) || !isValid(end)) {
            throw new IllegalArgumentException("Invalid start or end point");
        }

        for (int i = 0; i < rows; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        distances[start.y][start.x] = 0;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int curDist = distances[current.y][current.x];

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];

                if (isInBounds(newX, newY) && matrix[newY][newX] != -1 && distances[newY][newX] == Integer.MAX_VALUE) {
                    distances[newY][newX] = curDist + 1;
                    queue.add(new Point(newX, newY));
                }
            }
        }

        path = reconstructPath(start, end);
    }

    private LinkedList<Point> reconstructPath(Point start, Point end) {
        LinkedList<Point> path = new LinkedList<>();
        if (distances[end.y][end.x] == Integer.MAX_VALUE) {
            return path;
        }

        Point current = end;
        path.addFirst(current);

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!current.equals(start)) {
            int curDist = distances[current.y][current.x];

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];

                if (isInBounds(newX, newY) && distances[newY][newX] == curDist - 1) {
                    current = new Point(newX, newY);
                    path.addFirst(current);
                    break;
                }
            }
        }

        return path;
    }
}