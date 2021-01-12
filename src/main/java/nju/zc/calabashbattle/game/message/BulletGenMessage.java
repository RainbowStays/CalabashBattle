package nju.zc.calabashbattle.game.message;

import nju.zc.calabashbattle.game.bullet.AbstractBullet;
import nju.zc.calabashbattle.game.bullet.Bullet;
import nju.zc.calabashbattle.game.data.AbstractBulletData;

public class BulletGenMessage extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private final AbstractBulletData bData;

    public BulletGenMessage(Bullet b){
        this.bData = b.generateData();
    }

    public BulletGenMessage(AbstractBulletData bData){
        this.bData = bData;
    }

    public AbstractBullet generateBullet(){
        return bData.generateBullet();
    }

    public AbstractBulletData getBulletData(){
        return bData;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_BULLET_GEN;
    }
    
}