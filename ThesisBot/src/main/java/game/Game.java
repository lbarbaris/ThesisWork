package game;

import javax.swing.*;
import java.io.IOException;

public class Game {
    private Game(){}
    public static void start() throws IOException {
        JFrame frame = new JFrame("Game Bot");
        GameController gameController = new GameController();
        gameController.setupShootingLoop();

        Timer gameTimer = new Timer(8, new GameActionListener(gameController));
        gameTimer.start();

        frame.add(gameController);
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
