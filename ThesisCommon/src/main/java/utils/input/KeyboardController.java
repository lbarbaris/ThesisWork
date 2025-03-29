package utils.input;

import utils.player.Player;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardController implements KeyListener {

    boolean up, down, left, right;
    Player player;

    public KeyboardController(Player player){
        this.player = player;
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_D) right = true;
        if (key == KeyEvent.VK_R) {
            // Инициируем перезарядку, если оружие не уже перезаряжается
            if (!player.getGun().isReloading()) {
                player.getGun().reload();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_D) right = false;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }
}
