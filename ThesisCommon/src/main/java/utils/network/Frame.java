package utils.network;

public class Frame {
    private final int x;
    private final int y;
    private final boolean isBot;
    private int hp;
    private final int frameNumber;

    public Frame(int[] movementData, boolean isBot, int hp, int frameNumber) {
        this.frameNumber = frameNumber;
        this.x = movementData[0];
        this.y = movementData[2];
        this.isBot = isBot;
        this.hp = hp;
    }

    @Override
    public String toString() {
        return x +
                "," +
               y +
                "," +
               System.currentTimeMillis() +
                "," +
               isBot +
                "," +
               hp +
               "," +
               frameNumber;
    }
}
