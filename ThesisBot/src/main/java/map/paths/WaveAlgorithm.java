package map;

import java.awt.*;
import java.util.*;

public class WaveAlgorithm {
    private final int[][] matrix;
    private final int rows;
    private final int cols;
    private final int[][] distances;
    private LinkedList<Point> path;

    public WaveAlgorithm(MapCreator mapCreator) {
        this.matrix = mapCreator.generateMapMatrix();
        this.rows = matrix.length;
        this.cols = matrix[0].length;
        this.distances = new int[rows][cols];
        this.path = new LinkedList<Point>();
    }

    public void findPath(Point start, Point end) {
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
            return path; // Путь не найден
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

    private boolean isValid(Point p) {
        return isInBounds(p.x, p.y) && matrix[p.y][p.x] != -1;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public int[][] getDistances(){
        return distances;
    }

    public LinkedList<Point> getPath(){
        return path;
    }
}
