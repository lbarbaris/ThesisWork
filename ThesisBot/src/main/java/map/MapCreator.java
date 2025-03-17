package map;

import java.awt.*;
import java.util.ArrayList;

import static utils.Constants.SQUARE_SIZE;

public class MapCreator {
    private final ArrayList<Block> map;
    private final int mapWidth = 980;
    private final int mapHeight = 980;
    private String mapName;

    public ArrayList<Block> getMap() {
        return map;
    }

    public MapCreator(short mapId) {
        map = new ArrayList<>();
        switch (mapId) {
            case 1:
                map.add(new Block(240, 240)); // Исправлено с (250, 250)
                map.add(new Block(140, 140)); // Исправлено с (150, 150)
                mapName = "testMap1";
                break;
            case 2:
                mapName = "testMap2";
                break;
            case 3:
                addBorders();
                for (int x = 100; x <= 800; x += 400) {
                    map.add(new Block(x, 100));
                }
                map.add(new Block(160, 200)); // Уже кратно 20
                map.add(new Block(120, 200)); // Уже кратно 20
                map.add(new Block(80, 200)); // Уже кратно 20
                map.add(new Block(40, 200)); // Уже кратно 20
                map.add(new Block(300, 40));  // Исправлено на (300, 40) (было некратно)
                map.add(new Block(400, 300)); // Уже кратно 20
                map.add(new Block(60, 340));  // Исправлено с (50, 340)
                mapName = "testMap3";
                break;
        }
    }

    private void addBorders() {
        for (int x = 0; x < mapWidth; x += SQUARE_SIZE) {
            map.add(new Block(x, 0)); // Верхняя граница
            map.add(new Block(x, mapHeight - SQUARE_SIZE)); // Нижняя граница
        }
        for (int y = 0; y < mapHeight; y += SQUARE_SIZE) {
            map.add(new Block(0, y)); // Левая граница
            map.add(new Block(mapWidth - SQUARE_SIZE, y)); // Правая граница
        }
    }

    public String getMapName() {
        return mapName;
    }

    public int[][] generateMapMatrix() {
        int rows = mapHeight / SQUARE_SIZE;
        int cols = mapWidth / SQUARE_SIZE;
        int[][] matrix = new int[rows][cols];

        for (Block block : map) {
            int x = block.x / SQUARE_SIZE;
            int y = block.y / SQUARE_SIZE;
            if (x >= 0 && x < cols && y >= 0 && y < rows) {
                matrix[y][x] = -1;
            }
        }
        return matrix;
    }
}