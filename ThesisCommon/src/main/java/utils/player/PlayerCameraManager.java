package utils.player;

public class PlayerCameraManager {
    private int cameraX, cameraY;
    private final int screenWidth, screenHeight;

    public PlayerCameraManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void updateCamera(int x, int y) {
        int halfScreenWidth = (int) (screenWidth / 1.5);
        int halfScreenHeight = (int) (screenHeight / 1.5);

        if (x > cameraX + halfScreenWidth) {
            cameraX = x - halfScreenWidth;
        } else if (x < cameraX + halfScreenWidth) {
            cameraX = Math.max(0, x - halfScreenWidth);
        }

        if (y > cameraY + halfScreenHeight) {
            cameraY = y - halfScreenHeight;
        } else if (y < cameraY + halfScreenHeight) {
            cameraY = Math.max(0, y - halfScreenHeight);
        }
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }
}

