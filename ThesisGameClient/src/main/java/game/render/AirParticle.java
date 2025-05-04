package game.render;

import java.awt.*;
import java.util.Random;

public class AirParticle {
    private final int x, y;
    private final float size;
    private final Color color;
    private final long creationTime;
    private final float maxLifetime;
    private float dx;
    private float dy;

    public AirParticle(int x, int y, float directionX, float directionY) {
        Random rand = new Random();
        this.x = x;
        this.y = y;
        this.size = 2 + rand.nextFloat() * 3; // Размер от 2 до 5
        this.color = new Color(200, 230, 255, 200); // Голубоватый полупрозрачный
        this.creationTime = System.currentTimeMillis();
        this.maxLifetime = 300 + rand.nextFloat() * 200; // Время жизни 300-500 мс
        this.dx = directionX * (0.2f + rand.nextFloat() * 0.3f);
        this.dy = directionY * (0.2f + rand.nextFloat() * 0.3f);
    }

    public boolean isAlive(long currentTime) {
        return (currentTime - creationTime) < maxLifetime;
    }

    public void update() {
        // Движение частицы с замедлением
        dx *= 0.95f;
        dy *= 0.95f;
    }

    public void render(Graphics2D g2) {
        long currentTime = System.currentTimeMillis();
        float lifeProgress = (currentTime - creationTime) / maxLifetime;

        // Прозрачность уменьшается со временем
        int alpha = (int) (150 * (1 - lifeProgress * lifeProgress)); // Квадратичное затухание
        Color renderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        g2.setColor(renderColor);
        g2.fillOval(
                (int) (x + dx * lifeProgress * 10 - size/2),
                (int) (y + dy * lifeProgress * 10 - size/2),
                (int) size,
                (int) size
        );
    }
}
