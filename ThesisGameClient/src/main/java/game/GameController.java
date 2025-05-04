package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.NetworkHandler;
import movement.MovementManager;
import utils.Constants;
import utils.bullets.Gun;
import utils.bullets.RayCastManager;
import utils.input.KeyboardController;
import bullets.MouseController;
import utils.map.CollisionManager;
import utils.network.Enemy;
import utils.player.PlayerCameraManager;
import utils.map.MapCreator;
import bullets.BulletManager;
import utils.player.Player;

import static utils.Constants.*;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private Enemy targetEnemy;
    private final CollisionManager collisionManager;
    private final MouseController mouseController;
    private final RayCastManager rayCastManager;

    private final MovementManager playerMovementManager;
    private final NetworkHandler networkHandler;

    private final MapCreator mapCreator;
    private final BulletManager bulletManager;


    private final Player player;
    private final GameRenderer gameRenderer;

    public GameController(String serverAddress, String serverPort) throws IOException {
        playerCameraManager = new PlayerCameraManager(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);


        targetEnemy = new Enemy(true, 200, 200, Constants.PLAYER_MAX_HP,  System.currentTimeMillis());

        mapCreator = new MapCreator((short) 3);
/*        System.out.println(
                Arrays.deepToString(mapCreator.generateMapMatrix())
                        .replace("], ", "]\n")  // Перенос строки после каждой строки массива
                        .replace("[[", "[\n[")  // Перенос строки после первой скобки
                        .replace("]]", "]\n]")  // Перенос строки перед закрывающей скобкой
        );*/
        collisionManager = new CollisionManager(mapCreator.getWalls());
        setFocusable(true);

        Gun defaultGun = new Gun(10, 12.0, 3.0, 30, 1000, 1);
        player = new Player(PLAYER_SPAWN_X, PLAYER_SPAWN_Y, defaultGun);
        KeyboardController keyboardController = new KeyboardController(player);

        addKeyListener(keyboardController);

        rayCastManager = new RayCastManager(player, mapCreator);

        playerMovementManager = new MovementManager(keyboardController, PLAYER_SPAWN_X, PLAYER_SPAWN_Y, player, playerCameraManager, collisionManager);

        networkHandler = new NetworkHandler(serverAddress, serverPort, playerMovementManager, player);

        networkHandler.putToPlayerCoords(targetEnemy);
        networkHandler.startNetworkThreads();
        bulletManager = new BulletManager(player, this, targetEnemy, rayCastManager, networkHandler, mapCreator);
        mouseController = new MouseController(this, bulletManager);
        bulletManager.setPlayerCoords(networkHandler.getPlayerCoords());
        bulletManager.start();

        gameRenderer = new GameRenderer(playerCameraManager,  mapCreator, playerMovementManager,  networkHandler, SQUARE_SIZE, bulletManager);

        playerMovementManager.setGameRenderer(gameRenderer);
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
