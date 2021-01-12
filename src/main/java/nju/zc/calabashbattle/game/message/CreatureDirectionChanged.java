package nju.zc.calabashbattle.game.message;

public class CreatureDirectionChanged extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final int cID;
    private final int team;
    private final double newDirection;

    public CreatureDirectionChanged(int cID, int team, double newDirection){
        this.cID = cID;
        this.team = team;
        this.newDirection = newDirection;
    }

    public int getID(){
        return this.cID;
    }

    public int getTeam(){
        return this.team;
    }

    public double getNewDirection(){
        return this.newDirection;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_CREATURE_DIRECTION_CHANGED;
    }
}