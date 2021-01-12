package nju.zc.calabashbattle.game.data;

import nju.zc.calabashbattle.game.bullet.AbstractBullet;
import nju.zc.calabashbattle.game.bullet.Bullet;

public class BulletData extends AbstractBulletData {
    private static final long serialVersionUID = 1L;

    public BulletData(int bID, int cID, int team, int damage, int speed, double x, double y, double directionX, double directionY) {
        super(bID, cID, team, damage, speed, x, y, directionX, directionY);
    }
    
    @Override
    public AbstractBullet generateBullet() {
        return new Bullet(id, this);
    }

}