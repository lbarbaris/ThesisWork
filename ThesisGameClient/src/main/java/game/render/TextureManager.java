package game.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static utils.Constants.SQUARE_SIZE;

/**
 * Manages creation and caching of all textures in the game.
 * Implements lazy loading for textures.
 */
public class TextureManager {
    private static final int BRICK_ID = 1;
    private static final int LEAVES_ID = 2;
    private static final int GRASS_ID = 3;

    private final Map<Integer, TexturePaint> textureCache;
    private final Random random;

    public TextureManager() {
        this.textureCache = new HashMap<>();
        this.random = new Random();

        // Preload basic textures
        preloadTextures();
    }

    private void preloadTextures() {
        // Можно загрузить основные текстуры сразу или делать lazy loading
        getTexture(BRICK_ID);
        getTexture(LEAVES_ID);
        getTexture(GRASS_ID);
    }

    public TexturePaint getTexture(int blockId) {
        // Если текстура уже в кэше, возвращаем её
        if (textureCache.containsKey(blockId)) {
            return textureCache.get(blockId);
        }

        // Создаем новую текстуру и кэшируем
        TexturePaint texture = createTexture(blockId);
        textureCache.put(blockId, texture);
        return texture;
    }

    private TexturePaint createTexture(int blockId) {
        return switch (blockId) {
            case BRICK_ID -> createBrickTexture();
            case LEAVES_ID -> createLeavesTexture();
            case GRASS_ID -> createGrassTexture();
            default -> createDefaultTexture();
        };
    }

    private TexturePaint createBrickTexture() {
        BufferedImage texture = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = texture.createGraphics();

        // Основной цвет кирпича
        g2d.setColor(new Color(150, 70, 50));
        g2d.fillRect(0, 0, SQUARE_SIZE, SQUARE_SIZE);

        // "Раствор" между кирпичами
        g2d.setColor(new Color(100, 50, 40));

        // Горизонтальные линии
        g2d.fillRect(0, 0, SQUARE_SIZE, 2);
        g2d.fillRect(0, SQUARE_SIZE/2, SQUARE_SIZE, 2);

        // Вертикальные линии (со смещением в шахматном порядке)
        g2d.fillRect(0, 0, 2, SQUARE_SIZE);
        g2d.fillRect(SQUARE_SIZE/2, 0, 2, SQUARE_SIZE/2);
        g2d.fillRect(SQUARE_SIZE/2, SQUARE_SIZE/2, 2, SQUARE_SIZE/2);

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, SQUARE_SIZE, SQUARE_SIZE));
    }

    private TexturePaint createLeavesTexture() {
        BufferedImage texture = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = texture.createGraphics();

        // Основной цвет листвы
        g2d.setColor(new Color(50, 120, 50));
        g2d.fillRect(0, 0, SQUARE_SIZE, SQUARE_SIZE);

        // Узор листьев (с фиксированным seed для детерминизма)
        Random patternRandom = new Random(123);
        g2d.setColor(new Color(30, 90, 30));

        // Рисуем случайные овалы для имитации листьев
        for (int i = 0; i < 8; i++) {
            int x = patternRandom.nextInt(SQUARE_SIZE);
            int y = patternRandom.nextInt(SQUARE_SIZE);
            int w = 4 + patternRandom.nextInt(6);
            int h = 3 + patternRandom.nextInt(5);
            g2d.fillOval(x, y, w, h);
        }

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, SQUARE_SIZE, SQUARE_SIZE));
    }

    private TexturePaint createGrassTexture() {
        BufferedImage texture = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = texture.createGraphics();

        // Основной цвет травы
        g2d.setColor(new Color(140, 200, 100));
        g2d.fillRect(0, 0, SQUARE_SIZE, SQUARE_SIZE);

        // Вариации цвета (с фиксированным seed)
        Random grassRandom = new Random(456);
        g2d.setColor(new Color(120, 180, 90));

        // Текстура травы
        for (int i = 0; i < 15; i++) {
            int x = grassRandom.nextInt(SQUARE_SIZE);
            int y = grassRandom.nextInt(SQUARE_SIZE);
            g2d.fillRect(x, y, 1, 1);
        }

        // Травинки
        g2d.setColor(new Color(100, 160, 80));
        for (int i = 0; i < 5; i++) {
            int x = grassRandom.nextInt(SQUARE_SIZE);
            int y = grassRandom.nextInt(SQUARE_SIZE);
            int h = 2 + grassRandom.nextInt(3);
            g2d.fillRect(x, y, 1, h);
        }

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, SQUARE_SIZE, SQUARE_SIZE));
    }

    private TexturePaint createDefaultTexture() {
        BufferedImage texture = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = texture.createGraphics();

        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, SQUARE_SIZE, SQUARE_SIZE);

        g2d.dispose();
        return new TexturePaint(texture, new Rectangle(0, 0, SQUARE_SIZE, SQUARE_SIZE));
    }

    public void clearCache() {
        textureCache.clear();
    }
}
