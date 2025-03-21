package movement;

import map.cells.Cell;
import player.Player;
import player.PlayerCameraManager;
import utils.CollisionManager;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MovementManager {
    private final Queue<MovementCommand> commandHistory = new ConcurrentLinkedQueue<>();
    private final Player player;
    private final PlayerCameraManager playerCameraManager;
    private final CollisionManager collisionManager;
    private final int squareSize;
    private final int mapWidth, mapHeight;
    private LinkedList<Point> botPath = new LinkedList<>();

    private int x, dx, y, dy; // Локальные координаты

    public MovementManager(
            int x, int y,
            Player player,
            PlayerCameraManager playerCameraManager,
            CollisionManager collisionManager,
            int squareSize,
            int mapWidth,
            int mapHeight){

        this.player = player;
        this.playerCameraManager = playerCameraManager;
        this.collisionManager = collisionManager;
        this.squareSize = squareSize;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
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
            int targetX = nextPoint.x * squareSize;
            int targetY = nextPoint.y * squareSize;

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

            nextX = Math.max(0, Math.min(nextX, mapWidth - squareSize));
            nextY = Math.max(0, Math.min(nextY, mapHeight - squareSize));

            if (!collisionManager.isWallHit(new Rectangle(nextX, nextY, squareSize, squareSize))) {
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