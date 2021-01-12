package nju.zc.calabashbattle.game.message;

import nju.zc.calabashbattle.game.data.AbstractCreatureData;

public class CreatureCreateMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final AbstractCreatureData cData;

    public CreatureCreateMsg(AbstractCreatureData cData){
        this.cData = cData; 
    }

    public AbstractCreatureData getData(){
        return cData;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_CREATURE_CREATE;
    }
}