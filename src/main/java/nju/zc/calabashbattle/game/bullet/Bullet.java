package nju.zc.calabashbattle.game.bullet;

import nju.zc.calabashbattle.game.data.AbstractBulletData;
import nju.zc.calabashbattle.game.data.BulletData;

public class Bullet extends AbstractBullet{
    private static final long serialVersionUID = 1L;
    public Bullet(int attackerID, int team, int damage, int speed, double x, double y, double directionX, double directionY){
        super(attackerID, team, damage, speed, x, y, directionX, directionY);
    }

    public Bullet(BulletData bData){
        super(bData.attackerID, bData.team, bData.damage, bData.speed, bData.x, bData.y, bData.directionX, bData.directionY);
    }

    public Bullet(int ID, BulletData bData){
        super(ID, bData.attackerID, bData.team, bData.damage, bData.speed, bData.x, bData.y, bData.directionX, bData.directionY);
    }

    @Override
    public AbstractBulletData generateData(){
        moveUpdate();//重要
        return new BulletData(id, attackerID, team, damage, speed, x, y, directionX, directionY);
    }
}