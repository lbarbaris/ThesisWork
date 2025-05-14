package bullets;

import network.NetworkHandler;
import utils.bullets.AbstractBulletManager;
import utils.bullets.Bullet;
import utils.bullets.Gun;
import utils.bullets.RayCastManager;
import utils.map.CollisionManager;
import utils.map.MapCreator;
import utils.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static utils.Constants.INTERPOLATION_DELAY_MS;
import static utils.Constants.SQUARE_SIZE;

public class AdvancedBulletManager extends AbstractBulletManager {
    private final List<Bullet> bullets;
    private final List<Bullet> bulletsToSend;
    private final JPanel panel;
    private final Player player;
    private final RayCastManager rayCastManager;
    private final NetworkHandler networkHandler;
    private final CollisionManager collisionManager;

    public AdvancedBulletManager(Player player, JPanel panel, RayCastManager rayCastManager,
                                 NetworkHandler networkHandler, MapCreator mapCreator) {
        this.particles = new ArrayList<>();
        this.bullets = new CopyOnWriteArrayList<>();
        this.bulletsToSend = new CopyOnWriteArrayList<>();

        this.player = player;
        this.panel = panel;
        this.rayCastManager = rayCastManager;
        this.networkHandler = networkHandler;
        this.mapCreator = mapCreator;
        this.collisionManager = new CollisionManager(this.mapCreator.getWalls());

        startBulletUpdateLoop();
    }

    public void startShootingLoop() {
        if (shootingThread != null && shootingThread.isAlive()) return;

        shootingThread = new Thread(() -> {
            while (isShooting) {
                Gun gun = player.getGun();
                gun.updateReloadStatus();
                if (!gun.canShoot()) {
                    gun.reload();
                    continue;
                }

                Point mouse = panel.getMousePosition();
                if (mouse == null) return;

                double startX = player.getX();
                double startY = player.getY();
                double mouseX = mouse.getX();
                double mouseY = mouse.getY();
                double angle = Math.atan2(mouseY - startY, mouseX - startX);

                Bullet bullet = new Bullet(startX, startY, angle, gun.getDamage(), gun.getSpeed());
                bullets.add(bullet);
                bulletsToSend.add(bullet);

                gun.shoot();

                try {
                    Thread.sleep(gun.getDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        shootingThread.start();
    }

    private void startBulletUpdateLoop() {
        Timer bulletUpdateTimer = new Timer(16, e -> updateBullets());
        bulletUpdateTimer.start();
    }

    private void updateBullets() {
        networkHandler.sendAdvancedBullets(bulletsToSend);
        bulletsToSend.clear();

        for (var bullet : bullets) {
            bullet.update();

            var bulletBounds = bullet.getBounds();

            // Проверка столкновения со стеной
            if (collisionManager.isWallHit(bulletBounds)) {
                var wallPoint = collisionManager.getPointWallHit(bulletBounds);
                System.out.println("Пуля попала в стену: " + wallPoint);
                var wallColor = getWallColorAt(wallPoint);
                createWallParticles(wallPoint, wallColor);
                bullets.remove(bullet);
                continue;
            }

            // Проверка попадания по врагам
            for (var enemy : playerCoords.values()) {
                var interpolatedPosition = enemy.getInterpolatedPosition(INTERPOLATION_DELAY_MS);
                var bounds = new Rectangle(interpolatedPosition.x, interpolatedPosition.y, SQUARE_SIZE, SQUARE_SIZE);
                if (bounds.intersects(bulletBounds)) {
                    enemy.doDamage(bullet.getDamage());
                    createBloodParticles(enemy.getInterpolatedPosition(INTERPOLATION_DELAY_MS));
                    System.out.println("Пуля попала по врагу: " + enemy);
                    bullets.remove(bullet);
                    break;
                }
            }
        }
    }

    public List<Bullet> getBullets() {
        return bullets;
    }
}
