package game;

import utils.bullets.AbstractBulletManager;
import game.render.entity.EntityRenderer;
import game.render.particle.ParticleEffectsRenderer;
import game.render.ui.UIRenderer;
import movement.MovementManager;
import utils.map.MapCreator;
import utils.player.Player;
import utils.player.PlayerCameraManager;
import network.NetworkHandler;

import javax.swing.*;
import java.awt.*;

import static utils.Constants.SQUARE_SIZE;

public class GameRenderer {
    private final PlayerCameraManager playerCameraManager;
    private final EntityRenderer entityRenderer;
    private final UIRenderer uiRenderer;
    private final ParticleEffectsRenderer particleEffectsRenderer;
    private final NetworkHandler networkHandler;
    private final AbstractBulletManager bulletManager;
    private final MovementManager playerMovementManager;
    private final MapCreator mapCreator;

    public GameRenderer(PlayerCameraManager playerCameraManager,
                        MapCreator mapCreator, MovementManager playerMovementManager,
                        NetworkHandler networkHandler, int squareSize, AbstractBulletManager bulletManager, ParticleEffectsRenderer particleEffectsRenderer) {
        this.networkHandler = networkHandler;
        this.bulletManager = bulletManager;
        this.playerCameraManager = playerCameraManager;
        this.playerMovementManager = playerMovementManager;
        this.mapCreator = mapCreator;

        this.entityRenderer = new EntityRenderer(squareSize);
        this.uiRenderer = new UIRenderer();
        this.particleEffectsRenderer = particleEffectsRenderer;
    }

    public void render(Graphics g, JComponent component, Player player) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.translate(-playerCameraManager.getCameraX(), -playerCameraManager.getCameraY());

        entityRenderer.renderMap(g2, mapCreator);
        entityRenderer.renderPlayer(g2, new Point(playerMovementManager.getX(), playerMovementManager.getY()));

        entityRenderer.renderInterpolationEnemies(g2, networkHandler.getPlayerCoords(), bulletManager);

        renderPredicted(g2);

        renderReal(g2);

        g2.setColor(Color.RED);

        particleEffectsRenderer.renderEffects(g2,
                new Point(playerMovementManager.getX(), playerMovementManager.getY()),
                bulletManager
        );

        //particleEffectsRenderer.renderAdvancedBullets(bulletManager, g2);

        uiRenderer.renderReloadingBar(g2,
                new Point(playerMovementManager.getX(), playerMovementManager.getY()),
                SQUARE_SIZE,
                player.getGun()
        );

        g2.translate(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());

        uiRenderer.renderInterfaceBlock(g2, component, player, player.getGun());
    }

    private void renderPredicted(Graphics2D g2){
        g2.setColor(Color.YELLOW);
        if (networkHandler.getUniquePoint() != null){
            g2.fillRect(networkHandler.getUniquePoint().x,  networkHandler.getUniquePoint().y, SQUARE_SIZE, SQUARE_SIZE);
        }
/*            for (Point point: networkHandler.getUniquePoints()){

                g2.fillRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);
            }*/
    }

    private void renderReal(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        if (networkHandler.getRealPoint() != null){
            g2.drawRect(networkHandler.getRealPoint().x, networkHandler.getRealPoint().y, SQUARE_SIZE, SQUARE_SIZE);
        }
/*            for (Point point : networkHandler.getRealPoints()) {
                g2.drawRect(point.x, point.y, SQUARE_SIZE, SQUARE_SIZE);
            }*/
    }
}