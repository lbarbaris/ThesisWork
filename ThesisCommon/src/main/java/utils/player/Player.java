package utils.player;

import utils.bullets.Gun;

public class Player {
    private int x, y; // Позиция игрока
    private int cameraX, cameraY; // Камера
    private Gun gun; // Текущее оружие игрока

    private int hp;

    public Player(int startX, int startY, Gun gun) {
        this.x = startX;
        this.y = startY;
        this.gun = gun;
        this.hp = 100;
    }

    public int getHp(){
        return hp;
    }

    public String getHpString(){
        return String.valueOf(hp);
    }

    public void doDamage(int damage){
        hp -= damage;
    }

    public void heal (int heal){
        hp += heal;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    public void respawn(int x, int y, int hp){
        heal(hp);
        setPosition(x, y);
    }

    public void setCameraPosition(int cameraX, int cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public Gun getGun() {
        return gun;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
}

