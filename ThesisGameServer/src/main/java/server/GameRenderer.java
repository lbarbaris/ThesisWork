package server;

import utils.Constants;
import utils.bullets.Bullet;
import utils.map.Block;
import utils.map.MapCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static utils.Constants.SQUARE_SIZE;


public class GameRenderer extends JPanel {
    private final MapCreator mapCreator;
    private final Map<String, ServerEnemy> playerStates;
    private List<Bullet> bullets;

    public GameRenderer(MapCreator mapCreator, Map<String, ServerEnemy> playerStates, List<Bullet> bullets) {
        this.bullets = bullets;
        this.mapCreator = mapCreator;
        this.playerStates = playerStates;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        renderMap(g2, mapCreator);

        renderPlayerStates(g2);

        renderAdvancedBullets(g2);
    }

    private void renderAdvancedBullets(Graphics2D g2){
        for (Bullet bullet : bullets) {
            Point pos = bullet.getPosition();
            g2.setColor(Color.RED);
            g2.fillOval(pos.x, pos.y, 4, 4);
        }
    }

    private void renderMap(Graphics2D g2, MapCreator mapCreator) {
        // Создаем текстуры для разных типов блоков
        var brickTexture = createBrickTexture();
        var leavesTexture = createLeavesTexture();
        var grassTexture = createGrassTexture(); // Новая текстура для травы

        for (var block : mapCreator.getWalls()) {
            var bounds = block.getBounds();

            // Выбираем текстуру в зависимости от ID блока
            switch (block.getId()) {
                case 1: // Кирпич
                    g2.setPaint(brickTexture);
                    break;
                case 2: // Листва
                    g2.setPaint(leavesTexture);
                    break;
                case 3: // Трава (новый тип)
                    g2.setPaint(grassTexture);
                    break;
                default:
                    g2.setColor(Color.GRAY);
                    g2.setPaint(null);
            }

            g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    // Создает текстуру светлой травы 20x20
    private TexturePaint createGrassTexture() {
        var size = SQUARE_SIZE;
        var texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        var g2d = texture.createGraphics();

        // Основной цвет травы
        g2d.setColor(new Color(140, 200, 100));
        g2d.fillRect(0, 0, size, size);

        // Добавляем немного вариативности для натурального вида
        g2d.setColor(new Color(120, 180, 90));
        var random = new Random(456); // Фиксированное seed для повторяемости

        // Рисуем случайные точки для текстуры
        for (int i = 0; i < 15; i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            g2d.fillRect(x, y, 1, 1);
        }

        // Добавляем несколько "травинок"
        g2d.setColor(new Color(100, 160, 80));
        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            int h = 2 + random.nextInt(3);
            g2d.fillRect(x, y, 1, h);
        }

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, size, size));
    }

    private void renderPlayerStates(Graphics2D g2){
        long renderTime = getRenderTimestamp();

        for (ServerEnemy state : playerStates.values()) {
            Point interpolated = state.getInterpolatedPosition(renderTime);

            if (!state.isBot()){
                g2.setColor(Color.BLUE);
            } else {
                g2.setColor(Color.GREEN);
            }

            g2.fillRect(interpolated.x, interpolated.y, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE);
        }
    }

    private long getRenderTimestamp() {
        return System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS;
    }

    // Создает текстуру кирпича 20x20
    private TexturePaint createBrickTexture() {
        int size = SQUARE_SIZE;
        BufferedImage texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = texture.createGraphics();

        // Основной цвет кирпича
        g2d.setColor(new Color(150, 70, 50));
        g2d.fillRect(0, 0, size, size);

        // "Раствор" между кирпичами
        g2d.setColor(new Color(100, 50, 40));
        // Горизонтальные линии
        g2d.fillRect(0, 0, size, 2);
        g2d.fillRect(0, size/2, size, 2);
        // Вертикальные линии (со смещением в шахматном порядке)
        g2d.fillRect(0, 0, 2, size);
        g2d.fillRect(size/2, 0, 2, size/2);
        g2d.fillRect(size/2, size/2, 2, size/2);

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, size, size));
    }

    // Создает текстуру листвы 20x20
    private TexturePaint createLeavesTexture() {
        var size = SQUARE_SIZE;
        var texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        var g2d = texture.createGraphics();

        // Основной цвет листвы
        g2d.setColor(new Color(50, 120, 50));
        g2d.fillRect(0, 0, size, size);

        // Рисуем узор листьев
        g2d.setColor(new Color(30, 90, 30));
        var random = new Random(123); // Фиксированное seed для повторяемости

        // Рисуем случайные овалы для имитации листьев
        for (int i = 0; i < 8; i++) {
            var x = random.nextInt(size);
            var y = random.nextInt(size);
            var w = 4 + random.nextInt(6);
            var h = 3 + random.nextInt(5);
            g2d.fillOval(x, y, w, h);
        }

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, size, size));
    }
}

