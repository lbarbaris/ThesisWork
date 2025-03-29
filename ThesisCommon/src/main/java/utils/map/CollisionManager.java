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
        for (Shape shape : map) {
            if (shape.intersects((Rectangle2D) bullet)) {
                res = true;
                break;
            }
        }
        return res;
    }
}
