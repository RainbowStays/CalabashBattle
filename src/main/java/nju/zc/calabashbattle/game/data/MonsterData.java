package nju.zc.calabashbattle.game.data;

import nju.zc.calabashbattle.game.creature.*;

//角色初始化数据或传值使用
public class MonsterData extends AbstractCreatureData{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MonsterData(int id, String name, int hp, int hpMax, int attack, int defend, int attackSpeed, int team, int posX, int posY, double direction) {
        super(id, name, hp, hpMax, attack, defend, attackSpeed, team, posX, posY, direction);
    }

    @Override
    public AbstractCreature generateCreature(){
        return new Monster(this);
    }
}