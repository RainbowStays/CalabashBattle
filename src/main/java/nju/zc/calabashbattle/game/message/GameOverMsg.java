package nju.zc.calabashbattle.game.message;

public class GameOverMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final int victoryTeam;
    private final int victoryReason;

    public GameOverMsg(int team, int reason){
        this.victoryTeam = team;
        this.victoryReason = reason;
    }

    public int getVictoryTeam() {
        return this.victoryTeam;
    }

    public int getVictoryReason() {
        return victoryReason;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_GAMEOVER;
    }

}