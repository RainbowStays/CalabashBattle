package nju.zc.calabashbattle.game.message;

public class LoginMessage extends AbstractMessage{
    
    private static final long serialVersionUID = 1L;

    private final String username;
    
    public LoginMessage(String username){
        this.username = username;
    }

    public final String getUsername(){
        return username;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_LOGIN;
    }
    
}