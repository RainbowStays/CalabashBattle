package nju.zc.calabashbattle.game.message;

public class CreatureMoveMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final int ID;
    private final int newX;
    private final int newY;

    public CreatureMoveMsg(int ID, int newX, int newY){
        this.ID = ID;
        this.newX = newX;
        this.newY = newY;
    }

    public int getID(){
        return ID;
    }

    public int getNewX(){
        return newX;
    }

    public int getNewY(){
        return newY;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_CREATURE_MOVE;
    }
}