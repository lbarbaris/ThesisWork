package movement;

import map.cells.Cell;
import utils.Constants;
import utils.player.Player;
import utils.player.PlayerCameraManager;
import utils.map.CollisionManager;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MovementManager {
    private final Queue<MovementCommand> commandHistory = new ConcurrentLinkedQueue<>();
    private final Player player;
    private final PlayerCameraManager playerCameraManager;
    private final CollisionManager collisionManager;
    private LinkedList<Point> botPath = new LinkedList<>();

    private int x, dx, y, dy; // Локальные координаты

    public MovementManager(
            int x, int y,
            Player player,
            PlayerCameraManager playerCameraManager,
            CollisionManager collisionManager){

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
        int speed = 2;
        dx = 0;
        dy = 0;

       if (!botPath.isEmpty() ) {
            Point nextPoint = botPath.getFirst();
            int targetX = nextPoint.x * Constants.SQUARE_SIZE;
            int targetY = nextPoint.y * Constants.SQUARE_SIZE;

            if (x < targetX) dx = 1;
            if (x > targetX) dx = -1;
            if (y < targetY) dy = 1;
            if (y > targetY) dy = -1;

            if (x == targetX && y == targetY) {
                botPath.removeFirst();
            }
        }

        for (int i = 0; i < speed; i++) {
            int nextX = x + dx;
            int nextY = y + dy;

            nextX = Math.max(0, Math.min(nextX, Constants.MAP_WIDTH_SIZE - Constants.SQUARE_SIZE));
            nextY = Math.max(0, Math.min(nextY, Constants.MAP_HEIGHT_SIZE - Constants.SQUARE_SIZE));

            if (!collisionManager.isWallHit(new Rectangle(nextX, nextY, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE))) {
                x = nextX;
                y = nextY;
            }
        }

        player.setPosition(x, y);
        playerCameraManager.updateCamera(x, y);
    }

    public void applyServerData(int serverX, int serverY, long serverTimestamp){
        x = serverX;
        y = serverY;

        // Очищаем обработанные сервером команды
        commandHistory.removeIf(cmd -> cmd.timestamp <= serverTimestamp);
    }

    public void setPath(LinkedList<Point> path) {
        this.botPath = path;
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

    public Point getCell(){
        return new Cell(x, y);
    }
}