package nju.zc.calabashbattle.game.creature;

import nju.zc.calabashbattle.game.bullet.AbstractBullet;
import nju.zc.calabashbattle.game.bullet.Bullet;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;
import nju.zc.calabashbattle.game.data.MonsterData;

public class Monster extends AbstractCreature {

    public Monster(int id, String name, int hp, int attack, int defend, int attackSpeed, int x, int y, double direction) {
        super(id, name, 1, hp, attack, defend, attackSpeed, x, y, direction);
    }

    public Monster(MonsterData mData){
        super(mData.id, mData.name, mData.team, mData.hp, mData.attack, mData.defend, mData.attackSpeed, mData.posX, mData.posY, mData.direction);
    }

    public AbstractCreatureData generateData(){
        return new MonsterData(id, name, hp, hpMax, attack, defend, attackSpeed, team, x, y, direction);
    }

    public AbstractBullet generateBullet(double PointGoalX, double PointGoalY){
        int towards = (PointGoalX - (x + 0.5) * Define.BOX_X_LEN) > 0 ? 1 : 0;
        double directionX = PointGoalX - (x + towards) * Define.BOX_X_LEN;
        double directionY = PointGoalY - (y + 0.5) * Define.BOX_Y_LEN;
        if(directionX == 0 && directionY == 0)directionX = 1.0 * (towards == 1 ? 1 : -1);
        return new Bullet(id, team, attack, Define.BULLETSPEED, (x + towards)*Define.BOX_X_LEN, (y + 0.5) * Define.BOX_Y_LEN, directionX , directionY);
    }
}