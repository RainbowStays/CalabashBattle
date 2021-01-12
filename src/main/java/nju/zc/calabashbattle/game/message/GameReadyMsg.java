package nju.zc.calabashbattle.game.message;

public class GameReadyMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final int time;

    public GameReadyMsg(int time){
        this.time = time;
    }

    public int getTime(){
        return time;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_GAMEREADY;
    }

}