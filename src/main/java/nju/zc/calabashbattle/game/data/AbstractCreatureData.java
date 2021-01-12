package nju.zc.calabashbattle.game.data;

import java.io.Serializable;

import nju.zc.calabashbattle.game.creature.AbstractCreature;


//角色初始化数据或传值使用
public abstract class AbstractCreatureData implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    public final int id;
    public final String name;
    
    public final int hp;
    public final int hpMax;
    public final int attack;
    public final int defend;
    public final int attackSpeed;

    public final int team;
    public final int posX;
    public final int posY;
    public final double direction;

    public AbstractCreatureData(int id, String name, int hp, int hpMax, int attack, int defend, int attackSpeed, int team, int posX, int posY, double direction) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.hpMax = hpMax;
        this.attack = attack;
        this.defend = defend;
        this.attackSpeed = attackSpeed;
        this.team = team;
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
    }

    public abstract AbstractCreature generateCreature();
}