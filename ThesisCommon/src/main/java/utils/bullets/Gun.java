package utils.bullets;

public class Gun {
    private final long delay;
    private final double speed;
    private final double accuracy;
    private final int magazineSize;
    private final long reloadTime;
    private final int damage;

    private int currentAmmo;
    private boolean reloading;
    private long reloadStartTime;

    public Gun(long delay, double speed, double accuracy, int magazineSize, long reloadTime, int damage) {
        this.damage = damage;
        this.delay = delay;
        this.speed = speed;
        this.accuracy = accuracy;
        this.magazineSize = magazineSize;
        this.reloadTime = reloadTime;

        this.currentAmmo = magazineSize; // Изначально магазин полный
        this.reloading = false;
    }

    public long getDelay() {
        return delay;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public long getReloadTime() {
        return reloadTime;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public boolean isReloading() {
        return reloading;
    }

    public double getReloadProgress() {
        if (!reloading) {
            return 0.0;
        }
        long elapsedTime = System.currentTimeMillis() - reloadStartTime;
        return Math.min(1.0, (double) elapsedTime / reloadTime);
    }


    public void reload() {
        if (!reloading) {
            reloading = true;
            reloadStartTime = System.currentTimeMillis();
        }
    }

    public void updateReloadStatus() {
        if (reloading && System.currentTimeMillis() - reloadStartTime >= reloadTime) {
            currentAmmo = magazineSize;
            reloading = false;
        }
    }

    public boolean canShoot() {
        return !reloading && currentAmmo > 0;
    }

    public void shoot() {
        if (canShoot()) {
            currentAmmo--;
        }
    }

    public int getDamage(){
        return damage;
    }
}