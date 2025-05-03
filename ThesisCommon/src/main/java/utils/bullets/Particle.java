package utils.bullets;

import java.awt.*;

public class Particle {
    private final Color color;
    private final Point position;
    private final Point velocity;
    private final long creationTime;
    private final int lifetime; // в миллисекундах
    private final int size;

    public Particle(Color color, Point position, Point velocity, int lifetime, int size) {
        this.color = color;
        this.position = position;
        this.velocity = velocity;
        this.creationTime = System.currentTimeMillis();
        this.lifetime = lifetime;
        this.size = size;
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - creationTime < lifetime;
    }

    public void update() {
        position.x += velocity.x;
        position.y += velocity.y;
    }

    public void render(Graphics2D g2) {
        float progress = (System.currentTimeMillis() - creationTime) / (float)lifetime;
        int alpha = (int)(255 * (1 - progress * progress)); // Квадратичное затухание
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2.fillRect(position.x, position.y, size, size);
    }
}
