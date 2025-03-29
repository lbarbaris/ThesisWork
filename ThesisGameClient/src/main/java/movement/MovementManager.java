package movement;



import utils.Constants;
import utils.input.KeyboardController;
import utils.map.CollisionManager;
import utils.player.Player;
import utils.player.PlayerCameraManager;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MovementManager {
    private final KeyboardController keyboardController;
    private final Queue <MovementCommand> commandHistory = new ConcurrentLinkedQueue<>();
    private final Player player;
    private final PlayerCameraManager playerCameraManager;
    private final CollisionManager collisionManager;

    private int x, dx, y, dy; // Локальные координаты

    public MovementManager(
            KeyboardController keyboardController,
            int x, int y,
            Player player,
            PlayerCameraManager playerCameraManager,
            CollisionManager collisionManager){

        this.keyboardController = keyboardController;
        this.player = player;
        this.playerCameraManager = playerCameraManager;
        this.collisionManager = collisionManager;
        this.x = x;
        this.y = y;

    }




    public void addCommand(){
        commandHistory.add(new MovementCommand(dx, dy, System.currentTimeMillis()));
    }


    public void move() {
        int speed = 5;

        dx = 0;
        dy = 0;

        // Предсказание
        for (int i = 0; i < speed; i++) {
            int nextX = x;
            int nextY = y;

            if (keyboardController.isUp()) dy = -1;
            if (keyboardController.isDown()) dy = 1;
            if (keyboardController.isLeft()) dx = -1;
            if (keyboardController.isRight()) dx = 1;

            if (dx != 0 && dy != 0) {
                nextX += (int) (dx * 2 / Math.sqrt(2));
                nextY += (int) (dy * 2 / Math.sqrt(2));
            } else {
                nextX += dx;
                nextY += dy;
            }

            nextX = Math.max(0, Math.min(nextX, Constants.MAP_WIDTH_SIZE - Constants.SQUARE_SIZE));
            nextY = Math.max(0, Math.min(nextY, Constants.MAP_HEIGHT_SIZE - Constants.SQUARE_SIZE));

            if (!collisionManager.isWallHit(new Rectangle(nextX, nextY, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE))) {
                x = nextX;
                y = nextY;
            }
        }

        // Обновляем координаты игрока и камеры
        player.setPosition(x, y);
        playerCameraManager.updateCamera(x, y);
        player.setCameraPosition(playerCameraManager.getCameraX(), playerCameraManager.getCameraY());
    }

    public void applyServerData(int serverX, int serverY, long serverTimestamp){
        x = serverX;
        y = serverY;


        // Очищаем обработанные сервером команды
        commandHistory.removeIf(cmd -> cmd.timestamp <= serverTimestamp);
    }

    public int[] getCoords(){
        return new int[] {x, dx, y, dy};
    }

    public int getX() {
        return x;
    }

    public int getY(){
        return y;
    }
}