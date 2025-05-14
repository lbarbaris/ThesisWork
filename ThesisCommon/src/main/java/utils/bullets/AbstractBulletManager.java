package utils.bullets;

import utils.map.Block;
import utils.map.MapCreator;
import utils.network.ClientEnemy;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public abstract class AbstractBulletManager {
    protected volatile boolean isShooting;
    protected Thread shootingThread;
    protected HashMap<String, ClientEnemy> playerCoords;
    protected List<Particle> particles;
    private final Random random = new Random();
    protected MapCreator mapCreator;

    public abstract void startShootingLoop();

    public boolean isShooting() {
        return isShooting;
    }
    public void setShooting(boolean shooting) {
        this.isShooting = shooting;
    }

    public void setPlayerCoords(HashMap<String, ClientEnemy> playerCoords) {
        this.playerCoords = playerCoords;
    }

    protected void createWallParticles(Point hitPoint, Color color) {
        for (int i = 0; i < 15; i++) {
            var speed = 1 + random.nextInt(3);
            var angle = random.nextDouble() * Math.PI * 2;
            var velocity = new Point(
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

    protected void createBloodParticles(Point hitPoint) {
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

    protected Color getWallColorAt(Point hitPoint) {
        System.out.println(mapCreator);
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

    public List<Particle> getParticles() {
        particles.removeIf(p -> !p.isAlive());
        return particles;
    }
}
