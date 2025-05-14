package utils.bullets;

import java.awt.*;

public class Bullet {
    private final double startX, startY;
    private double x, y;
    private final double dx, dy;
    private final int speed;
    private final int damage;

    public Bullet(double x, double y, double angle, int damage, int speed) {
        this.startX = x;
        this.startY = y;
        this.x = x;
        this.y = y;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
        this.damage = damage;
        this.speed = speed;
    }

    public Bullet(double x, double y, double dx, double dy, int damage, int speed){
        this.startX = x;
        this.startY = y;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.damage = damage;
        this.speed = speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, 6, 6); // Размер пули
    }

    public Point getPosition() {
        return new Point((int) x, (int) y);
    }

    public int getDamage() {
        return damage;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }
}
