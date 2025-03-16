import java.util.LinkedList;
import java.awt.Point;

public class PlayerState {
    int x, y;
    long lastProcessedTimestamp;
    boolean isBot;
    final LinkedList<EnemyFrame> positionHistory = new LinkedList<>();

    PlayerState(int x, int y, boolean isBot) {
        this.x = x;
        this.y = y;
        this.isBot = isBot;
        this.lastProcessedTimestamp = System.currentTimeMillis();
        addFrame(x, y, lastProcessedTimestamp);
    }

    void addFrame(int x, int y, long timestamp) {
        positionHistory.add(new EnemyFrame(x, y, timestamp));
        if (positionHistory.size() > 10) {
            positionHistory.removeFirst();
        }
    }

    public Point getInterpolatedPosition(long timestamp) {
        if (positionHistory.size() < 2) {
            return new Point(x, y);
        }

        EnemyFrame prev = null, next = null;

        for (int i = 0; i < positionHistory.size() - 1; i++) {
            EnemyFrame a = positionHistory.get(i);
            EnemyFrame b = positionHistory.get(i + 1);

            if (a.timestamp <= timestamp && b.timestamp >= timestamp) {
                prev = a;
                next = b;
                break;
            }
        }

        if (prev == null || next == null) {
            return new Point(x, y);
        }

        double alpha = (timestamp - prev.timestamp) / (double)(next.timestamp - prev.timestamp);
        int interpX = (int)(prev.x + (next.x - prev.x) * alpha);
        int interpY = (int)(prev.y + (next.y - prev.y) * alpha);

        return new Point(interpX, interpY);
    }
}
