package game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import network.NetworkHandler;
import movement.MovementManager;
import utils.Constants;
import bullets.BulletManager;
import utils.bullets.Gun;
import utils.map.Block;
import utils.map.MapCreator;
import utils.network.Enemy;
import utils.player.PlayerCameraManager;
import utils.player.Player;

import static utils.Constants.SQUARE_SIZE;

public class GameRenderer {


    private final PlayerCameraManager playerCameraManager;
    private final MapCreator mapCreator;
    private final MovementManager playerMovementManager;
    private final int squareSize;
    private final NetworkHandler networkHandler;
    private final BulletManager bulletManager;
    private final List<ShotTrail> shotTrails = new ArrayList<>();

    public GameRenderer(PlayerCameraManager playerCameraManager,
                        MapCreator mapCreator, MovementManager playerMovementManager,
                        NetworkHandler networkHandler, int squareSize, BulletManager bulletManager) {
        this.networkHandler = networkHandler;
        this.bulletManager = bulletManager;
        this.playerCameraManager = playerCameraManager;
        this.mapCreator = mapCreator;
        this.playerMovementManager = playerMovementManager;
        this.squareSize = squareSize;
    }

    public void render(Graphics g, JComponent component, Player player) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Arial", Font.BOLD, 20));

        g2.translate(-playerCameraManager.getCameraX(), -playerCameraManager.getCameraY());

        renderMap(g2, mapCreator);
        renderMapName(g2, mapCreator);
        renderPlayer(g2);
        renderEnemies(g2);
        renderPredicted(g2);
        renderReal(g2);
        renderShotTrails(g2, player);
        renderReloadingBar(g2, player);

        g2.translate(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());

