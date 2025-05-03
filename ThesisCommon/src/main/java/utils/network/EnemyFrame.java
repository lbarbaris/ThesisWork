package utils.network;

import java.awt.*;

public class EnemyFrame {
    public int x, y;
    public long timestamp;

    public EnemyFrame(int x, int y, long timestamp) {
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    @Override
    public String toString() {
        return "EnemyFrame{" +
                "x=" + x +
                ", y=" + y +
                ", timestamp=" + timestamp +
                '}';
    }
}
