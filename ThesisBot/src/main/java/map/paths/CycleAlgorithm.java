package map.paths;

import map.cells.Cell;
import utils.Constants;
import utils.map.MapCreator;
import java.awt.*;
import java.util.LinkedList;

public class CycleAlgorithm extends PathfindingAbstractClass implements PathfindingInterface{
    private final LinkedList<Cell> cyclePath;
    private int currentPathIndex = 0;

    public CycleAlgorithm(MapCreator mapCreator) {
        super(mapCreator);
        this.cyclePath = new LinkedList<>();
        initializeCyclePath();
    }

    private void initializeCyclePath() {
        // Инициализация циклического пути, аналогично GameController
        int startX = Constants.BOT_SPAWN_X / Constants.SQUARE_SIZE;
        int startY = Constants.BOT_SPAWN_Y / Constants.SQUARE_SIZE;

        int x = startX;
        int y = startY;

        // Генерация пути в форме "забора"
        for (int i = 0; i < 10; i++) {
            x += 2;
            cyclePath.add(new Cell(x, y));
            y += (i % 2 == 0 ? -1 : 1);
            cyclePath.add(new Cell(x, y));
        }

        for (int i = 0; i < 10; i++) {
            y -= 2;
            cyclePath.add(new Cell(x, y));
            x += (i % 2 == 0 ? -1 : 1);
            cyclePath.add(new Cell(x, y));
        }

        for (int i = 0; i < 10; i++) {
            x -= 2;
            cyclePath.add(new Cell(x, y));
            y += (i % 2 == 0 ? 1 : -1);
            cyclePath.add(new Cell(x, y));
        }

        for (int i = 0; i < 10; i++) {
            y += 2;
            cyclePath.add(new Cell(x, y));
            x += (i % 2 == 0 ? 1 : -1);
            cyclePath.add(new Cell(x, y));
        }
    }

    @Override
    public void findPath(Cell start, Cell end) {
        // В данном алгоритме параметр end не используется, так как путь циклический

        // Проверяем, достигли ли текущей целевой точки
        Cell currentTarget = cyclePath.get(currentPathIndex);
        if (start.distance(currentTarget) < 1.5) { // Пороговое расстояние в клетках
            currentPathIndex = (currentPathIndex + 1) % cyclePath.size();
            currentTarget = cyclePath.get(currentPathIndex);
        }

        // Используем волновой алгоритм для поиска пути до следующей точки цикла
        WaveAlgorithm waveAlgorithm = new WaveAlgorithm(mapCreator);
        waveAlgorithm.findPath(start, currentTarget);
        this.path = waveAlgorithm.getPath();
    }

    @Override
    public LinkedList<Point> getPath() {
        return path;
    }

    @Override
    public int[][] getDistances() {
        // Возвращаем пустую матрицу, так как этот алгоритм не вычисляет расстояния
        return new int[rows][cols];
    }
}
