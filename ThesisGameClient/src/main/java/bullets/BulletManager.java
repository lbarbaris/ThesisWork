package bullets;

import utils.Constants;
import utils.bullets.*;
import utils.map.Block;
import utils.map.MapCreator;
import utils.network.Enemy;
import network.NetworkHandler;
import utils.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.atan2;
import static java.lang.Math.max;

public class BulletManager extends Thread {
    private final HashMap<Enemy, Long> enemyHitTimes;
    private final MapCreator mapCreator;
    private final JPanel panel;
    private final Player player;
    private final Enemy targetEnemy;
    private NetworkHandler networkHandler;
    private final List<Particle> particles = new ArrayList<>();
    private Random random = new Random();


    private volatile boolean shooting;
    private volatile boolean oneShot = false;
    private volatile Point lastHitPoint;
    private Thread shootingThread;
    private RayCastManager rayCastManager;
    private HashMap<String, Enemy> playerCoords = null;

    public void setPlayerCoords(HashMap<String, Enemy> playerCoords) {
        this.playerCoords = playerCoords;
    }

    public BulletManager(Player player, JPanel panel, Enemy targetEnemy, RayCastManager rayCastManager, NetworkHandler networkHandler, MapCreator mapCreator) {
        this.mapCreator = mapCreator;
        this.networkHandler = networkHandler;
        this.rayCastManager = rayCastManager;
        this.player = player;
        this.lastHitPoint = null;
        this.panel = panel;
        this.targetEnemy = targetEnemy;
        this.enemyHitTimes = new HashMap<>();
    }


    public void startShootingLoop() {
        if (shootingThread != null && shootingThread.isAlive()) return;

        shootingThread = new Thread(() -> {
            Gun gun = player.getGun();

            while (shooting) {
                gun.updateReloadStatus();
                if (!gun.canShoot()) {
                    gun.reload();
                    continue;
                }

                Point mouse = panel.getMousePosition();
                if (mouse == null) return; // мышь вне панели

                double mouseX = mouse.getX();
                double mouseY = mouse.getY();

                double startX = player.getX();
                double startY = player.getY();

                try {
                    networkHandler.sendShootRequest((int) startX, (int) startY, (int) mouseX, (int) mouseY);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                double angle = atan2(mouseY - startY, mouseX - startX);

                RaycastHit hit = rayCastManager.raycast(angle, playerCoords);

                lastHitPoint = hit.hitPoint;

                if (hit.type == HitType.WALL) {
                    System.out.println("Пуля попала в стену на расстоянии " + hit.distance);
                    gun.shoot();
                    oneShot = true;
                    // Получаем цвет стены в точке попадания
                    Color wallColor = getWallColorAt(hit.hitPoint);
                    createWallParticles(hit.hitPoint, wallColor);
                }

                if (hit.type == HitType.ENEMY) {
                    Enemy enemy = hit.hitEnemy;
                    enemy.doDamage(gun.getDamage());
                    enemyHitTimes.put(enemy, System.currentTimeMillis());
                    System.out.println("Попадание! Цель: " + enemy);
                    createBloodParticles(hit.hitPoint);
                    if (enemy == targetEnemy) {
                        System.out.println("Попал по мишени!");
                        if (enemy.getHp() <= 0) {
                            enemy.setHp(Constants.PLAYER_MAX_HP);
                            System.out.println("Цель убита и возродилась.");
                        }
                    }
                }

                gun.shoot();
                oneShot = true;

                try {
                    Thread.sleep(gun.getDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        shootingThread.start();
    }


    private Color getWallColorAt(Point hitPoint) {
        for (Block block : mapCreator.getWalls()) {
            if (block.getBounds().contains(hitPoint)) {
                return switch (block.getId()) {
                    case 1 -> new Color(150, 70, 50);
                    case 2 -> new Color(50, 120, 50);
                    case 3 -> new Color(140, 200, 100);
                    default -> Color.GRAY;
                };
            }
        }
        return Color.GRAY;
    }

    public boolean getOneShot(){
        if (oneShot) {
            oneShot = false;
            return true;
        }
        return false;
    }



    public HashMap<Enemy, Long> getEnemyHitTimes() {
        return enemyHitTimes;
    }

    public Point getLastHitPoint() {
        return lastHitPoint;
    }

    private void createWallParticles(Point hitPoint, Color color) {
        for (int i = 0; i < 15; i++) {
            int speed = 1 + random.nextInt(3);
            double angle = random.nextDouble() * Math.PI * 2;
            Point velocity = new Point(
                    (int)(Math.cos(angle) * speed),
                    (int)(Math.sin(angle) * speed)
            );
            particles.add(new Particle(
                    color,
                    new Point(hitPoint.x, hitPoint.y),
                    velocity,
                    500 + random.nextInt(500), // время жизни
                    1 + random.nextInt(3) // размер
            ));
        }
    }

    private void createBloodParticles(Point hitPoint) {
        for (int i = 0; i < 20; i++) {
            int speed = 2 + random.nextInt(4);
            double angle = random.nextDouble() * Math.PI * 2;
            Point velocity = new Point(
                    (int)(Math.cos(angle) * speed),
                    (int)(Math.sin(angle) * speed)
            );
            // Разные оттенки красного
            Color bloodColor = new Color(
                    150 + random.nextInt(100),
                    random.nextInt(30),
                    random.nextInt(30)
            );
            particles.add(new Particle(
                    bloodColor,
                    new Point(hitPoint.x, hitPoint.y),
                    velocity,
                    300 + random.nextInt(400),
                    2 + random.nextInt(3)
            ));
        }
    }

    public List<Particle> getParticles() {
        particles.removeIf(p -> !p.isAlive());
        return particles;
    }


    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
}



