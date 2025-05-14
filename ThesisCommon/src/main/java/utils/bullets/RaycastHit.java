package utils.bullets;

import utils.network.ClientEnemy;

import java.awt.*;

public class RaycastHit {
    public HitType type = HitType.NONE;
    public ClientEnemy hitClientEnemy = null;
    public Point hitPoint = null;
    public double distance = Double.MAX_VALUE;

}
