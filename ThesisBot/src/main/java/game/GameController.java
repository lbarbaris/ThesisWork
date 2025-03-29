package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import map.cells.Cell;
import map.paths.AStarAlgorithm;
import map.paths.WaveAlgorithm;
import utils.bullets.RayCastManager;

import map.paths.PathfindingAbstractClass;
import network.NetworkHandler;
import utils.graphs.GraphResource;
import utils.network.Enemy;
import utils.player.Player;
import utils.bullets.BulletManager;
import utils.map.MapCreator;
import utils.bullets.Gun;
import movement.MovementManager;
import utils.map.CollisionManager;
import utils.player.PlayerCameraManager;
import utils.Constants;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private RayCastManager rayCastManager;
    private Enemy targetEnemy;
    private final CollisionManager collisionManager;
    private final MovementManager playerMovementManager;
    private final NetworkHandler networkHandler;
    private final PathfindingAbstractClass pathFinding;

    private final MapCreator mapCreator;

    private final BulletManager bulletManager;
    private final Player player;
    private final GameRenderer gameRenderer;
    private int distanceCounter;
    private GraphResource<Long> graphResource;
    private int graphCounter;

    public GameController() throws IOException {
        graphCounter = 0;


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

        playerMovementManager = new MovementManager(50, 50, player, playerCameraManager, collisionManager);

        networkHandler = new NetworkHandler("localhost", Constants.SERVER_PORT, playerMovementManager, bulletManager, player);
        networkHandler.putToPlayerCoords(targetEnemy);
        networkHandler.startNetworkThreads();

        bulletManager.setPlayerCoords(networkHandler.getPlayerCoords());
        bulletManager.start();

        gameRenderer = new GameRenderer(playerCameraManager, bulletManager,  mapCreator, playerMovementManager, pathFinding, networkHandler);

        distanceCounter = 0;


        graphResource = new GraphResource<>("Замер скорости алгоритма " + pathFinding.getClass() + " при размере карты в " + mapCreator.getWidthCellsSize(), "Замеры", "Время поиска пути");

        graphResource.addSeries();
        graphResource.addSeries();

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
        //System.out.println(coords.keySet());

        if (distanceCounter % Constants.BOT_UPDATE_PATH_RATE == 0 && !coords.isEmpty()) {

            double distance = Double.MAX_VALUE;
            String closestPlayerName = "";
            long renderTime = System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS;


            for (Map.Entry<String, Enemy> entry1 : coords.entrySet()) {
                double checkDistance = playerMovementManager.getCell().distance(new Cell(entry1.getValue().getInterpolatedPosition(renderTime)));

                if (checkDistance < distance && !entry1.getValue().isBot()) {
                    distance = checkDistance;
                    closestPlayerName = entry1.getKey();
                }
            }


            if (!closestPlayerName.isEmpty()) {
                long startTime = System.currentTimeMillis();
                pathFinding.findPath(new Cell(playerMovementManager.getX(), playerMovementManager.getY()), new Cell(coords.get(closestPlayerName).getInterpolatedPosition(renderTime)));
                if (graphCounter < Constants.PATH_MEASUREMENT_SIZE){
                    graphResource.addValue(0, (long) graphCounter);
                    graphResource.addValue(1, (startTime - System.currentTimeMillis()));
                    graphCounter++;
                }
                else {
                    graphResource.exportToTxt("ThesisGraphs/resources/" + pathFinding.getClass() + "_" + mapCreator.getWidthCellsSize() + ".txt");
                }

                playerMovementManager.setPath(pathFinding.getPath());
            }
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
