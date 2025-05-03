package utils.network;

import utils.graphs.GraphResource;

import java.awt.*;
import java.util.LinkedList;

import static utils.Constants.*;

public class Enemy {
    private boolean isBot;
    private Point coordinates;
    private int hp;
    private final LinkedList<EnemyFrame> positionHistory = new LinkedList<>();
    private final GraphResource<Double> interpolatedDotsGraph;
    private final ExtrapolationDotsCounter extrapolationDotsCounter;
    private int interpolatedDotsCounter;
    private boolean isInterpolatedSaved;



    public Enemy(boolean isBot, int x, int y, int hp, long timestamp) {
        this.extrapolationDotsCounter = new ExtrapolationDotsCounter();
        this.interpolatedDotsGraph = new GraphResource<>("Тест интерполяции на " + INTERPOLATION_DELAY_MS + "мс", "количество замеров", "отклонение по расстоянию");
        this.isBot = isBot;
        this.coordinates = new Point(x, y);
        this.hp = hp;
        interpolatedDotsGraph.addSeries();
        interpolatedDotsGraph.addSeries();
        interpolatedDotsCounter = 0;
        addFrame(x, y, timestamp);
    }

    public int getHp(){
        return hp;
    }

    public boolean isBot() {
        return isBot;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Enemy { isBot=" + isBot + ", coordinates=" + coordinates + ", hp=" + hp + "}";
    }

    public Point getInterpolatedPosition(long renderTime) {
        if (positionHistory.size() < 2) {
            return positionHistory.getLast().toPoint();
        }
        var diff = 0L;

        EnemyFrame prev = null, next = null;
        for (int i = 0; i < positionHistory.size() - 1; i++) {
            EnemyFrame a = positionHistory.get(i);
            EnemyFrame b = positionHistory.get(i + 1);
            if (a.timestamp <= renderTime && b.timestamp >= renderTime) {
                prev = a;
                next = b;
                diff = b.timestamp - a.timestamp;
                break;
            }
        }

        if (prev == null || next == null) {
            return positionHistory.getLast().toPoint();
        }

        double alpha = (renderTime - prev.timestamp) / (double)(next.timestamp - prev.timestamp);
        int interpX = (int) (prev.x + (next.x - prev.x) * alpha);
        int interpY = (int) (prev.y + (next.y - prev.y) * alpha);

/*        if (diff % 50 == 0) {
            if (interpolatedDotsCounter < PREDICT_SIZE) {
                var point = new Point(interpX, interpY);
                var distance = extrapolationDotsCounter.getDistanceToNearestPoint(point);
                interpolatedDotsGraph.addValue(0, (double) interpolatedDotsCounter);
                interpolatedDotsGraph.addValue(1, distance);
                interpolatedDotsCounter++;
            } else if (!isInterpolatedSaved) {
                isInterpolatedSaved = true;
                interpolatedDotsGraph.exportToTxt(interpolatedDotsGraph.getTitle() + ".txt");
                System.exit(0);
            }
        }*/
        return new Point(interpX, interpY);
    }

    public void addFrame(int x, int y, long timestamp) {
        positionHistory.add(new EnemyFrame(x, y, timestamp));
        if (positionHistory.size() > 5) { // храним максимум 5 фреймов
            positionHistory.removeFirst();
        }
    }

    public Point getPredictedPosition(long currentTime, long lookaheadTimeMs) {
        EnemyFrame latest = positionHistory.getLast();

        EnemyFrame previous = positionHistory.getLast();

        for (int i = positionHistory.size() - 2; i >= 0; i--) {
            EnemyFrame candidate = positionHistory.get(i);
            if (candidate.x != latest.x || candidate.y != latest.y) {
                previous = candidate;
                break;
            }
        }

        long dt = latest.timestamp - previous.timestamp;
        if (dt == 0) {
            return latest.toPoint(); // нет движения
        }

        double dx = latest.x - previous.x;
        double dy = latest.y - previous.y;

        double velocityX = dx / dt;
        double velocityY = dy / dt;

        int predictedX = (int) (latest.x + velocityX * lookaheadTimeMs);
        int predictedY = (int) (latest.y + velocityY * lookaheadTimeMs);

        return new Point(predictedX, predictedY);
    }


    public Point getPredictedPositionWithAcceleration(long currentTime, long lookaheadTimeMs) {
        if (positionHistory.size() < 3) {
            return getPredictedPosition(currentTime, lookaheadTimeMs); // fallback
        }

        EnemyFrame a = positionHistory.get(positionHistory.size() - 3);
        EnemyFrame b = positionHistory.get(positionHistory.size() - 2);
        EnemyFrame c = positionHistory.getLast();

        double dt1 = b.timestamp - a.timestamp;
        double dt2 = c.timestamp - b.timestamp;

        if (dt1 <= 0 || dt2 <= 0) {
            return c.toPoint();
        }

        double vx1 = (b.x - a.x) / dt1;
        double vy1 = (b.y - a.y) / dt1;
        double vx2 = (c.x - b.x) / dt2;
        double vy2 = (c.y - b.y) / dt2;

        double ax = (vx2 - vx1) / dt2;
        double ay = (vy2 - vy1) / dt2;

        double predictedX = c.x + vx2 * lookaheadTimeMs + 0.5 * ax * lookaheadTimeMs * lookaheadTimeMs;
        double predictedY = c.y + vy2 * lookaheadTimeMs + 0.5 * ay * lookaheadTimeMs * lookaheadTimeMs;

        return new Point((int)predictedX, (int)predictedY);
    }

    public long getHistoryTimeSpan() {
        return positionHistory.getLast().timestamp - positionHistory.getFirst().timestamp;
    }


    public Point getSmartPredictedPosition(long currentTime, long lookaheadTimeMs) {
        int n = positionHistory.size();
        if (n < 3) return getPredictedPosition(currentTime, lookaheadTimeMs);

        double[] t = new double[n];
        double[] x = new double[n];
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            EnemyFrame frame = positionHistory.get(i);
            // нормализуем время относительно currentTime
            t[i] = frame.timestamp - currentTime;
            x[i] = frame.x;
            y[i] = frame.y;
        }

        PolynomialFit xFit = new PolynomialFit(t, x, 3);
        PolynomialFit yFit = new PolynomialFit(t, y, 3);

        double futureT = lookaheadTimeMs; // через сколько мс от текущего времени
        double predictedX = xFit.predict(futureT);
        double predictedY = yFit.predict(futureT);

        return new Point((int) predictedX, (int) predictedY);
    }

    public Point getPadeSmartPredictedPosition(long currentTime, long lookaheadTimeMs) {
        int n = positionHistory.size();
        if (n < 5) return getPredictedPosition(currentTime, lookaheadTimeMs); // Padé(2,2) требует хотя бы 5 точек

        double[] t = new double[n];
        double[] x = new double[n];
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            EnemyFrame frame = positionHistory.get(i);
            t[i] = frame.timestamp - currentTime;
            x[i] = frame.x;
            y[i] = frame.y;
        }

        PadeApproximator xFit = new PadeApproximator(t, x);
        PadeApproximator yFit = new PadeApproximator(t, y);

        double futureT = lookaheadTimeMs;
        double predictedX = xFit.predict(futureT);
        double predictedY = yFit.predict(futureT);

        return new Point((int) predictedX, (int) predictedY);
    }


    public void doDamage(int damage){
        hp -= damage;
    }

    public void heal (int heal){
        hp += heal;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
}
