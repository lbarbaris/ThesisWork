package player;

import bullets.Gun;

public class Player {
    private double x, y; // Позиция игрока
    private int cameraX, cameraY; // Камера
    private Gun gun; // Текущее оружие игрока
    private short hp;

    public Player(double startX, double startY, Gun gun) {
        this.x = startX;
        this.y = startY;
        this.gun = gun;
        this.hp = 100;
    }

    public short getHp(){
        return hp;
    }

    public String getHpString(){
        return String.valueOf(hp);
    }

    public void doDamage(short damage){
        hp = (short) (hp - damage);
    }

    public void heal (short heal){
        hp = (short) (hp + heal);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    public void setCameraPosition(int cameraX, int cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public Gun getGun() {
        return gun;
    }
}

