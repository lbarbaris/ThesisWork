package game;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import bullets.Gun;
import map.paths.PathfindingAbstractClass;
import map.paths.WaveAlgorithm;
import network.Enemy;
import network.NetworkHandler;
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
    private final PathfindingAbstractClass pathFindingAbstractClass;
    private final NetworkHandler networkHandler;


    public GameRenderer(PlayerCameraManager playerCameraManager, Player targetPlayer, CopyOnWriteArrayList<Bullet> bullets,
                        MapCreator mapCreator, MovementManager playerMovementManager, int squareSize, PathfindingAbstractClass pathFindingAbstractClass, NetworkHandler networkHandler) {
        this.playerCameraManager = playerCameraManager;
        this.targetPlayer = targetPlayer;
        this.bullets = bullets;
        this.mapCreator = mapCreator;
        this.playerMovementManager = playerMovementManager;
        this.squareSize = squareSize;
        this.pathFindingAbstractClass = pathFindingAbstractClass;
        this.networkHandler = networkHandler;
    }

    public void render(Graphics g, JComponent component, boolean targetHit, long targetHitTime, Player player) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Arial", Font.BOLD, 20));

        // Сохраняем состояние графического контекста
        g2.translate(-playerCameraManager.getCameraX(), -playerCameraManager.getCameraY());

        renderMap(g2, mapCreator);

        renderMapName(g2, mapCreator);

        renderPlayer(g2
        );
        renderEnemies(g2);

        renderBullets(g2);

        renderTarget(g2, targetHit, targetHitTime);

        g2.translate(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());

        renderReloadingBar(g2, player);

        renderGunInterface(g2, component, player);

        renderHP(g2, component, player);

        renderPath(g2);

    }

    private void renderTarget(Graphics2D g2, boolean targetHit, long targetHitTime){
        if (targetHit && System.currentTimeMillis() - targetHitTime < 100) {
            g2.setColor(Color.RED);
        } else {
            if (targetPlayer.getHp() <= 50){
                g2.setColor(Color.MAGENTA);
            }
            else{
                g2.setColor(Color.ORANGE);
            }
        }
        g2.fillRect((int) targetPlayer.getX(), (int) targetPlayer.getY(), squareSize, squareSize);
    }

    private void renderBullets(Graphics2D g2){
        g2.setColor(Color.RED);
        for (Bullet bullet : bullets) {
            g2.fillOval((int) bullet.getX(), (int) bullet.getY(), 10, 10);
        }
    }

    private void renderEnemies(Graphics2D g2){
        HashMap<String, Enemy> coords = networkHandler.getPlayerCoords();

        for (Map.Entry<String, Enemy> entry1: coords.entrySet()){
            if (entry1.getValue().isBot()){
                g2.setColor(Color.GREEN);
                g2.fillRect(entry1.getValue().getCoordinates().x, entry1.getValue().getCoordinates().y, squareSize, squareSize);
            }
            else {
                g2.setColor(Color.PINK);
                g2.fillRect(entry1.getValue().getCoordinates().x, entry1.getValue().getCoordinates().y, squareSize, squareSize);
            }
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

    private void renderPath(Graphics2D g2){
        LinkedList<Point> path = pathFindingAbstractClass.getPath();
        g2.setColor(Color.CYAN);
        for (Point p : path) {
            g2.fillRect(p.x * squareSize + squareSize / 4, p.y * squareSize + squareSize / 4,
                    squareSize / 2, squareSize / 2);
        }
    }
}