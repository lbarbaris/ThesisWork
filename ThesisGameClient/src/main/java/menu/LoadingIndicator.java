package menu;

import javax.swing.*;
import java.awt.*;

public class LoadingIndicator extends JComponent {
    private int angle = 0;
    private final Timer timer;

    public LoadingIndicator() {
        setPreferredSize(new Dimension(30, 30));
        timer = new Timer(10, e -> {
            angle = (angle + 3) % 360;
            repaint();
        });
    }

    public void start() {
        timer.start();
        setVisible(true);
    }

    public void stop() {
        timer.stop();
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = Math.min(getWidth(), getHeight()) - 4;
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.ORANGE); // более заметный цвет
        g2.setStroke(new BasicStroke(3));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        g2.drawArc(x, y, size, size, angle, 270);
    }
}
