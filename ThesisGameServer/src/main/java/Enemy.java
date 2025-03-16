import java.awt.*;
import java.util.LinkedList;

public class Enemy {
    private boolean isBot;
    private Point coordinates;
    private int hp;
    private final LinkedList<EnemyFrame> positionHistory = new LinkedList<>();

    public Enemy(boolean isBot, int x, int y, int hp, long timestamp) {
        this.isBot = isBot;
        this.coordinates = new Point(x, y);
        addFrame(x, y, timestamp);
    }

    public int getHp(){
        return hp;
    }

    public String getHpString(){
        return String.valueOf(hp);
    }

    public boolean isBot() {
        return isBot;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Enemy { isBot=" + isBot + ", coordinates=" + coordinates + ", hp=" + hp + "}";
    }


    public void addFrame(int x, int y, long timestamp) {
        positionHistory.add(new EnemyFrame(x, y, timestamp));
        if (positionHistory.size() > 5) { // храним максимум 5 фреймов
            positionHistory.removeFirst();
        }
    }
}
