package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import network.NetworkHandler;
import movement.MovementManager;
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


import static utils.Constants.SQUARE_SIZE;

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

    public GameController() throws IOException {
        playerCameraManager = new PlayerCameraManager(1000, 1000);


        targetEnemy = new Enemy(true, 200, 200, 100,  System.currentTimeMillis());

        mapCreator = new MapCreator((short) 3);
/*        System.out.println(
                Arrays.deepToString(mapCreator.generateMapMatrix())
                        .replace("], ", "]\n")  // Перенос строки после каждой строки массива
                        .replace("[[", "[\n[")  // Перенос строки после первой скобки
                        .replace("]]", "]\n]")  // Перенос строки перед закрывающей скобкой
        );*/
        collisionManager = new CollisionManager(mapCreator.getMap());
        setFocusable(true);

        Gun defaultGun = new Gun(1000, 12.0, 3.0, 1, 1000, 1);
        player = new Player(50, 50, defaultGun);
        KeyboardController keyboardController = new KeyboardController(player);

        addKeyListener(keyboardController);

        rayCastManager = new RayCastManager(player, mapCreator);




        playerMovementManager = new MovementManager(keyboardController, 50, 50, player, playerCameraManager, collisionManager, SQUARE_SIZE, 1500, 1200);

        networkHandler = new NetworkHandler("localhost", 12345, playerMovementManager, player);

        networkHandler.putToPlayerCoords(targetEnemy);
        networkHandler.startNetworkThreads();
        bulletManager = new BulletManager(player, this, targetEnemy, rayCastManager, networkHandler);
        mouseController = new MouseController(this, bulletManager);
        bulletManager.setPlayerCoords(networkHandler.getPlayerCoords());
        bulletManager.start();

        gameRenderer = new GameRenderer(playerCameraManager,  mapCreator, playerMovementManager,  networkHandler, SQUARE_SIZE, bulletManager);
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
