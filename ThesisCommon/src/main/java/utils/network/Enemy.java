package utils.network;

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
        this.hp = hp;
        addFrame(x, y, timestamp);
    }

    public int getHp(){
        return hp;
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

    public Point getInterpolatedPosition(long renderTime) {
        if (positionHistory.size() < 2) {
            return positionHistory.getLast().toPoint();
        }

        EnemyFrame prev = null, next = null;
        for (int i = 0; i < positionHistory.size() - 1; i++) {
            EnemyFrame a = positionHistory.get(i);
            EnemyFrame b = positionHistory.get(i + 1);
            if (a.timestamp <= renderTime && b.timestamp >= renderTime) {
                prev = a;
                next = b;
                break;
            }
        }

        if (prev == null || next == null) {
            return positionHistory.getLast().toPoint();
        }

        double alpha = (renderTime - prev.timestamp) / (double)(next.timestamp - prev.timestamp);
        int interpX = (int) (prev.x + (next.x - prev.x) * alpha);
        int interpY = (int) (prev.y + (next.y - prev.y) * alpha);

        return new Point(interpX, interpY);
    }

    public void addFrame(int x, int y, long timestamp) {
        positionHistory.add(new EnemyFrame(x, y, timestamp));
        if (positionHistory.size() > 5) { // храним максимум 5 фреймов
            positionHistory.removeFirst();
        }
    }

    public void doDamage(int damage){
        hp -= damage;
    }

    public void heal (int heal){
        hp += heal;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
}