        renderGunInterface(g2, component, player);
        renderHP(g2, component, player);
    }

    private void renderShotTrails(Graphics2D g2, Player player) {
        // Добавляем новые выстрелы
        if (bulletManager.getOneShot()) {
            Point start = new Point(
                    (int)(player.getX() + squareSize / 2.0),
                    (int)(player.getY() + squareSize / 2.0)
            );
            Point end = bulletManager.getLastHitPoint();
            if (end != null) {
                shotTrails.add(new ShotTrail(start, end, System.currentTimeMillis()));
            }
        }

        // Удаляем старые выстрелы
        shotTrails.removeIf(trail -> !trail.isAlive(System.currentTimeMillis()));

        // Рендерим все активные выстрелы
        for (ShotTrail trail : shotTrails) {
            float progress = trail.getProgress(System.currentTimeMillis());

            // Комбинированный эффект: затемнение + прозрачность
            int colorValue = 255 - (int)(220 * progress); // 220 чтобы не становилось совсем черным
            int alpha = 255 - (int)(255 * progress);

            // Цвет от ярко-желтого к темно-оранжевому с прозрачностью
            Color trailColor = new Color(
                    255,
                    Math.max(50, colorValue),
                    0,
                    alpha
            );

            g2.setStroke(new BasicStroke(4 - (3 * progress))); // Толщина тоже уменьшается
            g2.setColor(trailColor);
            g2.drawLine(trail.getStart().x, trail.getStart().y, trail.getEnd().x, trail.getEnd().y);
        }
    }

    // Остальные методы рендеринга остаются без изменений
    private void renderEnemies(Graphics2D g2) {
        HashMap<String, Enemy> coords = networkHandler.getPlayerCoords();
        long renderTime = System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS;
        HashMap<Enemy, Long> hitTimes = bulletManager.getEnemyHitTimes();

        for (Map.Entry<String, Enemy> entry : coords.entrySet()) {
            Enemy enemy = entry.getValue();
            Point interpPos = enemy.getInterpolatedPosition(renderTime);

            if (!hitTimes.isEmpty() && hitTimes.get(entry.getValue()) != null &&
                    (System.currentTimeMillis() - hitTimes.get(entry.getValue()) < 100)) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(enemy.isBot() ? Color.GREEN : Color.PINK);
            }

            g2.fillRect(interpPos.x, interpPos.y, squareSize, squareSize);
        }
    }

    private void renderPlayer(Graphics2D g2) {
        g2.setColor(Color.BLUE);
        g2.fillRect(playerMovementManager.getX(), playerMovementManager.getY(), squareSize, squareSize);
    }

        private void renderHP(Graphics2D g2, JComponent component, Player player){
            g2.setColor(Color.RED);

            int textWidth = g2.getFontMetrics().stringWidth(player.getHpString());
            int textHeight = g2.getFontMetrics().getHeight();
            int textX = 5 + textWidth; // Используем размеры компонента
            int textY = component.getHeight() - textHeight / 2;

            g2.drawString(player.getHpString(), textX, textY);
        }

        private void renderReloadingBar(Graphics2D g2, Player player){
            Gun gun = player.getGun();
            if (gun.isReloading()) {
                double progress = gun.getReloadProgress();
                int barWidth = squareSize;
                int barHeight = 5;
                int barX = playerMovementManager.getX();
                int barY = playerMovementManager.getY() - 10;

                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(barX, barY, barWidth, barHeight);

                g2.setColor(Color.GREEN);
                g2.fillRect(barX, barY, (int) (barWidth * progress), barHeight);
            }
        }

        private void renderGunInterface(Graphics2D g2, JComponent component, Player player){
            Gun gun = player.getGun();
            int currentAmmo = gun.getCurrentAmmo();
            int maxAmmo = gun.getMagazineSize();

            String ammoText = currentAmmo + " / " + maxAmmo;

            g2.setColor(Color.GREEN);

            int textWidth = g2.getFontMetrics().stringWidth(ammoText);
            int textHeight = g2.getFontMetrics().getHeight();
            int textX = component.getWidth() - textWidth - 10; // Используем размеры компонента
            int textY = component.getHeight() - textHeight / 2;

            g2.drawString(ammoText, textX, textY);
        }

        private void renderMapName(Graphics2D g2, MapCreator mapCreator){
            g2.setColor(Color.GREEN);
            g2.drawString(mapCreator.getMapName(), 10 + playerCameraManager.getCameraX(), 20 + playerCameraManager.getCameraY());
        }

        private void renderMap(Graphics2D g2, MapCreator mapCreator) {
            paintBlock(g2, mapCreator.getWalls());
            paintBlock(g2, mapCreator.getAir());


        }



        private void paintBlock(Graphics2D g2, List<Block> blocks){
            TexturePaint brickTexture = createBrickTexture();
            TexturePaint leavesTexture = createLeavesTexture();
            TexturePaint grassTexture = createGrassTexture();

            for (Block block : blocks) {
                Rectangle bounds = block.getBounds();

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
        private TexturePaint createGrassTexture() {
            int size = SQUARE_SIZE;
            BufferedImage texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = texture.createGraphics();

            // Основной цвет травы
            g2d.setColor(new Color(140, 200, 100));
            g2d.fillRect(0, 0, size, size);

            // Добавляем немного вариативности для натурального вида
            g2d.setColor(new Color(120, 180, 90));
            Random random = new Random(456); // Фиксированное seed для повторяемости

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

        private void renderPredicted(Graphics2D g2){
            for (Point point: networkHandler.getUniquePoints()){
                g2.setColor(Color.YELLOW);
                g2.fillRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);
            }
        }

        private void renderReal(Graphics2D g2) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2)); // Устанавливаем толщину линии контура
            for (Point point : networkHandler.getRealPoints()) {
                g2.drawRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);
            }
        }

        private Point lastShotStartPoint;
        private Point lastShotEndPoint;
        private long lastShotTime;

        private void renderInstantShot(Graphics2D g2, Player player, JComponent component) {
            // Фиксируем новые выстрелы
            if (bulletManager.getOneShot()) {
                lastShotStartPoint = new Point(
                        (int)(player.getX() + squareSize / 2.0),
                        (int)(player.getY() + squareSize / 2.0)
                );
                lastShotEndPoint = bulletManager.getLastHitPoint();
                lastShotTime = System.currentTimeMillis();
            }

            // Проверяем, не прошло ли 3 секунды с момента последнего выстрела
            long timeSinceShot = System.currentTimeMillis() - lastShotTime;
            if (timeSinceShot <= 3000 && lastShotEndPoint != null) {
                g2.setStroke(new BasicStroke(4)); // Толщина линии

                // Рассчитываем прогресс затемнения (0.0 - только выстрелили, 1.0 - прошло 3 секунды)
                float darkenProgress = timeSinceShot / 3000.0f;

                // Начинаем с желтого (255,255,0), затемняем до черного (0,0,0)
                int colorValue = 255 - (int)(255 * darkenProgress);
                Color shotColor = new Color(colorValue, colorValue, 0);

                g2.setColor(shotColor);
                g2.drawLine(
                        lastShotStartPoint.x,
                        lastShotStartPoint.y,
                        lastShotEndPoint.x,
                        lastShotEndPoint.y
                );
            }
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
            int size = SQUARE_SIZE;
            BufferedImage texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = texture.createGraphics();

            // Основной цвет листвы
            g2d.setColor(new Color(50, 120, 50));
            g2d.fillRect(0, 0, size, size);

            // Рисуем узор листьев
            g2d.setColor(new Color(30, 90, 30));
            Random random = new Random(123); // Фиксированное seed для повторяемости

            // Рисуем случайные овалы для имитации листьев
            for (int i = 0; i < 8; i++) {
                int x = random.nextInt(size);
                int y = random.nextInt(size);
                int w = 4 + random.nextInt(6);
                int h = 3 + random.nextInt(5);
                g2d.fillOval(x, y, w, h);
            }

            g2d.dispose();
            return new TexturePaint(texture, new Rectangle(0, 0, size, size));
        }
    }