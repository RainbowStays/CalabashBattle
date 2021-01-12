package nju.zc.calabashbattle.game.bullet;

import java.io.Serializable;

import nju.zc.calabashbattle.game.data.AbstractBulletData;

public abstract class AbstractBullet implements Serializable{
    private static final long serialVersionUID = 1L;
    private static int globalID = 0;

    protected final int attackerID;
    protected final int team;
    protected final int damage;
    protected final int speed;
    protected final int id;
    protected double x,y;
    protected final double directionX, directionY;

    protected long lastUpdateTime;
    
    public AbstractBullet(int ID, int attackerID, int team, int damage, int speed, double x, double y, double directionX, double directionY){
        this.attackerID = attackerID;
        this.team = team;
        this.damage = damage;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.id = ID;
        double length = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX /= length;
        directionY /= length;
        this.directionX = directionX;
        this.directionY = directionY;
        lastUpdateTime = System.currentTimeMillis();
    }

    public AbstractBullet(int attackerID, int team, int damage, int speed, double x, double y, double directionX, double directionY){
        this.attackerID = attackerID;
        this.team = team;
        this.damage = damage;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.id = globalID++;
        double length = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX /= length;
        directionY /= length;
        this.directionX = directionX;
        this.directionY = directionY;
        lastUpdateTime = System.currentTimeMillis();
    }

    

    public int getAttackerID(){
        return this.attackerID;
    }

    public int getTeam(){
        return this.team;
    }

    public int getDamage(){
        return this.damage;
    }

    public int getSpeed(){
        return this.speed;
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    public double getDIrectionX(){
        return this.directionX;
    }

    public double getDIrectionY(){
        return this.directionY;
    }


    public long getlastUpdateTime(){
        return this.lastUpdateTime;
    }

    public void updateTime(long time){
        this.lastUpdateTime = time;
    }

    public int getID(){
        return this.id;
    }

    public void moveUpdate(){
        long currentTime = System.currentTimeMillis();
        long timeCell = currentTime - this.lastUpdateTime;
        double distance = (timeCell / 6.0) * speed;
        this.x += distance * directionX;
        this.y += distance * directionY;
        this.updateTime(currentTime);
    }

    public void externalUpdate(AbstractBulletData bData){
        x = bData.x;
        y = bData.y;
        lastUpdateTime = System.currentTimeMillis();
    }

    public abstract AbstractBulletData generateData();
    
}