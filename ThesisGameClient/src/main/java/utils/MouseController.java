package utils;

import bullets.BulletManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseController {
    public MouseController(JPanel jPanel, BulletManager bulletManager){
        jPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                bulletManager.setShooting(true);
                bulletManager.startShootingLoop();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                bulletManager.setShooting(false);
            }
        });
    }
}
