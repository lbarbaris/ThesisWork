import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GameRenderer extends JPanel {
    private final MapCreator mapCreator;
    private final Map<String, SimpleGameServer.PlayerState> playerStates;

    public GameRenderer(MapCreator mapCreator, Map<String, SimpleGameServer.PlayerState> playerStates) {
        this.mapCreator = mapCreator;
        this.playerStates = playerStates;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Отрисовка карты
        g2.setColor(Color.GRAY);
        for (Shape shape : mapCreator.getMap()) {
            g2.fillRect(shape.getBounds().x, shape.getBounds().y, shape.getBounds().width, shape.getBounds().height);
        }

        // Отрисовка игроков

        for (SimpleGameServer.PlayerState state : playerStates.values()) {
            if (!state.isBot){
                g2.setColor(Color.BLUE);
                g2.fillRect(state.x, state.y, 20, 20);
            }
            else {
                g2.setColor(Color.GREEN);
                g2.fillRect(state.x, state.y, 20, 20);
            }

        }
    }
}

