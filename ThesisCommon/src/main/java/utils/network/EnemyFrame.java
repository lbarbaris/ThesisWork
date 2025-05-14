package utils.network;

import java.awt.*;

public class EnemyFrame {
    private int x, y;
    private long timestamp;

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
