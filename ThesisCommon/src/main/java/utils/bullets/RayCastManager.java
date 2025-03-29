package utils.bullets;

import utils.Constants;
import utils.map.Block;
import utils.map.MapCreator;
import utils.network.Enemy;
import utils.player.Player;

import java.awt.*;
import java.util.HashMap;


public class RayCastManager {

    private final Player player;
    private final MapCreator mapCreator;

    public RayCastManager(Player player, MapCreator mapCreator) {
        this.player = player;
        this.mapCreator = mapCreator;
    }

    public RaycastHit raycast(double angle, HashMap<String, Enemy> playerCoords) {
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
}
