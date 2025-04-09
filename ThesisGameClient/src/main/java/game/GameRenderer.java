package game;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import network.NetworkHandler;
import movement.MovementManager;
import utils.Constants;
import bullets.BulletManager;
import utils.bullets.Gun;
import utils.map.MapCreator;
import utils.network.Enemy;
import utils.player.PlayerCameraManager;
import utils.player.Player;



public class GameRenderer {
    private final PlayerCameraManager playerCameraManager;
    private final MapCreator mapCreator;
    private final MovementManager playerMovementManager;
    private final int squareSize;
    private final NetworkHandler networkHandler;
    private final BulletManager bulletManager;
    private long lastShot;

    public GameRenderer(PlayerCameraManager playerCameraManager,
                        MapCreator mapCreator, MovementManager playerMovementManager, NetworkHandler networkHandler, int squareSize, BulletManager bulletManager) {
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

        // Сохраняем состояние графического контекста
        g2.translate(-playerCameraManager.getCameraX(), -playerCameraManager.getCameraY());

        renderMap(g2, mapCreator);

        renderMapName(g2, mapCreator);

        renderPlayer(g2);

        renderEnemies(g2);

        renderInstantShot(g2, player, component);

        renderReloadingBar(g2, player);

        g2.translate(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());



        renderGunInterface(g2, component, player);

        renderHP(g2, component, player);
    }



    private void renderEnemies(Graphics2D g2){
        HashMap<String, Enemy> coords = networkHandler.getPlayerCoords();
        long renderTime = System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS;

        HashMap<Enemy, Long> hitTimes = bulletManager.getEnemyHitTimes();



        for (Map.Entry<String, Enemy> entry : coords.entrySet()) {
            Enemy enemy = entry.getValue();
            Point interpPos = enemy.getInterpolatedPosition(renderTime);
            if (!hitTimes.isEmpty() && hitTimes.get(entry.getValue()) != null && (System.currentTimeMillis() - hitTimes.get(entry.getValue()) < 100)){
                g2.setColor(Color.RED);
            }
            else {
                if (enemy.isBot()) {
                    g2.setColor(Color.GREEN);
                } else {
                    g2.setColor(Color.PINK);
                }
            }


            g2.fillRect(interpPos.x, interpPos.y, squareSize, squareSize);
        }
    }

    private void renderPlayer(Graphics2D g2){
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

    private void renderMap(Graphics2D g2, MapCreator mapCreator){
        for (Shape shape : mapCreator.getMap()) {
            g2.fillRect(shape.getBounds().x, shape.getBounds().y, shape.getBounds().width, shape.getBounds().height);
        }
    }

    private void renderInstantShot(Graphics2D g2, Player player, JComponent component) {
        if (bulletManager.getOneShot()) {
            lastShot = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastShot <= 50){
            Point hitPoint = bulletManager.getLastHitPoint();
            if (hitPoint == null) return;

            g2.setStroke(new BasicStroke(4)); // Здесь задаём толщину (например, 4 пикселя)

            g2.setColor(Color.YELLOW);
            g2.drawLine(
                    (int) (player.getX() + squareSize / 2.0),
                    (int) (player.getY() + squareSize / 2.0),
                    hitPoint.x,
                    hitPoint.y
            );
        }
    }
}