package nju.zc.calabashbattle.game.message;

public class CreatureDamageMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;

    private final int injuredID;//受伤害生物的id
    private final int attackerID;//攻击者id
    private final int damage;//未被防御削减过的伤害
    private final boolean isDead;



    public CreatureDamageMsg(int cID, int attackerID, int d, boolean isDead){
        this.injuredID = cID;
        this.attackerID = attackerID;
        this.damage = d;
        this.isDead = isDead;
    }

    public int getAttackerID(){
        return this.attackerID;
    }

    public int getInjuredID(){
        return this.injuredID;
    }

    public int getDamage(){
        return this.damage;
    }

    public boolean getIsDead(){
        return this.isDead;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_CREATURE_DAMAGE;
    }
}