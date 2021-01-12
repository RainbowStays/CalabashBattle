package nju.zc.calabashbattle.game.message;

public class GameExitMessage extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    public GameExitMessage(){
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_GAME_EXIT;
    }

}