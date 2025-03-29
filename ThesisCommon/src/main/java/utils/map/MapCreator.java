package utils.map;

import utils.Constants;

import java.util.ArrayList;

public class MapCreator {
    private final ArrayList<Block> map;
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
                map.add(new Block(200, 200)); // Уже кратно 20
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
        for (int x = 0; x < Constants.MAP_WIDTH_SIZE; x += Constants.SQUARE_SIZE) {
            map.add(new Block(x, 0)); // Верхняя граница
            map.add(new Block(x, Constants.MAP_HEIGHT_SIZE - Constants.SQUARE_SIZE)); // Нижняя граница
        }
        for (int y = 0; y < Constants.MAP_HEIGHT_SIZE; y += Constants.SQUARE_SIZE) {
            map.add(new Block(0, y)); // Левая граница
            map.add(new Block(Constants.MAP_WIDTH_SIZE - Constants.SQUARE_SIZE, y)); // Правая граница
        }
    }

    public String getMapName() {
        return mapName;
    }

    public int[][] generateMapMatrix() {
        int rows = Constants.MAP_HEIGHT_SIZE / Constants.SQUARE_SIZE;
        int cols = Constants.MAP_WIDTH_SIZE / Constants.SQUARE_SIZE;
        int[][] matrix = new int[rows][cols];

        for (Block block : map) {
            int x = block.x / Constants.SQUARE_SIZE;
            int y = block.y / Constants.SQUARE_SIZE;
            if (x >= 0 && x < cols && y >= 0 && y < rows) {
                matrix[y][x] = -1;
            }
        }
        return matrix;
    }

    public int getWidthCellsSize(){
        return Constants.MAP_WIDTH_SIZE / Constants.SQUARE_SIZE;
    }
}
