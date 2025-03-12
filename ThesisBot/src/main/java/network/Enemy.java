package network;

import java.awt.*;

public class Enemy {
    private boolean isBot;
    private Point coordinates;
    private short hp;

    public Enemy(boolean isBot, int x, int y, short hp) {
        this.isBot = isBot;
        this.coordinates = new Point(x, y);
    }

    public short getHp(){
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
}
