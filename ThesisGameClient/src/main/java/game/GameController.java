package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import utils.bullets.AbstractBulletManager;
import bullets.AdvancedBulletManager;
import game.render.particle.ParticleEffectsRenderer;
import network.NetworkHandler;
import movement.MovementManager;
import utils.Constants;
import utils.bullets.Gun;
import utils.bullets.RayCastManager;
import utils.input.KeyboardController;
import bullets.MouseController;
import utils.map.CollisionManager;
import utils.network.ClientEnemy;
import utils.player.PlayerCameraManager;
import utils.map.MapCreator;
import utils.player.Player;

import static utils.Constants.*;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private ClientEnemy targetClientEnemy;
    private final CollisionManager collisionManager;
    private final MouseController mouseController;
    private final RayCastManager rayCastManager;
    private final ParticleEffectsRenderer particleEffectsRenderer;

    private final MovementManager playerMovementManager;
    private final NetworkHandler networkHandler;

    private final AbstractBulletManager bulletManager;

    private final MapCreator mapCreator;


    private final Player player;
    private final GameRenderer gameRenderer;

    public GameController(String serverAddress, String serverPort) throws IOException {
        particleEffectsRenderer = new ParticleEffectsRenderer();

        playerCameraManager = new PlayerCameraManager(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);


        targetClientEnemy = new ClientEnemy(true, 200, 200, Constants.PLAYER_MAX_HP);

        mapCreator = new MapCreator((short) 3);
/*        System.out.println(
                Arrays.deepToString(mapCreator.generateMapMatrix())
                        .replace("], ", "]\n")  // Перенос строки после каждой строки массива
                        .replace("[[", "[\n[")  // Перенос строки после первой скобки
                        .replace("]]", "]\n]")  // Перенос строки перед закрывающей скобкой
        );*/
        collisionManager = new CollisionManager(mapCreator.getWalls());
        setFocusable(true);

        //Gun defaultGun = new Gun(30, 50, 3.0, 300, 1000, 1);
        var defaultGun = new Gun(0);
        player = new Player(PLAYER_SPAWN_X, PLAYER_SPAWN_Y, defaultGun);
        KeyboardController keyboardController = new KeyboardController(player);

        addKeyListener(keyboardController);

        rayCastManager = new RayCastManager(player, mapCreator);

        playerMovementManager = new MovementManager(keyboardController, PLAYER_SPAWN_X, PLAYER_SPAWN_Y, player, playerCameraManager, collisionManager);

        networkHandler = new NetworkHandler(serverAddress, serverPort, playerMovementManager, player);

        networkHandler.putToPlayerCoords(targetClientEnemy);
        networkHandler.startNetworkThreads();
        bulletManager = new AdvancedBulletManager(player, this, rayCastManager, networkHandler, mapCreator);

        //bulletManager = new BulletManager(player, this, targetEnemy, rayCastManager, networkHandler, mapCreator);
        mouseController = new MouseController(this, bulletManager);
        bulletManager.setPlayerCoords(networkHandler.getPlayerCoords());



        gameRenderer = new GameRenderer(playerCameraManager,  mapCreator, playerMovementManager,  networkHandler, SQUARE_SIZE, bulletManager, particleEffectsRenderer);

        playerMovementManager.setParticleEffectsRenderer(particleEffectsRenderer);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameRenderer.render(g, this, player);
    }

    public MovementManager getPlayerMovementManager() {
        return playerMovementManager;
    }


    public Player getPlayer() {
        return player;
    }
}
