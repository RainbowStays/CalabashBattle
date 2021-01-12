package nju.zc.calabashbattle.game.message;

public class LoginReplyMessage extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    public enum loginReplyType{
        TRUE,
        wrongUsername
    }

    loginReplyType replyType;
    int run_id;

    public LoginReplyMessage(loginReplyType r, int run_id){
        this.replyType = r;
        this.run_id = run_id;
    }

    public loginReplyType getReplyType(){
        return replyType;
    }

    public int getRunID(){
        return run_id;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_LOGINREPLY;
    }
    
}