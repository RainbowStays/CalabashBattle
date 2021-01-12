package nju.zc.calabashbattle.game.message;

public abstract class AbstractMessage implements java.io.Serializable {
    /**
     * Message: 用于封装服务器与客户端的交流信息
     */
    public enum MsgType{
        MSG_UNDEFINED,
        MSG_LOGIN,
        MSG_MATCH,
        MSG_LOGINREPLY, 
        MSG_FIELD_CREATE,
        MSG_BULLET_GEN,
        MSG_CREATURE_MOVE,
        MSG_BULLET_DEATH,
        MSG_GAMEREADY,
        MSG_GAMEOVER,
        MSG_CREATURE_CREATE,
        MSG_CREATURE_DAMAGE,
        MSG_BULLET_SYNC,
        MSG_REQ_BULLET_GEN,
        MSG_CREATURE_DIRECTION_CHANGED,
        MSG_GAME_EXIT,
        MSG_GAME_INFO
    }

    private long generateTime;

    AbstractMessage(){
        generateTime = System.currentTimeMillis();
    }


    private static final long serialVersionUID = 1L;

    public abstract MsgType getType();

    public long getTimeCreate(){
        return generateTime;
    }

    public void setTime(long time){
        generateTime = time;
    }
}