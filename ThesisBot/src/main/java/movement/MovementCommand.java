package movement;

public class MovementCommand {
    int dx, dy;
    long timestamp;

    public MovementCommand(int dx, int dy, long timestamp) {
        this.dx = dx;
        this.dy = dy;
        this.timestamp = timestamp;
    }
}
