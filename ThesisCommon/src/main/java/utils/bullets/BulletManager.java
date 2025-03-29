package utils.bullets;

import utils.network.Enemy;
import utils.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

import static java.lang.Math.atan2;

public class BulletManager extends Thread {
    private final HashMap<Enemy, Long> enemyHitTimes;
    private final JPanel panel;
    private final Player player;
    private final Enemy targetEnemy;


    private volatile boolean shooting;
    private volatile boolean oneShot = false;
    private volatile Point lastHitPoint;
    private Thread shootingThread;
    private RayCastManager rayCastManager;
    private HashMap<String, Enemy> playerCoords = null;

    public void setPlayerCoords(HashMap<String, Enemy> playerCoords) {
        this.playerCoords = playerCoords;
    }

    public BulletManager(Player player, JPanel panel, Enemy targetEnemy, RayCastManager rayCastManager) {
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

                double angle = atan2(mouseY - startY, mouseX - startX);

                RaycastHit hit = rayCastManager.raycast(angle, playerCoords);

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



    public HashMap<Enemy, Long> getEnemyHitTimes() {
        return enemyHitTimes;
    }

    public Point getLastHitPoint() {
        return lastHitPoint;
    }


    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
}



