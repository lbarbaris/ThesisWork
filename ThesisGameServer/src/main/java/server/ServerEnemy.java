package server;

import utils.network.Enemy;

import java.util.LinkedList;

public class ServerEnemy extends Enemy {
    private int packetNumber;

    @Override
    public String toString() {
        return positionHistory.getLast().getX() + "," + positionHistory.getLast().getY() + "," + positionHistory.getLast().getTimestamp() + "," + hp + "," + packetNumber + ",";
    }

    public ServerEnemy(int x, int y, boolean isBot, int hp, int packetNumber) {
        this.positionHistory = new LinkedList<>();
        this.packetNumber = packetNumber;
        this.isBot = isBot;
        this.hp = hp;
        addFrame(x, y, System.currentTimeMillis());
    }


    public ServerEnemy getStateAt(long timestamp) {
        var pos = getInterpolatedPosition(timestamp);

        return new ServerEnemy(pos.x, pos.y, isBot, hp, packetNumber);
    }



    public int getPacketNumber() {
        return packetNumber;
    }

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

}
