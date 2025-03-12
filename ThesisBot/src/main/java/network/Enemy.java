package network;

import java.awt.*;

public class Enemy {
    private boolean isBot;
    private Point coordinates;

    public Enemy(boolean isBot, int x, int y) {
        this.isBot = isBot;
        this.coordinates = new Point(x, y);
    }

    public boolean isBot() {
        return isBot;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Player{isBot=" + isBot + ", coordinates=" + coordinates + "}";
    }
}
