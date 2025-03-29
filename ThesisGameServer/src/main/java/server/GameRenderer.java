package server;

import utils.map.MapCreator;

import javax.swing.*;
import java.awt.*;
import java.util.Map;


public class GameRenderer extends JPanel {
    private final MapCreator mapCreator;
    private final Map<String, Enemy2> playerStates;

    public GameRenderer(MapCreator mapCreator, Map<String, Enemy2> playerStates) {
        this.mapCreator = mapCreator;
        this.playerStates = playerStates;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        renderMap(g2);

        renderPlayerStates(g2);

    }

    private void renderMap(Graphics2D g2){
        g2.setColor(Color.GRAY);
        for (Shape shape : mapCreator.getMap()) {
            g2.fillRect(shape.getBounds().x, shape.getBounds().y, shape.getBounds().width, shape.getBounds().height);
        }
    }

    private void renderPlayerStates(Graphics2D g2){
        long renderTime = getRenderTimestamp();

        for (Enemy2 state : playerStates.values()) {
            Point interpolated = state.getInterpolatedPosition(renderTime);

            if (!state.isBot){
                g2.setColor(Color.BLUE);
            } else {
                g2.setColor(Color.GREEN);
            }

            g2.fillRect(interpolated.x, interpolated.y, 20, 20);
        }
    }

    private long getRenderTimestamp() {
        return System.currentTimeMillis() - 100; // ≈100 мс задержка для интерполяции
    }
}

