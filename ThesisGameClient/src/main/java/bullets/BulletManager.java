package bullets;

import map.Block;
import map.MapCreator;
import network.Enemy;
import player.Player;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import static java.lang.Math.atan2;

public class BulletManager extends Thread {
    private final HashMap<Enemy, Long> enemyHitTimes;
    private final JPanel panel;
    private final Player player;
    private final Enemy targetEnemy;
    private volatile boolean shooting;
    private volatile boolean oneShot = false;
    private boolean targetHit;
    private long targetHitTime;
    private volatile Point lastHitPoint;
    private Thread shootingThread;
    private HashMap<String, Enemy> playerCoords = null;
    private MapCreator mapCreator;

    public void setPlayerCoords(HashMap<String, Enemy> playerCoords) {
        this.playerCoords = playerCoords;
    }

    public BulletManager(Player player, JPanel panel, Enemy targetEnemy, MapCreator mapCreator) {
        this.mapCreator = mapCreator;
        this.targetHit = false;
        this.player = player;
        this.lastHitPoint = null;
        this.panel = panel;
        this.targetEnemy = targetEnemy;
        this.enemyHitTimes = new HashMap<>();
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
    }

    private void startShooting() {
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

                double angle = atan2(mouseY - startY, mouseX - startX);

                RaycastHit hit = raycast(angle);

                lastHitPoint = hit.hitPoint;

                if (hit.type == HitType.WALL) {
                    System.out.println("Пуля попала в стену на расстоянии " + hit.distance);
                    gun.shoot();
                    oneShot = true;
                }

                if (hit.type == HitType.ENEMY) {
                    Enemy enemy = hit.hitEnemy;
                    enemy.doDamage(gun.getDamage());
                    enemyHitTimes.put(enemy, System.currentTimeMillis());
                    System.out.println("Попадание! Цель: " + enemy);
                    if (enemy == targetEnemy) {
                        System.out.println("Попал по мишени!");
                        if (enemy.getHp() <= 0) {
                            enemy.setHp(100);
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

    public boolean getOneShot(){
        if (oneShot) {
            oneShot = false;
            return true;
        }
        return false;
    }

    public boolean isTargetHit() {
        return targetHit;
    }

    public long getTargetHitTime() {
        return targetHitTime;
    }

    private RaycastHit raycast(double angle) {
        double px = player.getX();
        double py = player.getY();
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);

        RaycastHit result = new RaycastHit();

        // --- 1. Проверяем врагов
        for (Enemy enemy : playerCoords.values()) {
            Point enemyPos = enemy.getInterpolatedPosition(System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS);
            double tx = enemyPos.x;
            double ty = enemyPos.y;

            double vx = tx - px;
            double vy = ty - py;
            double dot = vx * dx + vy * dy;

            if (dot < 0) continue; // враг позади

            double closestX = px + dot * dx;
            double closestY = py + dot * dy;

            double distSq = Math.pow(closestX - tx, 2) + Math.pow(closestY - ty, 2);
            double radius = 10; // "радиус" попадания по врагу

            if (distSq < radius * radius && dot < result.distance) {
                result.type = HitType.ENEMY;
                result.hitEnemy = enemy;
                result.distance = dot;
                result.hitPoint = new Point((int) closestX, (int) closestY);
            }
        }

        // --- 2. Проверяем стены
        double maxRayLength = 1000;
        for (Block block : mapCreator.getMap()) {
            Rectangle wall = new Rectangle(block.x, block.y, block.width, block.height);

            for (double t = 0; t < maxRayLength; t += 1) {
                int checkX = (int) (px + dx * t);
                int checkY = (int) (py + dy * t);
                if (wall.contains(checkX, checkY)) {
                    if (t < result.distance) {
                        result.type = HitType.WALL;
                        result.distance = t;
                        result.hitPoint = new Point(checkX, checkY);
                        result.hitEnemy = null;
                    }
                    break;
                }
            }
        }

        return result;
    }

    public HashMap<Enemy, Long> getEnemyHitTimes() {
        return enemyHitTimes;
    }

    public Point getLastHitPoint() {
        return lastHitPoint;
    }
}



