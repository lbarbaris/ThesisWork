package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import map.cells.Cell;
import map.paths.PathfindingAbstractClass;
import map.paths.WaveAlgorithm;
import network.Enemy;
import network.NetworkHandler;
import player.Player;
import bullets.Bullet;
import bullets.BulletManager;
import map.MapCreator;
import bullets.Gun;
import movement.MovementManager;
import utils.CollisionManager;
import player.PlayerCameraManager;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private Player targetPlayer;
    private final CollisionManager collisionManager;
    private boolean targetHit;
    private long targetHitTime;
    private final MovementManager playerMovementManager;
    private final NetworkHandler networkHandler;
    private final PathfindingAbstractClass pathFinding;

    private final int squareSize = 20;
    private final MapCreator mapCreator;

    private final CopyOnWriteArrayList<Bullet> bullets = new CopyOnWriteArrayList<>();

    private final BulletManager bulletManager;
    private final Player player;
    private final GameRenderer gameRenderer;
    private int distanceCounter;

    public GameController() throws IOException {
        playerCameraManager = new PlayerCameraManager(1000, 1000);

        Gun targetGun = new Gun(100, 8.0, 5.0, 10, 3000, (short) 10);
        targetPlayer = new Player(200, 200, targetGun);
        targetHit = false;

        mapCreator = new MapCreator((short) 3);


        pathFinding = new WaveAlgorithm(mapCreator);
        collisionManager = new CollisionManager(mapCreator.getMap());
        setFocusable(true);

        Gun defaultGun = new Gun(1000, 12.0, 3.0, 100, 2500, (short) 10);
        player = new Player(50, 50, defaultGun);

        bulletManager = new BulletManager(player, bullets, this);
        bulletManager.start();
        playerMovementManager = new MovementManager(50, 50, player, playerCameraManager, collisionManager, squareSize, 1500, 1200);

        networkHandler = new NetworkHandler("localhost", 12345, playerMovementManager);
        networkHandler.startNetworkThreads();

        gameRenderer = new GameRenderer(playerCameraManager, targetPlayer, bullets, mapCreator, playerMovementManager,  squareSize, pathFinding, networkHandler);
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

        bullets.removeIf(bullet ->
                bullet.getX() < 0 || bullet.getX() > 1500 ||
                        bullet.getY() < 0 || bullet.getY() > 1200 ||
                        collisionManager.isWallHit(new Rectangle((int) bullet.getX(), (int) bullet.getY(), 10, 10))
        );

        bullets.forEach(bullet -> {
            bullet.setX(bullet.getX() + bullet.getSpeedX());
            bullet.setY(bullet.getY() + bullet.getSpeedY());

            if (bulletManager.isBulletHitPlayer(bullet, targetPlayer, squareSize)) {
                targetHit = true;
                targetHitTime = System.currentTimeMillis();
                targetPlayer.doDamage(player.getGun().getDamage());
                if (targetPlayer.getHp() <= 0){
                    Gun targetGun = targetPlayer.getGun();
                    targetPlayer = new Player(200, 200, targetGun);
                    targetHit = false;
                }
            }
        });

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
        gameRenderer.render(g, this, targetHit, targetHitTime, player);
    }

    public MovementManager getPlayerMovementManager() {
        return playerMovementManager;
    }
}
