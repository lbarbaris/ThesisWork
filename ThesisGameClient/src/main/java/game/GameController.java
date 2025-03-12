package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import network.NetworkHandler;
import player.Player;
import bullets.Bullet;
import bullets.BulletManager;
import map.MapCreator;
import bullets.Gun;
import movement.MovementManager;
import utils.CollisionManager;
import utils.KeyboardController;
import player.PlayerCameraManager;

public class GameController extends JPanel {
    private final PlayerCameraManager playerCameraManager;
    private Player targetPlayer;
    private final CollisionManager collisionManager;
    private boolean targetHit;
    private long targetHitTime;
    private final MovementManager playerMovementManager;
    private final NetworkHandler networkHandler;

    private final int squareSize = 20;
    private final MapCreator mapCreator;

    private final CopyOnWriteArrayList<Bullet> bullets = new CopyOnWriteArrayList<>();

    private final BulletManager bulletManager;
    private final Player player;
    private final GameRenderer gameRenderer;

    public GameController() throws IOException {
        playerCameraManager = new PlayerCameraManager(1000, 1000);

        Gun targetGun = new Gun(100, 8.0, 5.0, 10, 3000, (short) 10);
        targetPlayer = new Player(200, 200, targetGun);
        targetHit = false;

        mapCreator = new MapCreator((short) 3);
        System.out.println(
                Arrays.deepToString(mapCreator.generateMapMatrix())
                        .replace("], ", "]\n")  // Перенос строки после каждой строки массива
                        .replace("[[", "[\n[")  // Перенос строки после первой скобки
                        .replace("]]", "]\n]")  // Перенос строки перед закрывающей скобкой
        );
        collisionManager = new CollisionManager(mapCreator.getMap());
        setFocusable(true);

        Gun defaultGun = new Gun(10, 12.0, 3.0, 100, 2500, (short) 10);
        player = new Player(50, 50, defaultGun);
        KeyboardController keyboardController = new KeyboardController(player);
        addKeyListener(keyboardController);

        bulletManager = new BulletManager(player, bullets, this);
        bulletManager.start();
        playerMovementManager = new MovementManager(keyboardController, 50, 50, player, playerCameraManager, collisionManager, squareSize, 1500, 1200);

        networkHandler = new NetworkHandler("localhost", 12345, playerMovementManager);
        networkHandler.startNetworkThreads();

        gameRenderer = new GameRenderer(playerCameraManager, targetPlayer, bullets, mapCreator, playerMovementManager,  networkHandler, squareSize);
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
                    this.targetPlayer.respawn(200, 200, 100);
                }
            }
        });
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
