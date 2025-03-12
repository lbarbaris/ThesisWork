package map;

import map.cells.MapCell;

import java.util.ArrayList;

public class MapCreator {
    private final ArrayList<MapCell> map;
    private final int mapWidth = 980;
    private final int mapHeight = 980;
    private String mapName;

    public ArrayList<MapCell> getMap() {
        return map;
    }

    public MapCreator(short mapId) {
        map = new ArrayList<>();
        switch (mapId) {
            case 1:
                map.add(new MapCell(240, 240));
                map.add(new MapCell(140, 140));
                mapName = "testMap1";
                break;
            case 2:
                mapName = "testMap2";
                break;
            case 3:
                addBorders();
                for (int x = 100; x <= 800; x += 400) {
                    map.add(new MapCell(x, 100));
                }
                map.add(new MapCell(200, 200));
                map.add(new MapCell(160, 200));
                map.add(new MapCell(120, 200));
                map.add(new MapCell(80, 200));
                map.add(new MapCell(40, 200));
                map.add(new MapCell(300, 40));
                map.add(new MapCell(400, 300));
                map.add(new MapCell(60, 340));
                mapName = "testMap3";
                break;
            case 4:
                addBorders();
                generateDenseBlocks();
                mapName = "testMap4";
                break;
        }
    }

    private void addBorders() {
        for (int x = 0; x < mapWidth; x += 20) {
            map.add(new MapCell(x, 0));
            map.add(new MapCell(x, mapHeight - 20));
        }
        for (int y = 0; y < mapHeight; y += 20) {
            map.add(new MapCell(0, y));
            map.add(new MapCell(mapWidth - 20, y));
        }
    }

    private void generateDenseBlocks() {
        for (int x = 80; x < mapWidth - 40; x += 20) {
            for (int y = 80; y < mapHeight - 40; y += 20) {
                if (Math.random() > 0.3 && x != 220 && y != 220) {
                    map.add(new MapCell(x, y));
                }
            }
        }
    }

    public String getMapName() {
        return mapName;
    }

    public int[][] generateMapMatrix() {
        int rows = mapHeight / 20;
        int cols = mapWidth / 20;
        int[][] matrix = new int[rows][cols];

        for (MapCell mapCell : map) {
            int x = mapCell.x / 20;
            int y = mapCell.y / 20;
            if (x >= 0 && x < cols && y >= 0 && y < rows) {
                matrix[y][x] = -1;
            }
        }
        return matrix;
    }
}