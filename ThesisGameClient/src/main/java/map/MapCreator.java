package map;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class MapCreator {
    private final ArrayList<Shape> map;
    private final int mapWidth = 1500; // Ширина карты
    private final int mapHeight = 1200; // Высота карты

    private String mapName;

    public ArrayList<Shape> getMap() {
        return map;
    }

    public MapCreator(short mapId) {
        map = new ArrayList<>();
        switch (mapId){
            case 1:
                map.add(new Rectangle(250,250,50,50));
                map.add(new Rectangle(150,150,50,50));
                mapName = "testMap1";
                break;
            case 2:
                mapName = "testMap2";
                break;
            case 3:
                addBorders();
                map.add(new Rectangle(100, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(200, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(300, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(400, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(500, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(600, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(700, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(800, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(900, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1000, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1100, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1200, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1300, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1400, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1500, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1600, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1700, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1800, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(1900, 100, 50, 50)); // Прямоугольник в верхнем левом углу
                map.add(new Rectangle(200, 200, 100, 50)); // Более широкий прямоугольник
                map.add(new Rectangle(300, 50, 50, 100)); // Вертикальный прямоугольник
                map.add(new Rectangle(400, 300, 75, 75)); // Квадрат ближе к центру
                map.add(new Rectangle(50, 350, 30, 60)); // Маленький узкий прямоугольник
                mapName = "testMap3";
                break;
        }
    }

    private void addBorders() {
        // Верхняя граница
        map.add(new Rectangle(0, 0, mapWidth, 10));
        // Нижняя граница
        map.add(new Rectangle(0, mapHeight - 10, mapWidth, 10));
        // Левая граница
        map.add(new Rectangle(0, 0, 10, mapHeight));
        // Правая граница
        map.add(new Rectangle(mapWidth - 10, 0, 10, mapHeight));
    }
    public String getMapName() {
        return mapName;
    }

}
