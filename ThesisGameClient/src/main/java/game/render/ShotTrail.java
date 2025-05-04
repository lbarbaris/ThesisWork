package game.render;

import java.awt.*;

public class ShotTrail {
    private Point start;
    private Point end;
    private long shotTime;

    public ShotTrail(Point start, Point end, long shotTime) {
        this.start = start;
        this.end = end;
        this.shotTime = shotTime;
    }

    public float getProgress(long currentTime) {
        return (currentTime - shotTime) / 3000.0f;
    }

    public boolean isAlive(long currentTime) {
        return currentTime - shotTime <= 3000;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public long getShotTime() {
        return shotTime;
    }
}
