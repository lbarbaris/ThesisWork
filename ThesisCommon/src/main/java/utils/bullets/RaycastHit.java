package utils.bullets;

import utils.network.Enemy;

import java.awt.*;

public class RaycastHit {
    public HitType type = HitType.NONE;
    public Enemy hitEnemy = null;
    public Point hitPoint = null;
    public double distance = Double.MAX_VALUE;

}
