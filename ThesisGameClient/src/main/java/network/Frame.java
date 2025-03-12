package network;

public class Frame {
    private final int x;
    private final int dx;
    private final int y;
    private final int dy;
    private final boolean isBot;

    public Frame(int[] movementData, boolean isBot) {
        this.x = movementData[0];
        this.dx = movementData[1];
        this.y = movementData[2];
        this.dy = movementData[3];
        this.isBot = isBot;
    }

    @Override
    public String toString() {
        return dx +
                "," +
               dy +
                "," +
               x +
                "," +
               y +
                "," +
               System.currentTimeMillis() +
                "," +
               isBot;
    }
}
