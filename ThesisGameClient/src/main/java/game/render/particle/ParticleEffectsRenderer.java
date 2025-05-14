package game.render.particle;

import utils.bullets.AbstractBulletManager;
import bullets.AdvancedBulletManager;
import utils.bullets.Bullet;
import bullets.BulletManager;
import utils.Constants;
import utils.bullets.Particle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ParticleEffectsRenderer {
    private final List<ShotTrailParticle> shotTrails = new ArrayList<>();
    private final List<AirParticle> airParticles = new ArrayList<>();
    private long lastParticleSpawnTime;

    public ParticleEffectsRenderer() {
    }

    public void renderAdvancedBullets(AbstractBulletManager bulletManager, Graphics2D g2){
        var advancedBullets = (AdvancedBulletManager) bulletManager;
        for (Bullet bullet : advancedBullets.getBullets()) {
            Point pos = bullet.getPosition();
            g2.setColor(Color.YELLOW);
            g2.fillOval(pos.x, pos.y, 4, 4);
        }
    }

    public void renderEffects(Graphics2D g2, Point playerPosition, AbstractBulletManager bulletManager) {

        if (bulletManager.getClass().equals(AdvancedBulletManager.class)){
            var advancedBM = (AdvancedBulletManager) bulletManager;

            renderAdvancedBullets(advancedBM, g2);

        } else if (bulletManager.getClass().equals(BulletManager.class)) {
            var basicBM = (BulletManager) bulletManager;
            Point playerCenter = new Point(
                    playerPosition.x + Constants.SQUARE_SIZE /2,
                    playerPosition.y + Constants.SQUARE_SIZE/2
            );
            if (basicBM.getOneShot() && basicBM.getLastHitPoint() != null) {
                addShotTrail(playerCenter, basicBM.getLastHitPoint());
            }

            renderShotTrails(g2);


        }
        renderBulletParticles(g2, bulletManager.getParticles());

        renderAirParticles(g2);
    }

    private void addShotTrail(Point start, Point end) {
        shotTrails.add(new ShotTrailParticle(start, end, System.currentTimeMillis()));
    }

    private void renderBulletParticles(Graphics2D g2, List<Particle> particles) {
        for (Particle particle : particles) {
            particle.update();
            particle.render(g2);
        }
    }

    public void renderShotTrails(Graphics2D g2) {

        shotTrails.removeIf(trail -> !trail.isAlive(System.currentTimeMillis()));

        for (ShotTrailParticle trail : shotTrails) {
            float progress = trail.getProgress(System.currentTimeMillis());
            int colorValue = 255 - (int)(220 * progress);
            int alpha = 255 - (int)(255 * progress);

            Color trailColor = new Color(255, Math.max(50, colorValue), 0, alpha);
            g2.setStroke(new BasicStroke(4 - (3 * progress)));
            g2.setColor(trailColor);
            g2.drawLine(trail.getStart().x, trail.getStart().y, trail.getEnd().x, trail.getEnd().y);
            g2.setStroke(new BasicStroke(3));
        }
    }

    public void renderAirParticles(Graphics2D g2) {
        airParticles.removeIf(particle -> !particle.isAlive(System.currentTimeMillis()));
        for (AirParticle particle : airParticles) {
            particle.update();
            particle.render(g2);
        }
    }

    public void createMovementParticles(Point playerPosition, float deltaX, float deltaY) {
        long currentTime = System.currentTimeMillis();
        if ((deltaX != 0 || deltaY != 0) && currentTime - lastParticleSpawnTime > 30) {
            var sqrt = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            var particleCount = 2 + (int)(sqrt / 2);

            var dirX = -deltaX / (float) sqrt;
            var dirY = -deltaY / (float) sqrt;
            var centerX = playerPosition.x + Constants.SQUARE_SIZE/2;
            var centerY = playerPosition.y + Constants.SQUARE_SIZE/2;

            for (int i = 0; i < particleCount; i++) {
                airParticles.add(new AirParticle(centerX, centerY, dirX, dirY));
            }
            lastParticleSpawnTime = currentTime;
        }
    }
}
