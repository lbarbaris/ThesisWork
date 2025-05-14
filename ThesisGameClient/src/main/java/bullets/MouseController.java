package bullets;

import utils.bullets.AbstractBulletManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseController {
    JPanel jPanel;
    public MouseController(JPanel jPanel, AbstractBulletManager bulletManager){
        this.jPanel = jPanel;
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

    public Point getMousePoint(){
        return jPanel.getMousePosition();
    }

}
