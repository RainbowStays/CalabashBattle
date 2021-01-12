package nju.zc.calabashbattle.game.data;

import java.io.Serializable;

import nju.zc.calabashbattle.game.bullet.AbstractBullet;

public abstract class AbstractBulletData implements Serializable{
    private static final long serialVersionUID = 1L;

    public final int team;
    public final int attackerID;
    public final int damage;
    public final int speed;
    public final int id;
    public final double x, y;
    public final double directionX, directionY;

    public long lastUpdateTime;
    

    public AbstractBulletData(int bID, int cID, int team, int damage, int speed, double x, double y, double directionX, double directionY){
        this.team = team;
        this.damage = damage;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.id = bID;
        this.attackerID = cID;
        this.directionX = directionX;
        this.directionY = directionY;
    }

    public abstract AbstractBullet generateBullet();
}