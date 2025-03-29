package game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameActionListener implements ActionListener {
    private final GameController gameController;

    public GameActionListener(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameController.getPlayerMovementManager().move();
        gameController.getPlayer().getGun().updateReloadStatus();
        gameController.repaint();
    }
}
