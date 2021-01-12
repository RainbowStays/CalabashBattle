package nju.zc.calabashbattle.game.message;

public class GameInfoMessage extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final int gameID;
    private final int team;
    private final String team0;
    private final String team1;
    private String serverIP;

    public GameInfoMessage(int gameID, int team, String team0, String team1){
        this.gameID = gameID;
        this.team = team;
        this.team0 = team0;
        this.team1 = team1;
    }

    public int getTeam(){
        return this.team;
    }

    public int getGameID() {
        return this.gameID;
    }

    public String getTeam0() {
        return this.team0;
    }

    public String getTeam1() {
        return this.team1;
    }

    public String getServerIP(){
        return serverIP;
    }

    public void setServerIP(String ip){
        serverIP = ip;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_GAME_INFO;
    }

}