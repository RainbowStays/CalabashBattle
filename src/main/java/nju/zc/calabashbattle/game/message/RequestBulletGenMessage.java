package nju.zc.calabashbattle.game.message;

public class RequestBulletGenMessage extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private final int cID;
    private final double pointGoalX;
    private final double pointGoalY;

    public RequestBulletGenMessage(int cID, double pointGoalX, double pointGoalY){
        this.cID = cID;
        this.pointGoalX = pointGoalX;
        this.pointGoalY = pointGoalY;

    }

    public int getCreatureID(){
        return cID;
    }

    public double getPointGoalX(){
        return pointGoalX;
    }

    public double getPointGoalY(){
        return pointGoalY;
    }
    

    @Override
    public MsgType getType(){
        return MsgType.MSG_REQ_BULLET_GEN;
    }
    
}