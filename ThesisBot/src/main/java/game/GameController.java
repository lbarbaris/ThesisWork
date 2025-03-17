package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import bullets.RayCastManager;
import map.cells.Cell;
import map.paths.PathfindingAbstractClass;
import map.paths.WaveAlgorithm;
import network.Enemy;
import network.NetworkHandler;
import player.Player;
import bullets.BulletManager;
import map.MapCreator;
import bullets.Gun;
import movement.MovementManager;
import utils.CollisionManager;
import player.PlayerCameraManager;
import utils.MouseController;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private RayCastManager rayCastManager;
    private Enemy targetEnemy;
    private final CollisionManager collisionManager;
    private final MouseController mouseController;
    private final MovementManager playerMovementManager;
    private final NetworkHandler networkHandler;
    private final PathfindingAbstractClass pathFinding;

    private final int squareSize = 20;
    private final MapCreator mapCreator;

    private final BulletManager bulletManager;
    private final Player player;
    private final GameRenderer gameRenderer;
    private int distanceCounter;

    public GameController() throws IOException {

        playerCameraManager = new PlayerCameraManager(1000, 1000);

        targetEnemy = new Enemy(true, 200, 200, 100,  System.currentTimeMillis());

        mapCreator = new MapCreator((short) 3);


        pathFinding = new WaveAlgorithm(mapCreator);
        collisionManager = new CollisionManager(mapCreator.getMap());
        setFocusable(true);

        Gun defaultGun = new Gun(1000, 12.0, 3.0, 100, 2500, (short) 10);
        player = new Player(50, 50, defaultGun);

        this.rayCastManager = new RayCastManager(player, mapCreator);
        bulletManager = new BulletManager(player, this, targetEnemy, rayCastManager);

        mouseController = new MouseController(this, bulletManager);

        playerMovementManager = new MovementManager(50, 50, player, playerCameraManager, collisionManager, squareSize, 1500, 1200);

        networkHandler = new NetworkHandler("localhost", 12345, playerMovementManager, bulletManager, player);
        networkHandler.putToPlayerCoords(targetEnemy);
        networkHandler.startNetworkThreads();

        bulletManager.setPlayerCoords(networkHandler.getPlayerCoords());
        bulletManager.start();

        gameRenderer = new GameRenderer(playerCameraManager, bulletManager,  mapCreator, playerMovementManager,  squareSize, pathFinding, networkHandler);
        long time = System.currentTimeMillis();

        long time2 = System.currentTimeMillis();
        System.out.println("Algorithm time:" + (time2 - time));

        distanceCounter = 0;


/*        System.out.println(
                Arrays.deepToString(pathFinding.getDistances())
                        .replace("], ", "]\n")  // Перенос строки после каждой строки массива
                        .replace("[[", "[\n[")  // Перенос строки после первой скобки
                        .replace("]]", "]\n]")  // Перенос строки перед закрывающей скобкой
        );*/




    }

    public void setupShootingLoop(){
        new Timer(2, e -> updateGame()).start();
    }

    private void updateGame() {
        player.getGun().updateReloadStatus();

        HashMap<String, Enemy> coords = networkHandler.getPlayerCoords();

        if (distanceCounter % 150 == 0 && !coords.isEmpty()) {

            double distance = Double.MAX_VALUE;
            String closestPlayerName = "";


            for (Map.Entry<String, Enemy> entry1 : coords.entrySet()) {
                double checkDistance = playerMovementManager.getCell().distance(new Cell(entry1.getValue().getCoordinates()));
                if (checkDistance < distance && !entry1.getValue().isBot()) {
                    distance = checkDistance;
                    closestPlayerName = entry1.getKey();
                }
            }

            pathFinding.findPath(new Cell(playerMovementManager.getX(), playerMovementManager.getY()), new Cell(coords.get(closestPlayerName).getCoordinates()));
            playerMovementManager.setPath(pathFinding.getPath());
            //;
        }
        distanceCounter++;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameRenderer.render(g, this, player);
    }

    public MovementManager getPlayerMovementManager() {
        return playerMovementManager;
    }
}
