package utils.bullets;

public class Gun {
    private final long delay;
    private final int speed;
    private final double accuracy;
    private final int magazineSize;
    private final long reloadTime;
    private final int damage;

    private int currentAmmo;
    private boolean reloading;
    private long reloadStartTime;
    private int id;

    public Gun(long delay, int speed, double accuracy, int magazineSize, long reloadTime, int damage) {
        this.damage = damage;
        this.delay = delay;
        this.speed = speed;
        this.accuracy = accuracy;
        this.magazineSize = magazineSize;
        this.reloadTime = reloadTime;

        this.currentAmmo = magazineSize; // Изначально магазин полный
        this.reloading = false;
    }

    public Gun(int id) {
        this.id = id;
        switch (id) {
            case 0: // Быстрая, слабая
                this.delay = 25;
                this.speed = 15;
                this.accuracy = 0.9;
                this.magazineSize = 50;
                this.reloadTime = 1000;
                this.damage = 5;
                break;
            case 1: // Сбалансированная
                this.delay = 50;
                this.speed = 10;
                this.accuracy = 0.85;
                this.magazineSize = 30;
                this.reloadTime = 1500;
                this.damage = 15;
                break;
            case 2: // Медленная, мощная
                this.delay = 800;
                this.speed = 25;
                this.accuracy = 0.75;
                this.magazineSize = 5;
                this.reloadTime = 2500;
                this.damage = 50;
                break;
            default:
                throw new IllegalArgumentException("Invalid gun preset ID: " + id);
        }

        this.currentAmmo = this.magazineSize;
        this.reloading = false;
    }

    public long getDelay() {
        return delay;
    }

    public int getSpeed() {
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

    @Override
    public String toString() {
        return "Gun{" +
                "delay=" + delay +
                ", speed=" + speed +
                ", accuracy=" + accuracy +
                ", magazineSize=" + magazineSize +
                ", reloadTime=" + reloadTime +
                ", damage=" + damage +
                ", currentAmmo=" + currentAmmo +
                ", reloading=" + reloading +
                ", reloadStartTime=" + reloadStartTime +
                '}';
    }

    public int getDamage(){
        return damage;
    }

    public int getId() {
        return id;
    }
}