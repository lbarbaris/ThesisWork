package bullets;

import network.NetworkHandler;
import utils.Constants;
import utils.bullets.*;
import utils.map.MapCreator;
import utils.network.ClientEnemy;
import utils.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.atan2;

public class BulletManager extends AbstractBulletManager {
    private final HashMap<ClientEnemy, Long> enemyHitTimes;
    private final JPanel panel;
    private final Player player;
    private final ClientEnemy targetClientEnemy;
    private NetworkHandler networkHandler;
    private Random random;

    private volatile boolean oneShot = false;
    private volatile Point lastHitPoint;
    private RayCastManager rayCastManager;

    public BulletManager(Player player, JPanel panel, ClientEnemy targetClientEnemy, RayCastManager rayCastManager, NetworkHandler networkHandler, MapCreator mapCreator) {
        particles = new ArrayList<>();
        this.mapCreator = mapCreator;
        this.random = new Random();
        this.networkHandler = networkHandler;
        this.rayCastManager = rayCastManager;
        this.player = player;
        this.lastHitPoint = null;
        this.panel = panel;
        this.targetClientEnemy = targetClientEnemy;
        this.enemyHitTimes = new HashMap<>();
    }


    public void startShootingLoop() {
        if (shootingThread != null && shootingThread.isAlive()) return;

        shootingThread = new Thread(() -> {
            Gun gun = player.getGun();

            while (isShooting) {
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

                playerCoords = networkHandler.getPlayerCoords();
                System.out.println(playerCoords);

                networkHandler.sendBasicBullets((int) startX, (int) startY, (int) mouseX, (int) mouseY);



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
                    ClientEnemy clientEnemy = hit.hitClientEnemy;
                    clientEnemy.doDamage(gun.getDamage());
                    enemyHitTimes.put(clientEnemy, System.currentTimeMillis());
                    System.out.println("Попадание! Цель: " + clientEnemy);
                    createBloodParticles(hit.hitPoint);
                    if (clientEnemy == targetClientEnemy) {
                        System.out.println("Попал по мишени!");
                        if (clientEnemy.getHp() <= 0) {
                            clientEnemy.setHp(Constants.PLAYER_MAX_HP);
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



    public HashMap<ClientEnemy, Long> getEnemyHitTimes() {
        return enemyHitTimes;
    }

    public Point getLastHitPoint() {
        return lastHitPoint;
    }

}



