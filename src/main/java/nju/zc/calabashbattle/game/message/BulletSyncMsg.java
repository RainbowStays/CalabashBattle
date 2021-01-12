package nju.zc.calabashbattle.game.message;

import java.util.Set;

import nju.zc.calabashbattle.game.data.AbstractBulletData;

public class BulletSyncMsg extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private final Set<AbstractBulletData> bSyncData;

    public BulletSyncMsg(Set<AbstractBulletData> s){
        this.bSyncData = s;
    }

    public Set<AbstractBulletData> getBulletSyncData(){
        return bSyncData;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_BULLET_SYNC;
    }
    
}