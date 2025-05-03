package utils.map;

import utils.Constants;

import java.awt.*;
import java.util.ArrayList;

public class MapCreator {
    private final ArrayList<Block> walls;
    private final ArrayList<Block> air;
    public ArrayList<Block> getAir() {
        return air;
    }


    private String mapName;

    public ArrayList<Block> getWalls() {
        return walls;
    }

    public MapCreator(short mapId) {
        air = new ArrayList<>();
        walls = new ArrayList<>();
        switch (mapId) {
            case 1:
                walls.add(new Block(240, 240, 2, false)); // Исправлено с (250, 250)
                walls.add(new Block(140, 140, 2, false)); // Исправлено с (150, 150)
                mapName = "testMap1";
                break;
            case 2:
                mapName = "clearMap";
                addBorders();
                break;
            case 3:
                addBorders();
                for (int x = 100; x <= 800; x += 400) {
                    walls.add(new Block(x, 100,2, false));
                }
                walls.add(new Block(200, 200,2, false)); // Уже кратно 20
                walls.add(new Block(160, 200,2, false)); // Уже кратно 20
                walls.add(new Block(120, 200,2, false)); // Уже кратно 20
                walls.add(new Block(80, 200,2, false)); // Уже кратно 20
                walls.add(new Block(40, 200,2, false)); // Уже кратно 20
                walls.add(new Block(300, 40,2, false));  // Исправлено на (300, 40) (было некратно)
                walls.add(new Block(400, 300,2, false)); // Уже кратно 20
                walls.add(new Block(60, 340,2, false));  // Исправлено с (50, 340)
                mapName = "testMap3";
                break;
            case 4:
                addBorders();
                for (int y = 100; y < Constants.MAP_HEIGHT_SIZE - 100; y += 120) {
                    for (int x = 100; x < Constants.MAP_WIDTH_SIZE - 100; x += 120) {
                        // Левая вертикаль подковы
                        walls.add(new Block(x, y - 20,2, false));
                        walls.add(new Block(x, y,2, false));
                        walls.add(new Block(x, y + 20,2, false));

                        // Нижняя горизонталь подковы
                        walls.add(new Block(x + 20, y + 20,2, false));
                        walls.add(new Block(x + 40, y + 20,2, false));
                        walls.add(new Block(x + 60, y + 20,2, false));

                        // Правая вертикаль подковы
                        walls.add(new Block(x + 60, y,2, false));
                        walls.add(new Block(x + 60, y - 20,2, false));
                        walls.add(new Block(x + 60, y + 20,2, false));
                    }
                }
                mapName = "horseshoePatternMap";
                break;
            case 5:
                addBorders();
                for (int i = 100; i < Constants.MAP_HEIGHT_SIZE - 100; i += 80) {
                    for (int j = 100; j < Constants.MAP_WIDTH_SIZE - 100; j += 80) {

                        // Центр плюса
                        walls.add(new Block(j, i,2, false));

                        // Вертикальная линия
                        walls.add(new Block(j, i - 20,2, false));
                        walls.add(new Block(j, i + 20,2, false));

                        // Горизонтальная линия
                        walls.add(new Block(j - 20, i,2, false));
                        walls.add(new Block(j + 20, i,2, false));
                    }
                }
                mapName = "plusPatternMap";
                break;
        }

        fillEmptySpaceWithGrass();
    }

    private void addBorders() {
        for (int x = 0; x < Constants.MAP_WIDTH_SIZE; x += Constants.SQUARE_SIZE) {
            walls.add(new Block(x, 0,1, false)); // Верхняя граница
            walls.add(new Block(x, Constants.MAP_HEIGHT_SIZE - Constants.SQUARE_SIZE,1, false)); // Нижняя граница
        }
        for (int y = 0; y < Constants.MAP_HEIGHT_SIZE; y += Constants.SQUARE_SIZE) {
            walls.add(new Block(0, y,1, false)); // Левая граница
            walls.add(new Block(Constants.MAP_WIDTH_SIZE - Constants.SQUARE_SIZE, y,1, false)); // Правая граница
        }
    }

    public String getMapName() {
        return mapName;
    }

    public int[][] generateMapMatrix() {
        int rows = Constants.MAP_HEIGHT_SIZE / Constants.SQUARE_SIZE;
        int cols = Constants.MAP_WIDTH_SIZE / Constants.SQUARE_SIZE;
        int[][] matrix = new int[rows][cols];

        for (Block block : walls) {
            int x = block.x / Constants.SQUARE_SIZE;
            int y = block.y / Constants.SQUARE_SIZE;
            if (x >= 0 && x < cols && y >= 0 && y < rows) {
                matrix[y][x] = -1;
            }
        }
        return matrix;
    }

    private void fillEmptySpaceWithGrass() {
        int[][] matrix = generateMapMatrix();
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (matrix[y][x] == 0) { // Если клетка пустая
                    int worldX = x * Constants.SQUARE_SIZE;
                    int worldY = y * Constants.SQUARE_SIZE;
                    air.add(new Block(worldX, worldY, 3, true)); // Добавляем проходимый блок травы
                }
            }
        }
    }



    public int getWidthCellsSize(){
        return Constants.MAP_WIDTH_SIZE / Constants.SQUARE_SIZE;
    }
}
