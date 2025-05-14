package utils.network;

import java.awt.*;
import java.util.LinkedList;

public class Enemy {
    protected boolean isBot;
    protected int hp;

    protected LinkedList<EnemyFrame> positionHistory;

    public void addFrame(int x, int y, long timestamp) {
        positionHistory.add(new EnemyFrame(x, y, timestamp));
        if (positionHistory.size() > 10) { // храним максимум 5 фреймов
            positionHistory.removeFirst();
        }
    }

    public Point getInterpolatedPosition(long renderTime) {
        if (positionHistory.size() < 2) {
            return positionHistory.getLast().toPoint();
        }

        EnemyFrame prev = null, next = null;
        for (int i = 0; i < positionHistory.size() - 1; i++) {
            var a = positionHistory.get(i);
            var b = positionHistory.get(i + 1);
            if (a.getTimestamp() <= renderTime && b.getTimestamp() >= renderTime) {
                prev = a;
                next = b;
                break;
            }
        }

        if (prev == null || next == null) {
            return positionHistory.getLast().toPoint();
        }

        var alpha = (renderTime - prev.getTimestamp()) / (double)(next.getTimestamp() - prev.getTimestamp());
        var interpX = (int) (prev.getX() + (next.getX() - prev.getX()) * alpha);
        var interpY = (int) (prev.getY() + (next.getY() - prev.getY()) * alpha);

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



    public Point getPredictedPosition(long lookaheadTimeMs) {
        var latest = positionHistory.getLast();

        var previous = positionHistory.getLast();

        for (var i = positionHistory.size() - 2; i >= 0; i--) {
            var candidate = positionHistory.get(i);
            if (candidate.getX() != latest.getX() || candidate.getY() != latest.getY()) {
                previous = candidate;
                break;
            }
        }

        var dt = latest.getTimestamp() - previous.getTimestamp();
        if (dt == 0) {
            return latest.toPoint();
        }

        var dx = latest.getX() - previous.getX();
        var dy = latest.getY() - previous.getY();

        var velocityX = dx / dt;
        var velocityY = dy / dt;

        var predictedX = (int) (latest.getX() + velocityX * lookaheadTimeMs);
        var predictedY = (int) (latest.getY() + velocityY * lookaheadTimeMs);

        return new Point(predictedX, predictedY);
    }

    public Point getPredictedPositionWithAcceleration(long lookaheadTimeMs) {
        if (positionHistory.size() < 3) {
            return getPredictedPosition(lookaheadTimeMs); // fallback
        }

        var a = positionHistory.get(positionHistory.size() - 3);
        var b = positionHistory.get(positionHistory.size() - 2);
        var c = positionHistory.getLast();

        var dt1 = b.getTimestamp() - a.getTimestamp();
        var dt2 = c.getTimestamp() - b.getTimestamp();

        if (dt1 <= 0 || dt2 <= 0) {
            return c.toPoint();
        }

        var vx1 = (b.getX() - a.getX()) / dt1;
        var vy1 = (b.getY() - a.getY()) / dt1;
        var vx2 = (c.getX() - b.getX()) / dt2;
        var vy2 = (c.getY() - b.getY()) / dt2;

        var ax = (vx2 - vx1) / dt2;
        var ay = (vy2 - vy1) / dt2;

        var predictedX = c.getX() + vx2 * lookaheadTimeMs + 0.5 * ax * lookaheadTimeMs * lookaheadTimeMs;
        var predictedY = c.getY() + vy2 * lookaheadTimeMs + 0.5 * ay * lookaheadTimeMs * lookaheadTimeMs;

        return new Point((int)predictedX, (int)predictedY);
    }

    public Point getSmartPredictedPosition(long currentTime, long lookaheadTimeMs) {
        var n = positionHistory.size();
        if (n < 3) return getPredictedPosition(lookaheadTimeMs);

        var t = new double[n];
        var x = new double[n];
        var y = new double[n];

        for (int i = 0; i < n; i++) {
            var frame = positionHistory.get(i);
            // нормализуем время относительно currentTime
            t[i] = frame.getTimestamp() - currentTime;
            x[i] = frame.getX();
            y[i] = frame.getY();
        }

        var xFit = new PolynomialFit(t, x, 3);
        var yFit = new PolynomialFit(t, y, 3);

        var futureT = lookaheadTimeMs; // через сколько мс от текущего времени
        var predictedX = xFit.predict(futureT);
        var predictedY = yFit.predict(futureT);

        return new Point((int) predictedX, (int) predictedY);
    }

    public Point getPadeSmartPredictedPosition(long currentTime, long lookaheadTimeMs) {
        var n = positionHistory.size();
        if (n < 5) return getPredictedPosition(lookaheadTimeMs);

        var t = new double[n];
        var x = new double[n];
        var y = new double[n];

        for (var i = 0; i < n; i++) {
            EnemyFrame frame = positionHistory.get(i);
            t[i] = frame.getTimestamp() - currentTime;
            x[i] = frame.getX();
            y[i] = frame.getY();
        }

        var xFit = new PadeApproximator(t, x);
        var yFit = new PadeApproximator(t, y);

        var futureT = lookaheadTimeMs;
        var predictedX = xFit.predict(futureT);
        var predictedY = yFit.predict(futureT);

        return new Point((int) predictedX, (int) predictedY);
    }

    public int getHp(){
        return hp;
    }

    public boolean isBot() {
        return isBot;
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

    public EnemyFrame getLast(){
        return positionHistory.getLast();
    }
}
