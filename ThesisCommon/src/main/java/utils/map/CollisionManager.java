package utils.map;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CollisionManager {
    private final ArrayList<Block> map;

    public CollisionManager(ArrayList<Block> map) {
        this.map = map;
    }

    public boolean isWallHit(Shape bullet){
        boolean res = false;
        for (Block block : map) {
            if (block.intersects((Rectangle2D) bullet) && !block.isAir()) {
                res = true;
                break;
            }
        }
        return res;
    }
}
