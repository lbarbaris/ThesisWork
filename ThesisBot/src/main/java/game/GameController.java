package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import map.cells.Cell;
import map.paths.CycleAlgorithm;
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

import static utils.Constants.BOT_SPAWN_X;
import static utils.Constants.BOT_SPAWN_Y;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private final RayCastManager rayCastManager;
    private final Enemy targetEnemy;
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
    private boolean isGraphSaved;

    public GameController() throws IOException {
        graphCounter = 0;
        isGraphSaved = false;

        playerCameraManager = new PlayerCameraManager(1000, 1000);

        targetEnemy = new Enemy(true, 200, 200, Constants.PLAYER_MAX_HP,  System.currentTimeMillis());

        mapCreator = new MapCreator((short) 3);


        pathFinding = new CycleAlgorithm(mapCreator);
        collisionManager = new CollisionManager(mapCreator.getWalls());
        setFocusable(true);

        Gun defaultGun = new Gun(1000, 12.0, 3.0, 100, 2500, (short) 10);
        player = new Player(BOT_SPAWN_X, BOT_SPAWN_Y, defaultGun);

        this.rayCastManager = new RayCastManager(player, mapCreator);
        bulletManager = new BulletManager(player, this, targetEnemy, rayCastManager);

        playerMovementManager = new MovementManager(BOT_SPAWN_X, BOT_SPAWN_Y, player, playerCameraManager, collisionManager);

        networkHandler = new NetworkHandler(Constants.SERVER_ADDRESS, Constants.SERVER_PORT, playerMovementManager, bulletManager, player);
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

        if (distanceCounter % Constants.BOT_UPDATE_PATH_RATE == 0 && !coords.isEmpty()) {

            double distance = Double.MAX_VALUE;
            String closestPlayerName = "";
            long renderTime = System.currentTimeMillis() - Constants.INTERPOLATION_DELAY_MS;

            for (Map.Entry<String, Enemy> entry1 : coords.entrySet()) {
                double checkDistance = 0;// playerMovementManager.getCell().distance(new Cell(entry1.getValue().getInterpolatedPosition(renderTime)));

                if (checkDistance < distance && !entry1.getValue().isBot()) {
                    distance = checkDistance;
                    closestPlayerName = entry1.getKey();
                }
            }

            if (!closestPlayerName.isEmpty()) {
                long startTime = System.nanoTime();

                if (!Constants.IS_CYCLE_BOT) {
                    var botCell = new Cell(playerMovementManager.getX(), playerMovementManager.getY());
                    var playerCell = new Cell(0,0); //coords.get(closestPlayerName).getInterpolatedPosition(renderTime).x, coords.get(closestPlayerName).getInterpolatedPosition(renderTime).y);
                    pathFinding.findPath(botCell, playerCell);
                }
                else {
                    Cell current = new Cell(playerMovementManager.getX(), playerMovementManager.getY());
                  pathFinding.findPath(current, null);
                }

                if (graphCounter < Constants.PATH_MEASUREMENT_SIZE){
/*                    graphResource.addValue(0, (long) graphCounter);
                    graphResource.addValue(1, (System.nanoTime() - startTime));
                    graphCounter++;*/
                }
                else if (!isGraphSaved){
/*                    graphResource.exportToTxt(pathFinding.getClass().
                            getSimpleName() + "_" +
                            Constants.MAP_WIDTH_SIZE / Constants.SQUARE_SIZE + "x" +
                            Constants.MAP_HEIGHT_SIZE / Constants.SQUARE_SIZE + ".txt");*/
                    isGraphSaved = true;
                }

                playerMovementManager.setPath(pathFinding.getPath());
            }
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
