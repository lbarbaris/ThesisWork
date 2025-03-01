package game;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

import bullets.Gun;
import player.Player;
import bullets.Bullet;
import map.MapCreator;
import player.PlayerCameraManager;
import movement.MovementManager;

public class GameRenderer {
    private final PlayerCameraManager playerCameraManager;
    private final Player targetPlayer;
    private final CopyOnWriteArrayList<Bullet> bullets;
    private final MapCreator mapCreator;
    private final MovementManager playerMovementManager;
    private final int squareSize;

    public GameRenderer(PlayerCameraManager playerCameraManager, Player targetPlayer, CopyOnWriteArrayList<Bullet> bullets,
                        MapCreator mapCreator, MovementManager playerMovementManager, int squareSize) {
        this.playerCameraManager = playerCameraManager;
        this.targetPlayer = targetPlayer;
        this.bullets = bullets;
        this.mapCreator = mapCreator;
        this.playerMovementManager = playerMovementManager;
        this.squareSize = squareSize;
    }

    public void render(Graphics g, JComponent component, boolean targetHit, long targetHitTime, Player player) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Arial", Font.BOLD, 20));

        // Сохраняем состояние графического контекста
        g2.translate(-playerCameraManager.getCameraX(), -playerCameraManager.getCameraY());

        // Отрисовка игровых объектов с учетом камеры
        for (Shape shape : mapCreator.getMap()) {
            g2.fillRect(shape.getBounds().x, shape.getBounds().y, shape.getBounds().width, shape.getBounds().height);
        }

        g2.setColor(Color.GREEN);
        g2.drawString(mapCreator.getMapName(), 10 + playerCameraManager.getCameraX(), 20 + playerCameraManager.getCameraY());

        g2.setColor(Color.BLUE);
        g2.fillRect(playerMovementManager.getX(), playerMovementManager.getY(), squareSize, squareSize);

        g2.setColor(Color.RED);
        for (Bullet bullet : bullets) {
            g2.fillOval((int) bullet.getX(), (int) bullet.getY(), 10, 10);
        }

        // Отрисовка мишени
        if (targetHit && System.currentTimeMillis() - targetHitTime < 100) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.ORANGE);
        }
        g2.fillRect((int) targetPlayer.getX(), (int) targetPlayer.getY(), squareSize, squareSize);

        g2.translate(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());

        // Отрисовка элементов интерфейса (без смещения камеры)
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
}