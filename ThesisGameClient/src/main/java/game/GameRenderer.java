package game;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import game.render.TextureManager;
import network.NetworkHandler;
import movement.MovementManager;
import game.render.AirParticle;
import game.render.ShotTrail;
import utils.Constants;
import bullets.BulletManager;
import utils.bullets.Gun;
import utils.bullets.Particle;
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
    private TextureManager textureManager;
    private final int squareSize;
    private final NetworkHandler networkHandler;
    private final BulletManager bulletManager;
    private final List<ShotTrail> shotTrails = new ArrayList<>();
    private final List<AirParticle> airParticles = new ArrayList<>();
    private long lastParticleSpawnTime;

    public GameRenderer(PlayerCameraManager playerCameraManager,
                        MapCreator mapCreator, MovementManager playerMovementManager,
                        NetworkHandler networkHandler, int squareSize, BulletManager bulletManager) {
        this.networkHandler = networkHandler;
        this.bulletManager = bulletManager;
        this.playerCameraManager = playerCameraManager;
        this.mapCreator = mapCreator;
        this.playerMovementManager = playerMovementManager;
        this.squareSize = squareSize;
        this.textureManager = new TextureManager();
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
        renderParticles(g2);
        renderAirParticles(g2);
        renderReloadingBar(g2, player);

        g2.translate(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());

        renderGunInterface(g2, component, player);
        renderHP(g2, component, player);
    }

    private void renderAirParticles(Graphics2D g2) {
        // Удаляем мертвые частицы
        airParticles.removeIf(particle -> !particle.isAlive(System.currentTimeMillis()));

        // Обновляем и рендерим все частицы
        for (AirParticle particle : airParticles) {
            particle.update();
            particle.render(g2);
        }
    }

    private void renderParticles(Graphics2D g2) {
        List<Particle> particles = bulletManager.getParticles();
        for (Particle particle : particles) {
            particle.update();
            particle.render(g2);
        }
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

        shotTrails.removeIf(trail -> !trail.isAlive(System.currentTimeMillis()));

        for (ShotTrail trail : shotTrails) {
            float progress = trail.getProgress(System.currentTimeMillis());

            int colorValue = 255 - (int)(220 * progress); // 220 чтобы не становилось совсем черным
            int alpha = 255 - (int)(255 * progress);

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
            for (Block block : blocks) {
                Rectangle bounds = block.getBounds();
                g2.setPaint(textureManager.getTexture(block.getId()));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        private void renderPredicted(Graphics2D g2){
            g2.setColor(Color.YELLOW);
            if (networkHandler.getUniquePoint() != null){
                g2.fillRect(networkHandler.getUniquePoint().x,  networkHandler.getUniquePoint().y, SQUARE_SIZE, SQUARE_SIZE);
            }
/*            for (Point point: networkHandler.getUniquePoints()){

                g2.fillRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);
            }*/
        }

        private void renderReal(Graphics2D g2) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2));
            if (networkHandler.getRealPoint() != null){
                g2.drawRect(networkHandler.getRealPoint().x, networkHandler.getRealPoint().y, SQUARE_SIZE, SQUARE_SIZE);
            }
/*            for (Point point : networkHandler.getRealPoints()) {
                g2.drawRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);
            }*/
        }

    public void createMovementParticles(Player player, float deltaX, float deltaY) {
        long currentTime = System.currentTimeMillis();

        // Создаем частицы только если игрок двигается и прошло достаточно времени
        if ((deltaX != 0 || deltaY != 0) && currentTime - lastParticleSpawnTime > 30) {
            int particleCount = 2 + (int)(Math.sqrt(deltaX*deltaX + deltaY*deltaY) / 2);

            // Нормализуем направление движения
            float length = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY);
            float dirX = -deltaX / length;
            float dirY = -deltaY / length;

            // Центр игрока
            int centerX = (int) (player.getX() + SQUARE_SIZE/2);
            int centerY = (int) (player.getY() + SQUARE_SIZE/2);

            // Создаем частицы
            for (int i = 0; i < particleCount; i++) {
                airParticles.add(new AirParticle(centerX, centerY, dirX, dirY));
            }

            lastParticleSpawnTime = currentTime;
        }
    }
    }