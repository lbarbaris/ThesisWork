package bullets;

import player.Player;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class BulletManager extends Thread {
    private final List<Bullet> bullets;
    private final JPanel panel;
    private final Player player;

    private volatile boolean shooting;
    private Thread shootingThread;

    public BulletManager(Player player, List<Bullet> bullets, JPanel panel) {
        this.player = player;
        this.bullets = bullets;
        this.panel = panel;
        this.shooting = false;
    }

    @Override
    public void run() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                shooting = true;
                startShooting();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                shooting = false;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Перемещение мыши обрабатывается здесь
            }
        });
    }

    private void startShooting() {
        if (shootingThread != null && shootingThread.isAlive()) {
            return;
        }

        shootingThread = new Thread(() -> {
            Gun gun = player.getGun();
            while (shooting) {
                if (!gun.canShoot()) {
                    gun.reload();
                    continue;
                }

                double startX = player.getX();
                double startY = player.getY();
                double mouseX = panel.getMousePosition().getX() + player.getCameraX();
                double mouseY = panel.getMousePosition().getY() + player.getCameraY();

                double angle = Math.atan2(
                        mouseY - startY + (Math.random() * 2 - 1) * gun.getAccuracy(),
                        mouseX - startX + (Math.random() * 2 - 1) * gun.getAccuracy()
                );

                double speedX = Math.cos(angle) * gun.getSpeed();
                double speedY = Math.sin(angle) * gun.getSpeed();

                synchronized (bullets) {
                    bullets.add(new Bullet(startX, startY, speedX, speedY));
                }

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

    public boolean isBulletHitPlayer(Bullet bullet, Player player, double squareSize) {
        double dx = bullet.getX() - (player.getX() + squareSize / 2.0);
        double dy = bullet.getY() - (player.getY() + squareSize / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance < squareSize / 2.0; // Считаем попадание, если пуля в радиусе игрока
    }
}