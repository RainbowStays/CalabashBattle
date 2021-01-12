package nju.zc.calabashbattle.game.message;

public class BulletDeathMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;
    
    private final int deathID;

    public BulletDeathMsg(int ID){
        this.deathID = ID;
    }

    public int getID(){
        return this.deathID;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_BULLET_DEATH;
    }
}