package utils.network;

import utils.graphs.GraphResource;

import java.awt.*;
import java.util.LinkedList;

import static utils.Constants.*;

public class ClientEnemy extends Enemy {
    private final GraphResource<Double> interpolatedDotsGraph;
    private final ExtrapolationDotsCounter extrapolationDotsCounter;
    private int interpolatedDotsCounter;
    private boolean isInterpolatedSaved;

    public ClientEnemy(boolean isBot, int x, int y, int hp) {
        this.positionHistory = new LinkedList<>();
        this.extrapolationDotsCounter = new ExtrapolationDotsCounter();
        this.interpolatedDotsGraph = new GraphResource<>("Тест интерполяции на " + INTERPOLATION_DELAY_MS + "мс", "количество замеров", "отклонение по расстоянию");
        this.isBot = isBot;
        this.hp = hp;
        interpolatedDotsGraph.addSeries();
        interpolatedDotsGraph.addSeries();
        interpolatedDotsCounter = 0;
        addFrame(x, y, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "Enemy{" +
                "isBot=" + isBot +
                ", hp=" + hp +
                ", positionHistory=" + positionHistory +
                '}';
    }
}
