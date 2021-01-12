package nju.zc.calabashbattle.game.message;

public class MatchMessage extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final String Username;//匹配成功时对方的用户名
    private final int Camp;//匹配成功时己方的阵营, 0代表葫芦娃， 1代表妖精。

    public MatchMessage(String u, int c){
        this.Username = u;
        this.Camp = c;
    }

    public String getUser(){
        return this.Username;
    }

    public int getCamp(){
        return this.Camp;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_MATCH;
    }
}