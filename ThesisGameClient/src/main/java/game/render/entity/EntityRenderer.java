package game.render.entity;

import utils.bullets.AbstractBulletManager;
import bullets.BulletManager;
import game.render.TextureManager;
import utils.Constants;
import utils.map.Block;
import utils.map.MapCreator;
import utils.network.ClientEnemy;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRenderer {
    private final TextureManager textureManager;
    private final int squareSize;

    public EntityRenderer(int squareSize) {
        this.squareSize = squareSize;
        this.textureManager = new TextureManager();
    }

    public void renderPlayer(Graphics2D g2, Point playerPosition) {
        g2.setColor(Color.BLUE);
        g2.fillRect(playerPosition.x, playerPosition.y, squareSize, squareSize);
    }


    public void renderInterpolationEnemies(Graphics2D g2, HashMap<String, ClientEnemy> enemies, AbstractBulletManager bulletManager) {
        long renderTime = System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS;

        for (Map.Entry<String, ClientEnemy> entry : enemies.entrySet()) {
            ClientEnemy clientEnemy = entry.getValue();
            Point interpPos = clientEnemy.getInterpolatedPosition(renderTime);

            if (bulletManager.getClass().equals(BulletManager.class)){
                var basicBM = (BulletManager) bulletManager;
                if (basicBM.getEnemyHitTimes().get(entry.getValue()) != null &&
                        (System.currentTimeMillis() - basicBM.getEnemyHitTimes().get(entry.getValue()) < 100)) {
                    g2.setColor(Color.RED);
                } else {
                    g2.setColor(clientEnemy.isBot() ? Color.GREEN : Color.PINK);
                }
            }
            else {
                g2.setColor(clientEnemy.isBot() ? Color.GREEN : Color.PINK);
            }


            g2.fillRect(interpPos.x, interpPos.y, squareSize, squareSize);
        }
    }

    public void renderMap(Graphics2D g2, MapCreator mapCreator) {
        paintBlock(g2, mapCreator.getWalls());
        paintBlock(g2, mapCreator.getAir());
    }

    private void paintBlock(Graphics2D g2, List<Block> blocks) {
        for (Block block : blocks) {
            Rectangle bounds = block.getBounds();
            g2.setPaint(textureManager.getTexture(block.getId()));
            g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}